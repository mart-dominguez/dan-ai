# 04 - Chatbot simple con `ChatClient` y Ollama

Integra Ollama con Spring AI y responde a un prompt simple del usuario, **sin usar aún el catálogo**.

## Objetivo

Integrar Ollama con Spring AI y responder a un prompt simple del usuario, sin usar aún el catálogo.

## Teoría

- **`ChatClient`**: interfaz principal de Spring AI para hablar con un LLM. Reemplaza al uso directo de HTTP contra Ollama.
- **Autoconfiguración de Spring AI**: `spring-ai-starter-model-ollama` configura el cliente a partir de properties.
- **Properties clave**: `spring.ai.ollama.base-url`, `spring.ai.ollama.chat.model`.
- **Prompt y respuesta**: el usuario envía texto, el LLM devuelve texto.
- **Sin estado**: el LLM no recuerda conversaciones previas salvo que lo modelemos.

## Endpoint

```bash
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '{"message":"Hola, ¿qué sos?"}'
```

## Configuración

`application.properties`:

```properties
spring.ai.ollama.base-url=http://localhost:11434
spring.ai.ollama.chat.model=llama3
```

## Preparar Ollama

```bash
ollama pull llama3
ollama serve   # suele estar corriendo por defecto
```

`ChatClient.Builder` es inyectado por Spring AI a partir de las properties anteriores.

## Idea clave

`ChatClient` oculta la llamada HTTP de bajo nivel a Ollama y ofrece una API de prompt estilo Spring.

En este paso el LLM puede responder preguntas genéricas, pero no conoce el catálogo de vinos salvo que se le entregue contexto (Hito 6) o se use RAG (Hito 10).

## Resultado esperado

Al mandar "Hola, ¿qué sos?" el endpoint responde con texto generado por Ollama.

## Verificación manual

```bash
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '{"message":"Hola, ¿qué sos?"}'
```

Verificar que Ollama devuelve una respuesta generada.