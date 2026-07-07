# 04 - ChatClient And Ollama

The project uses Spring AI's `ChatClient` to call Ollama.

## Endpoint

```bash
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '{"message":"Hello, what are you?"}'
```

## Configuration

Ollama is configured in `application.properties`:

```properties
spring.ai.ollama.base-url=http://localhost:11434
spring.ai.ollama.chat.model=qwen3:14b
```

## Theory

`ChatClient` hides the low-level HTTP call to Ollama and gives a Spring-style prompt API.

At this step, the LLM can answer generic questions but does not know the wine catalog unless context is provided.
