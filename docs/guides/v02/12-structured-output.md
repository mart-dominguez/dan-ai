# 12 - Salida estructurada del LLM

Que el LLM devuelva una recomendación estructurada (JSON) en vez de texto libre, para que la API la pueda consumir un frontend.

## Objetivo

Que el LLM devuelva una recomendación estructurada (JSON) en vez de texto libre, para que la API la pueda consumir un frontend.

## Teoría

- **Salida estructurada**: pedirle al LLM que responda con un objeto JSON que cumpla un esquema.
- **`ChatClient.entity(...)` / `BeanOutputConverter`**: Spring AI convierte la respuesta del LLM a un POJO o record.
- **Esquema del output**: definir un `WineRecommendation` con campos como `wineId`, `name`, `reason`, `confidence`.
- **Ventajas**: parseo seguro, integración con frontend, validación.

## Esquema sugerido

```java
public record WineRecommendation(
    Long wineId,
    String name,
    String type,
    BigDecimal price,
    String reason,
    String pairingExplanation,
    Double confidence
) {}
```

## Respuesta envolvente

El endpoint puede devolver una envoltura predecible:

```json
{
  "question": "¿Qué vino sirve con mariscos?",
  "answer": "Te recomiendo el Sauvignon Blanc Costa...",
  "recommendedWines": [
    {
      "wineId": 2,
      "name": "Sauvignon Blanc Costa",
      "type": "White",
      "price": 16.50,
      "reason": "Acidez fresca que acompaña mariscos.",
      "pairingExplanation": "Las notas cítricas complementan mariscos sin dominarlos.",
      "confidence": 0.9
    }
  ]
}
```

## Alcance de implementación

- Definir `WineRecommendation` (record).
- Usar `ChatClient` con conversión a ese tipo.
- Actualizar `POST /api/recommend` para devolver `WineRecommendation` (o una lista) en JSON.

## Resultado esperado

La respuesta es un JSON con campos predecibles, no un texto libre.

## Verificación manual

```bash
curl -X POST http://localhost:8080/api/recommend \
  -H "Content-Type: application/json" \
  -d '{"message":"¿Qué vino sirve con mariscos?","topK":3}'
```

Validar que:

- La respuesta es JSON válido.
- Los campos requeridos están presentes.
- `wineId` corresponde a un vino real del catálogo.
- La explicación es comprensible.