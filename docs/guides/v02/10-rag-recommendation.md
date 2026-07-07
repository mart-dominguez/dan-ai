# 10 - Recomendación con RAG

Combina búsqueda semántica + LLM para responder con RAG: recuperar vinos relevantes y dárselos como contexto al `ChatClient`.

## Objetivo

Combinar búsqueda semántica + LLM para responder con RAG: recuperar vinos relevantes y dárselos como contexto al `ChatClient`.

## Teoría

- **RAG = Retrieve + Generate**: (1) recuperar documentos relevantes del vector store, (2) armar un prompt con esos documentos como contexto, (3) dejar que el LLM genere la respuesta.
- **`RetrievalAugmentationAdvisor` / patrones de Spring AI**: Spring AI ofrece advisors que automatizan el retrieval y la inyección de contexto en el prompt.
- **Context window**: ahora solo pasamos los vinos relevantes (no todo el catálogo), escalando mejor.
- **Citación / fundamento**: la respuesta debe apoyarse en los vinos recuperados; se puede pedir al LLM que cite nombres o ids.

## Alcance de implementación

- Refactorizar `POST /api/recommend` para que:
  1. Recupere top-k vinos vía búsqueda semántica.
  2. Construya (o use un advisor para) el prompt con contexto.
  3. Llame al `ChatClient` y devuelva la respuesta.
- Mantener el system prompt de sommelier del Hito 5.

## Endpoint

```bash
curl -X POST http://localhost:8080/api/recommend \
  -H "Content-Type: application/json" \
  -d '{"message":"¿Qué vino sirve con mariscos?","topK":3}'
```

## Flujo RAG

```text
mensaje del usuario
  -> SemanticSearchService.search(mensaje, topK)   (recupera vinos)
  -> construir prompt con vinos como contexto
  -> ChatClient.prompt(system + context + question)
  -> respuesta del LLM fundamentada en el catálogo
```

## Comparación con el Hito 6

| | Hito 6 (sin RAG) | Hito 10 (RAG) |
| --- | --- | --- |
| Qué se pasa al LLM | Catálogo completo | Solo top-k relevantes |
| Escala con el catálogo | No | Sí |
| Riesgo de alucinación | Moderado | Bajo (contexto acotado) |

## Resultado esperado

Preguntas como "¿Qué vino sirve con mariscos?" se responden citando vinos reales del catálogo, recuperados por semántica.

## Verificación manual

Probar varias preguntas naturales y verificar que las recomendaciones correspondan a vinos existentes en la BD.

```bash
curl -X POST http://localhost:8080/api/recommend \
  -H "Content-Type: application/json" \
  -d '{"message":"¿Qué vino sirve con mariscos?","topK":3}'
```

Comprobar que la respuesta **nombra vinos del catálogo** y que el motivo se basa en sus campos reales.