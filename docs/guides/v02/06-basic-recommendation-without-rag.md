# 06 - Recomendación sin RAG (contexto en el prompt)

Demuestra el problema base: recomendar vinos pasándole el catálogo entero dentro del prompt. Esto revela las limitaciones que RAG resolverá.

## Objetivo

Demostrar el problema base: recomendar vinos pasándole el catálogo entero dentro del prompt. Esto revela las limitaciones que RAG resolverá.

## Teoría

- **Inyección de contexto**: incluir datos del catálogo como texto en el prompt del usuario.
- **Limitaciones**: el contexto cabe hasta un límite de tokens; no escala con muchos vinos; el LLM puede ignorar datos.
- **Por qué RAG luego**: en lugar de pasar *todo*, pasar solo los vinos *relevantes* recuperados por similitud.
- **`UserMessage` dinámico**: construir el prompt concatenando los vinos recuperados de la BD relacional.

## Alcance de implementación

- `WineService` con método que traiga todos los vinos y los formatee como texto.
- Endpoint `POST /api/recommend` que arma un prompt con el catálogo y la pregunta del usuario, y lo envíe al `ChatClient`.

> A diferencia del `POST /api/chat` del Hito 4/5, este endpoint incluye el catálogo completo dentro del prompt del usuario.

## Endpoint

```bash
curl -X POST http://localhost:8080/api/recommend/basic \
  -H "Content-Type: application/json" \
  -d '{"message":"¿Qué tinto barato recomendás?"}'
```

## Estructura del prompt

```text
SYSTEM: Sos un sommelier experto. Respondé en español, en breve.

USER:
Catálogo de vinos:
- 1. Malbec Reserva — Red, Malbec, Mendoza, 18.0, maridaje: grilled meat...
- 2. Sauvignon Blanc Costa — White, Sauvignon Blanc, Casablanca, 16.5...

Pregunta del usuario: ¿Qué tinto barato recomendás?
```

## Resultado esperado

El asistente recomienda vinos reales del catálogo, pero el prompt crece con la cantidad de vinos (problema a resolver).

## Verificación manual

Mandar "¿Qué tinto barato recomendás?" y comprobar que la respuesta menciona vinos del catálogo. **Contar el tamaño del prompt enviado** (por ejemplo logueando la cantidad de caracteres o tokens) para evidenciar que no escala.