# 11 - Filtros SQL (precio, stock, tipo, comida)

Combina búsqueda semántica con filtros estructurados (rango de precio, stock disponible, tipo, categoría de comida) para que la recuperación respete restricciones duras.

## Objetivo

Combinar búsqueda semántica con filtros estructurados (rango de precio, stock disponible, tipo, categoría de comida) para que la recuperación respete restricciones duras.

## Teoría

- **Filtros estructurados vs semánticos**: la semántica encuentra "parecidos", pero el precio y el stock son restricciones exactas.
- **Metadata filter en vector store**: Spring AI permite filtrar por metadatos durante `similaritySearch` (ej. `price < 20`, `stock == true`).
- **Estrategia híbrida**: aplicar filtros por metadata en el vector store o, alternativamente, filtrar en la BD relacional después de recuperar candidatos.
- **Extracción de criterios**: parsear la consulta del usuario para inferir filtros (precio máx, tipo, etc.) antes de recuperar.

## Alcance de implementación

- Agregar metadata útil a cada `Document` (`price`, `type`, `stockAvailable`, `foodPairings`).
- Definir un `WineSearchCriteria` (precio máx, tipo, stock, comida).
- Aplicar el filtro en `similaritySearch` o post-recuperación.
- Actualizar `POST /api/recommend` para aceptar criterios opcionales.

## `WineSearchCriteria`

```java
public record WineSearchCriteria(
    BigDecimal maxPrice,
    String type,
    Boolean stockAvailable,
    String foodCategory,
    Integer topK
) {}
```

## Endpoints

```bash
curl "http://localhost:8080/api/wines/search?q=pasta%20with%20tomato%20sauce&maxPrice=20&stockAvailable=true&topK=5"

curl -X POST http://localhost:8080/api/recommend \
  -H "Content-Type: application/json" \
  -d '{
    "message":"Recomendá un vino para pasta con salsa de tomate por menos de 20 dólares.",
    "maxPrice":20,
    "stockAvailable":true,
    "topK":5
  }'
```

## Dónde aplicar el filtro

| Estrategia | Dónde | Pros / Contras |
| --- | --- | --- |
| Metadata filter en vector store | Dentro de `similaritySearch` | Rápido; depende del soporte del store |
| Post-recuperación en BD relacional | Después de recuperar candidatos | Más seguro para reglas de negocio exactas |
| Híbrido | Filtros baratos en metadata, reglas duras en SQL | Equilibra rendimiento y rigor |

Para el aprendizaje, empezar con una opción explícita y simple; refactorizar después.

## Metadatos recomendados por `Document`

```text
wineId
type
price
stockAvailable
foodPairings
region
country
```

## Resultado esperado

"Recomendá un vino para pasta por menos de 20 dólares" devuelve solo vinos que cumplen precio y maridaje.

## Verificación manual

Mandar consultas con y sin restricciones, y comprobar que los resultados respetan los filtros:

- Precio: todos los vinos devueltos ≤ `maxPrice`.
- Stock: solo vinos con `stockAvailable == true` cuando se pide.
- Tipo: solo vinos del tipo solicitado.
- Comida: solo vinos cuyo maridaje incluye la categoría pedida.