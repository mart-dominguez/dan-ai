# 05 - Personalización con system prompts

Da al LLM un "rol" y reglas de comportamiento mediante un *system prompt*.

## Objetivo

Darle al LLM un "rol" y reglas de comportamiento mediante un *system prompt*.

## Teoría

- **System prompt**: instrucciones de contexto que definen el rol, tono y reglas del asistente. Se envía antes del mensaje del usuario.
- **User prompt**: la pregunta concreta del usuario.
- **`ChatClient` con `defaultSystem(...)`**: fija un system prompt por defecto para todos los pedidos.
- **Parámetros con `{placeholder}`**: Spring AI permite plantillas con variables reemplazables.
- **Por qué importa**: un system prompt convierte un LLM genérico en un "sommelier" con personalidad y límites.

Un system prompt ayuda a la conducta, **no reemplaza la recuperación**: el modelo puede alucinar vinos porque sigue sin conocer el catálogo real.

## Alcance de implementación

- Definir un system prompt: "Sos un sommelier experto en vinos del catálogo. Respondé en español, en breve, y si no sabés algo decílo."
- Usar `ChatClient.Builder` con `defaultSystem(...)`.
- Endpoint `POST /api/chat` reutilizado, ahora con comportamiento guiado.

## Ejemplo de `ChatClient` con system prompt

```java
ChatClient chatClient = chatClientBuilder
    .defaultSystem("Sos un sommelier experto en vinos del catálogo. " +
                   "Respondé en español, en breve, y si no sabés algo decílo.")
    .build();
```

También puede usarse una plantilla con variables:

```java
.defaultSystem("Sos un sommelier de la región {region}. Respondé en español, en breve.")
```

## Resultado esperado

Las respuestas adoptan el rol y tono definidos, aunque el LLM todavía no conoce el catálogo real (puede alucinar).

## Verificación manual

Mandar "Recomendá un tinto" y verificar que la respuesta tenga el tono de sommelier y admita limitaciones.

```bash
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '{"message":"Recomendá un tinto"}'
```