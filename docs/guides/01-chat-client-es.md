# Guía del Chat Client

Guía práctica de la implementación actual de chat en el proyecto `dan-ia`,
construida con Spring AI y Ollama.

## 1. Overview

`dan-ia` es un pequeño servicio Spring Boot que expone un chatbot de IA por
HTTP. Un cliente envía un mensaje de texto a un endpoint REST, el servicio lo
reenvía a un modelo de lenguaje grande (LLM) local, y la respuesta del modelo
se devuelve al cliente.

Dos componentes de Spring AI hacen esto posible:

- **`ChatClient`** — la API fluida de alto nivel de Spring AI para comunicarse
  con un modelo de chat. Se construye una sola vez a partir de un
  `ChatClient.Builder` (auto-configurado por Spring Boot) y luego se invoca
  `.prompt().user(...).call().content()` por cada petición. Oculta los detalles
  de bajo nivel de armar prompts y parsear respuestas.
- **Ollama** — un runtime local que sirve LLMs en tu máquina
  (`http://localhost:11434` por defecto). El starter de Ollama de Spring AI
  auto-configura un `OllamaChatModel` que `ChatClient` usa por debajo, de modo
  que el modelo corre completamente en tu hardware — sin claves de API en la
  nube.

El modelo configurado es `qwen3:14b`, un modelo con capacidad de razonamiento
de la familia Qwen3.

## 2. Capacidades actuales

La aplicación actualmente puede:

- Recibir un mensaje de usuario como JSON (`{ "message": "..." }`) en
  `POST /api/chat` y devolver la respuesta del modelo como JSON
  (`{ "answer": "..." }`).
- Transmitir la respuesta token por token en `POST /api/chat/stream` como
  Server-Sent Events (SSE), de modo que la UI pueda renderizar el texto a
  medida que se genera.
