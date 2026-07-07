# 09 - Búsqueda semántica sobre vinos

Dada una consulta del usuario, recupera los vinos más parecidos por significado.

## Objetivo

Dada una consulta del usuario, recuperar los vinos más parecidos por significado.

## Teoría

- **Similaridad semántica**: la consulta del usuario se embebe y se comparan sus vectores contra los del store.
- **`VectorStore.similaritySearch(...)`**: método de Spring AI que devuelve los `Document` más cercanos.
- **Top-k y umbral (threshold)**: controlar cuántos resultados y qué tan parecidos deben ser.
- **Volver a la fuente de verdad**: con los `wineId` de metadata, ir al `WineRepository` y reconstruir los vinos completos.

## Alcance de implementación

- `SemanticSearchService` que reciba un texto, haga `similaritySearch` y devuelva los `Wine` completos.
- Endpoint `GET /api/wines/search?q=...` que devuelva los vinos similares en JSON.

## Endpoint

Primero indexar (si no se hizo en el Hito 8):

```bash
curl -X POST http://localhost:8080/api/wines/index
```

Luego buscar:

```bash
curl "http://localhost:8080/api/wines/search?q=tinto%20para%20carne%20asada&topK=5"
```

## Flujo

```text
query (texto)
  -> EmbeddingModel.embed(query)        (vector de la consulta)
  -> VectorStore.similaritySearch(topK) (Documents más cercanos)
  -> leer wineId de metadata
  -> WineRepository.findAllById(ids)    (Vinos completos)
  -> devolver JSON
```

## Parámetros

| Parámetro | Default | Notas |
| --- | --- | --- |
| `q` | obligatorio | Texto libre del usuario |
| `topK` | 3 o 5 | Cuántos resultados devolver |
| `threshold` | opcional | Umbral de similaridad (ej. 0.7) |

## Resultado esperado

Buscar "tinto para carne asada" devuelve vinos tintos con maridaje a carnes, ordenados por relevancia, **aunque las palabras exactas no coincidan**.

## Verificación manual

```bash
curl "http://localhost:8080/api/wines/search?q=tinto%20para%20carne%20asada"
```

Revisar que los resultados tengan sentido: tintos con maridaje a carnes asadas o parrilla.