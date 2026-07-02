# Chat Client Guide

A practical guide to the current chat implementation in the `dan-ia` project, built
with Spring AI and Ollama.

## 1. Overview

`dan-ia` is a small Spring Boot service that exposes an AI chatbot over HTTP. A
client sends a text message to a REST endpoint, the service forwards it to a
local large language model (LLM), and the model's answer is returned to the
client.

Two Spring AI building blocks make this work:

- **`ChatClient`** — Spring AI's high-level, fluent API for talking to a chat
  model. You build it once from a `ChatClient.Builder` (auto-configured by Spring
  Boot) and then call `.prompt().user(...).call().content()` per request. It
  hides the low-level details of assembling prompts and parsing responses.
- **Ollama** — a local runtime that serves LLMs on your machine
  (`http://localhost:11434` by default). Spring AI's Ollama starter
  auto-configures an `OllamaChatModel` that `ChatClient` uses under the hood, so
  the model runs entirely on your hardware — no cloud API keys required.

The configured model is `qwen3:14b`, a reasoning-capable model from the Qwen3
family.

## 2. Current Capabilities

The application currently can:

- Receive a user message as JSON (`{ "message": "..." }`) on `POST /api/chat`
  and return the model's answer as JSON (`{ "answer": "..." }`).
- Stream the answer token-by-token on `POST /api/chat/stream` as Server-Sent
  Events (SSE), so the UI can render text as it is generated.
