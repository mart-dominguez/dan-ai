### Mejoras en la creación del ChatClient

Especificación de mejoras sobre `ChatController`, aplicadas en la creación del
`ChatClient` (constructor) y en el endpoint de chat.

#### Contexto

El `ChatClient` se construye únicamente con `defaultSystem(...)`. El resto de
parámetros se resuelven con los valores por defecto de Ollama, lo que es
problemático para el modelo `qwen3:14b`:

- `qwen3` es un modelo de razonamiento (*thinking*). En Ollama 0.12+ el modo
  thinking se auto-habilita si no se setea explícitamente, añadiendo latencia y
  tokens adicionales antes de la respuesta (en conflicto con "keep the response
  concise").
- `num-ctx` por defecto es `2048` tokens (contexto muy chico para un modelo de
  14B).
- `num-predict` por defecto es `-1` (generación no acotada).

> Nota: `OllamaOptions` está deprecado en Spring AI 2.0.0. Usar
> `OllamaChatOptions`.

#### Mejora 1 — `defaultOptions` (OllamaChatOptions)

Setear opciones por defecto aplicables a todas las llamadas:

| Opción | Valor | Motivo |
| --- | --- | --- |
| `temperature` | `0.3` | Respuestas más factuales y concisas. |
| `numPredict` | `512` | Acota el largo de salida (default `-1` = ilimitado). |
| `numCtx` | `8192` | Agranda la ventana de contexto (default `2048`). |
| `disableThinking()` | — | Evita el razonamiento automático de `qwen3` (latencia/tokens). |

Estas opciones pueden sobreescribirse por petición con `.options(...)` en la
cadena del prompt.

#### Mejora 2 — Endpoint de streaming

Agregar `POST /api/chat/stream` que devuelve `Flux<String>` como SSE usando
`.stream().content()`. Mejora la experiencia de chat al enviar los tokens a
medida que se generan, en lugar de bloquear hasta tener la respuesta completa.

No requiere dependencias nuevas: `reactor-core` y `spring-webflux` ya están en
el classpath (vía Spring Boot 4.1), por lo que Spring MVC adapta el tipo
reactivo `Flux` como Server-Sent Events.

#### Implementación

```java
package ar.edu.utn.frsf.isi.dan.dan_ia.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

	private final ChatClient chatClient;

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

	@PostMapping
	public ChatResponse chat(@RequestBody ChatRequest request) {
		String answer = this.chatClient.prompt()
				.user(request.message())
				.call()
				.content();

		return new ChatResponse(answer);
	}

	@PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public Flux<String> chatStream(@RequestBody ChatRequest request) {
		return this.chatClient.prompt()
				.user(request.message())
				.stream()
				.content();
	}

	public record ChatRequest(String message) {
	}

	public record ChatResponse(String answer) {
	}
}
```

#### Endpoints resultantes

| Método | Path | Produces | Descripción |
| --- | --- | --- | --- |
| `POST` | `/api/chat` | `application/json` | Respuesta completa síncrona. |
| `POST` | `/api/chat/stream` | `text/event-stream` | Tokens en streaming (SSE). |

#### Opciones no implementadas (futuras)

- `defaultAdvisors(new SimpleLoggerAdvisor())` + log level DEBUG para depuración.
- `MessageChatMemoryAdvisor` con `ChatMemory` en memoria + `conversationId` para conversaciones multi-turno.
- `.entity(Class)` para salida estructurada.
- `.tools(...)` con métodos `@Tool` para invocación de funciones backend.
- `QuestionAnswerAdvisor` + vector store para RAG.

https://docs.spring.io/spring-ai/reference/api/chatclient.html
https://docs.spring.io/spring-ai/reference/api/chat/ollama-chat.html