- Aplicar un **system prompt por defecto** a cada llamada ("You are a helpful
  assistant. Answer in simple words and keep the response concise.").
- Aplicar **opciones de modelo por defecto** a cada llamada: temperatura baja,
  largo de salida acotado, ventana de contexto ampliada y modo thinking
  deshabilitado (ver sección 4).
- Mantener la forma del request/response simple con DTOs `record` de Java.

No hay persistencia, ni memoria de conversación, ni autenticación todavía —
cada petición es independiente y stateless.

## 3. Configuración y dependencias

### Dependencias (`pom.xml`)

| Dependencia | Propósito |
| --- | --- |
| `spring-boot-starter-webmvc` | Endpoints REST basados en servlets. |
| `spring-boot-starter-actuator` | Endpoints de health/info/observabilidad. |
| `spring-ai-starter-model-ollama` | Auto-configura `OllamaApi` y `OllamaChatModel`, y provee el `ChatClient.Builder`. |
| `lombok` (opcional) | Reducción de boilerplate (no es estrictamente requerido por el flujo de chat). |
| `spring-boot-devtools` (runtime) | Hot reload durante el desarrollo. |

El BOM de Spring AI se importa para alinear versiones:

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

> El proyecto usa Spring Boot `4.1.0` y Spring AI `2.0.0`, sobre Java 21.

### Configuración (`src/main/resources/application.properties`)

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

Propiedades clave:

| Property | Significado |
| --- | --- |
| `spring.ai.ollama.base-url` | Dónde está corriendo el servidor Ollama. |
| `spring.ai.ollama.chat.model` | El modelo a usar para las completions de chat. |
| `spring.ai.ollama.init.pull-model-strategy` | `never` = la app no intentará descargar el modelo al iniciar; debe estar presente. |
| `spring.ai.ollama.init.timeout` / `max-retries` | Límites para auto-pull (solo relevantes si la estrategia no es `never`). |

Las opciones específicas del modelo (temperatura, ventana de contexto, etc.)
**no** se setean en properties; se setean programáticamente en el `ChatClient`
(ver sección 4).

## 4. Notas de implementación paso a paso

### `DanIaApplication.java` (clase main)

`@SpringBootApplication` estándar como punto de entrada. El component scanning
comienza en el paquete `ar.edu.utn.frsf.isi.dan.dan_ia`. No hay nada
específico de IA aquí — la auto-configuración de Ollama se activa porque la
dependencia `spring-ai-starter-model-ollama` está en el classpath y
`spring.ai.model.chat` por defecto es `ollama`.

### `ChatController.java` (la única clase consciente de IA)

Este `@RestController` es el núcleo de la implementación. Está mapeado a
`/api/chat` y mantiene una única instancia de `ChatClient` construida una vez
en el constructor.

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

Responsabilidades:

- **`defaultSystem(...)`** — setea el system message que se aplica a cada
  prompt. Acá se define la personalidad/comportamiento del asistente.
- **`defaultOptions(OllamaChatOptions...)`** — setea defaults del modelo
  importantes para `qwen3:14b`:
  - `temperature(0.3)` — menor aleatoriedad para respuestas factuales y
    concisas.
  - `numPredict(512)` — acota la cantidad de tokens generados (el default es
    `-1`, i.e. ilimitado).
  - `numCtx(8192)` — agranda la ventana de contexto (el default de Ollama es
    `2048`).
  - `disableThinking()` — `qwen3` es un modelo de razonamiento; en Ollama 0.12+
    auto-habilita "thinking" si no se le indica lo contrario. Deshabilitarlo
    evita latencia y tokens extra, en línea con el system prompt "concise".

> `OllamaOptions` está deprecado en Spring AI 2.0.0. Usar `OllamaChatOptions`.

Se exponen dos endpoints:

```java
// Síncrono: devuelve la respuesta completa como JSON.
@PostMapping
public ChatResponse chat(@RequestBody ChatRequest request) {
    String answer = this.chatClient.prompt()
            .user(request.message())
            .call()
            .content();
    return new ChatResponse(answer);
}

// Streaming: devuelve los tokens a medida que arrivegan, como SSE.
@PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
public Flux<String> chatStream(@RequestBody ChatRequest request) {
    return this.chatClient.prompt()
            .user(request.message())
            .stream()
            .content();
}
```

Los DTOs son `record` de Java mínimos, de modo que el JSON de
request/response es automático:

```java
public record ChatRequest(String message) {}
public record ChatResponse(String answer) {}
```

### `DanIaApplicationTests.java`

Solo contiene el smoke test por defecto `contextLoads()`. No hay tests
específicos de IA todavía.

## 5. Flujo del endpoint

El flujo síncrono para `POST /api/chat`:

1. **Cliente** envía `POST /api/chat` con body
   `{"message":"What is Spring AI?"}`.
2. **Spring MVC** deserializa el body en un `ChatRequest` record y lo rutea a
   `ChatController.chat(...)`.
3. **Controller** invoca `this.chatClient.prompt().user(request.message())...`,
   que arma un `Prompt` con el system message por defecto más el mensaje del
   usuario, y aplica los `OllamaChatOptions` por defecto.
4. **Spring AI** pasa el prompt al `OllamaChatModel` auto-configurado, que hace
   un HTTP POST al endpoint `/api/chat` de Ollama
   (`http://localhost:11434`).
5. **Ollama** ejecuta `qwen3:14b` y devuelve (o transmite) el texto generado.
6. **Spring AI** parsea la respuesta; `.content()` devuelve solo el texto de la
   respuesta como `String`.
7. **Controller** lo envuelve en un `ChatResponse` record; Spring MVC lo
   serializa a `{"answer":"..."}` y lo devuelve al cliente.

Para `POST /api/chat/stream`, el armado es el mismo, pero
`.stream().content()` devuelve un `Flux<String>`. Como `reactor-core` y
`spring-webflux` están en el classpath (transitivamente vía Spring Boot 4.1),
Spring MVC adapta el `Flux` reactivo a un stream SSE, de modo que el cliente
recibe eventos `data: <token>` a medida que el modelo los genera.

## 6. Cómo ejecutar y probar

### Prerrequisitos

- Java 21
- Maven (el proyecto incluye `mvnw`, así que Maven local es opcional)
- [Ollama](https://ollama.com/download) instalado y corriendo

### 1. Iniciar Ollama

```bash
ollama serve
```

Escucha en `http://localhost:11434` por defecto.

### 2. Verificar / descargar el modelo

La app está configurada con `pull-model-strategy=never`, de modo que el modelo
debe estar presente localmente. Verificá o descargalo:

```bash
ollama list              # ver qué hay disponible
ollama pull qwen3:14b    # descargar si falta (descarga grande)
```

### 3. Ejecutar la aplicación Spring Boot

```bash
./mvnw spring-boot:run
```

La app inicia en `http://localhost:8080`.

### 4. Probar los endpoints

Chat síncrono:

```bash
curl -s -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '{"message":"What is Spring AI in one sentence?"}'
```

Respuesta esperada:

```json
{"answer":"Spring AI is a Spring module for integrating AI models and tools into Spring applications."}
```

Chat en streaming (SSE):

```bash
curl -N -X POST http://localhost:8080/api/chat/stream \
  -H "Content-Type: application/json" \
  -d '{"message":"Explain recursion briefly."}'
```

Deberías ver llegar líneas `data:` de a poco a medida que el modelo genera
texto.

## 7. Mejoras posibles y trabajo futuro

- **Mejores DTOs de request/response** — agregar validación (`@NotBlank`),
  campos de metadata (nombre del modelo, uso de tokens, latencia) y tipos
  distintos de request/response por endpoint.
- **System prompts** — hacer el system prompt configurable por petición o por
  "persona", usando `defaultSystem(Consumer<PromptSystemSpec>)` con variables
  plantilladas.
- **Memoria de chat** — registrar un `MessageChatMemoryAdvisor` con un store de
  `ChatMemory` (en memoria o JDBC) y pasar un `conversationId` por llamada para
  soportar conversaciones multi-turno.
- **Plantillas de prompt** — usar
  `.user(u -> u.text("...{var}...").param(...))` para construir prompts
  reutilizables y parametrizados en lugar de strings crudos.
- **Salida estructurada** — usar `.entity(MyType.class)` (o
  `.outputSchema(jsonSchema)` de Ollama) para devolver objetos Java tipados en
  lugar de texto libre.
- **Tool calling** — anotar métodos con `@Tool` y registrarlos vía
  `.defaultTools(...)` / `.tools(...)` para que el modelo invoque lógica del
  backend (`qwen3` soporta tool calling; Ollama >= 0.2.8, >= 0.4.6 para
  streaming).
- **RAG** — agregar un modelo de embeddings y un vector store, y luego usar
  `QuestionAnswerAdvisor` para basar las respuestas en tus propios documentos.
- **Manejo de errores** — agregar un `@ControllerAdvice` para mapear fallos del
  modelo/Ollama (timeouts, conexión rechazada, overflow de contexto) a
  statuses HTTP apropiados.
- **Tests** — agregar tests `@SpringBootTest` usando `MockChatModel` o WireMock
  para no requerir un Ollama vivo en CI.
- **Documentación de API** — integrar springdoc-openapi para publicar docs
  OpenAPI de los endpoints de chat, y habilitar observabilidad/métricas de
  actuator para las llamadas de IA.

### Referencias

- Spring AI ChatClient: https://docs.spring.io/spring-ai/reference/api/chatclient.html
- Spring AI Ollama chat: https://docs.spring.io/spring-ai/reference/api/chat/ollama-chat.html