- Apply a **default system prompt** to every call ("You are a helpful
  assistant. Answer in simple words and keep the response concise.").
- Apply **default model options** to every call: low temperature, capped output
  length, enlarged context window, and thinking mode disabled (see section 4).
- Keep the request/response shape simple with Java `record` DTOs.

There is no persistence, no conversation memory, and no authentication yet —
each request is independent and stateless.

## 3. Configuration and Dependencies

### Dependencies (`pom.xml`)

| Dependency | Purpose |
| --- | --- |
| `spring-boot-starter-webmvc` | Servlet-based REST endpoints. |
| `spring-boot-starter-actuator` | Health/info/observability endpoints. |
| `spring-ai-starter-model-ollama` | Auto-configures `OllamaApi` and `OllamaChatModel`, and provides `ChatClient.Builder`. |
| `lombok` (optional) | Boilerplate reduction (not strictly required by the chat flow). |
| `spring-boot-devtools` (runtime) | Hot reload during development. |

The Spring AI BOM is imported for version alignment:

```xml
<properties>
    <java.version>21</java.version>
    <spring-ai.version>2.0.0</spring-ai.version>
</properties>
...
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.springframework.ai</groupId>
            <artifactId>spring-ai-bom</artifactId>
            <version>${spring-ai.version}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

> The project uses Spring Boot `4.1.0` and Spring AI `2.0.0`, targeting Java 21.

### Configuration (`src/main/resources/application.properties`)

```properties
spring.application.name=dan-ia

spring.ai.ollama.base-url=http://localhost:11434
spring.ai.ollama.chat.model=qwen3:14b
spring.ai.ollama.init.pull-model-strategy=never
spring.ai.ollama.init.timeout=5m
spring.ai.ollama.init.max-retries=0
spring.ai.ollama.init.chat.include=true
spring.ai.ollama.init.chat.additional-models=[]
```

Key settings:

| Property | Meaning |
| --- | --- |
| `spring.ai.ollama.base-url` | Where the Ollama server is running. |
| `spring.ai.ollama.chat.model` | The model to use for chat completions. |
| `spring.ai.ollama.init.pull-model-strategy` | `never` = the app will not try to download the model at startup; it must already be present. |
| `spring.ai.ollama.init.timeout` / `max-retries` | Limits for auto-pulling (only relevant when the strategy is not `never`). |

Model-specific options (temperature, context window, etc.) are **not** set in
properties; they are set programmatically on the `ChatClient` (see section 4).

## 4. Step-by-Step Implementation Notes

### `DanIaApplication.java` (main class)

Standard `@SpringBootApplication` entry point. Component scanning starts in the
`ar.edu.utn.frsf.isi.dan.dan_ia` package. Nothing AI-specific lives here — the
Ollama auto-configuration kicks in because the
`spring-ai-starter-model-ollama` dependency is on the classpath and
`spring.ai.model.chat` defaults to `ollama`.

### `ChatController.java` (the only AI-aware class)

This `@RestController` is the heart of the implementation. It is mapped to
`/api/chat` and holds a single `ChatClient` instance built once in the
constructor.

```java
public ChatController(ChatClient.Builder chatClientBuilder) {
    this.chatClient = chatClientBuilder
            .defaultSystem("You are a helpful assistant. Answer in simple words and keep the response concise.")
            .defaultOptions(OllamaChatOptions.builder()
                    .temperature(0.3)
                    .numPredict(512)
                    .numCtx(8192)
                    .disableThinking()
                    .build())
            .build();
}
```

Responsibilities:

- **`defaultSystem(...)`** — sets the system message applied to every prompt.
  This is where the assistant's persona/behavior is defined.
- **`defaultOptions(OllamaChatOptions...)`** — sets model defaults that matter
  for `qwen3:14b`:
  - `temperature(0.3)` — lower randomness for factual, concise answers.
  - `numPredict(512)` — caps the number of generated tokens (default is `-1`,
    i.e. unbounded).
  - `numCtx(8192)` — enlarges the context window (Ollama default is `2048`).
  - `disableThinking()` — `qwen3` is a reasoning model; on Ollama 0.12+ it
    auto-enables "thinking" unless told otherwise. Disabling it avoids extra
    latency and tokens, in line with the "concise" system prompt.

> `OllamaOptions` is deprecated in Spring AI 2.0.0. Use `OllamaChatOptions`.

Two endpoints are exposed:

```java
// Synchronous: returns the full answer as JSON.
@PostMapping
public ChatResponse chat(@RequestBody ChatRequest request) {
    String answer = this.chatClient.prompt()
            .user(request.message())
            .call()
            .content();
    return new ChatResponse(answer);
}

// Streaming: returns tokens as they arrive, as SSE.
@PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
public Flux<String> chatStream(@RequestBody ChatRequest request) {
    return this.chatClient.prompt()
            .user(request.message())
            .stream()
            .content();
}
```

The DTOs are minimal Java records, so request/response JSON is automatic:

```java
public record ChatRequest(String message) {}
public record ChatResponse(String answer) {}
```

### `DanIaApplicationTests.java`

Only contains the default `contextLoads()` smoke test. There are no
AI-specific tests yet.

## 5. Endpoint Flow

The synchronous flow for `POST /api/chat`:

1. **Client** sends `POST /api/chat` with body `{"message":"What is Spring AI?"}`.
2. **Spring MVC** deserializes the body into a `ChatRequest` record and routes
   it to `ChatController.chat(...)`.
3. **Controller** calls `this.chatClient.prompt().user(request.message())...`,
   which assembles a `Prompt` containing the default system message plus the
   user message, and applies the default `OllamaChatOptions`.
4. **Spring AI** hands the prompt to the auto-configured `OllamaChatModel`,
   which performs an HTTP POST to Ollama's `/api/chat` endpoint
   (`http://localhost:11434`).
5. **Ollama** runs `qwen3:14b` and streams/returns the generated text.
6. **Spring AI** parses the response; `.content()` returns just the answer text
   as a `String`.
7. **Controller** wraps it in a `ChatResponse` record; Spring MVC serializes it
   to `{"answer":"..."}` and returns it to the client.

For `POST /api/chat/stream`, the same assembly happens, but `.stream().content()`
returns a `Flux<String>`. Because `reactor-core` and `spring-webflux` are on the
classpath (transitively via Spring Boot 4.1), Spring MVC adapts the reactive
`Flux` into an SSE stream, so the client receives `data: <token>` events as the
model generates them.

## 6. How to Run and Test

### Prerequisites

- Java 21
- Maven (the project includes `mvnw`, so a local Maven is optional)
- [Ollama](https://ollama.com/download) installed and running

### 1. Start Ollama

```bash
ollama serve
```

It listens on `http://localhost:11434` by default.

### 2. Verify / pull the model

The app is configured with `pull-model-strategy=never`, so the model must be
present locally. Check or pull it:

```bash
ollama list              # see what's available
ollama pull qwen3:14b    # download if missing (large download)
```

### 3. Run the Spring Boot application

```bash
./mvnw spring-boot:run
```

The app starts on `http://localhost:8080`.

### 4. Test the endpoints

Synchronous chat:

```bash
curl -s -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '{"message":"What is Spring AI in one sentence?"}'
```

Expected response:

```json
{"answer":"Spring AI is a Spring module for integrating AI models and tools into Spring applications."}
```

Streaming chat (SSE):

```bash
curl -N -X POST http://localhost:8080/api/chat/stream \
  -H "Content-Type: application/json" \
  -d '{"message":"Explain recursion briefly."}'
```

You should see `data:` lines arrive incrementally as the model generates text.

## 7. Possible Improvements and Further Work

- **Better request/response DTOs** — add validation (`@NotBlank`), metadata
  fields (model name, token usage, latency), and distinct request/response
  types per endpoint.
- **System prompts** — make the system prompt configurable per request or per
  "persona", using `defaultSystem(Consumer<PromptSystemSpec>)` with templated
  variables.
- **Chat memory** — register a `MessageChatMemoryAdvisor` with a `ChatMemory`
  store (in-memory or JDBC) and pass a `conversationId` per call to support
  multi-turn conversations.
- **Prompt templates** — use `.user(u -> u.text("...{var}...").param(...))` to
  build reusable, parameterized prompts instead of raw strings.
- **Structured output** — use `.entity(MyType.class)` (or Ollama's
  `.outputSchema(jsonSchema)`) to return typed Java objects instead of free
  text.
- **Tool calling** — annotate methods with `@Tool` and register them via
  `.defaultTools(...)` / `.tools(...)` so the model can invoke backend logic
  (`qwen3` supports tool calling; Ollama >= 0.2.8, >= 0.4.6 for streaming).
- **RAG** — add an embedding model and a vector store, then use
  `QuestionAnswerAdvisor` to ground answers in your own documents.
- **Error handling** — add a `@ControllerAdvice` to map model/Ollama failures
  (timeouts, connection refused, context overflow) to proper HTTP statuses.
- **Tests** — add `@SpringBootTest` tests using `MockChatModel` or WireMock to
  avoid requiring a live Ollama in CI.
- **API documentation** — integrate springdoc-openapi to publish OpenAPI docs
  for the chat endpoints, and enable actuator observability/metrics for AI
  calls.

### References

- Spring AI ChatClient: https://docs.spring.io/spring-ai/reference/api/chatclient.html
- Spring AI Ollama chat: https://docs.spring.io/spring-ai/reference/api/chat/ollama-chat.html
