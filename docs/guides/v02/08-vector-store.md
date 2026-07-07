# 08 - Almacenamiento de embeddings en un vector store

Persiste los embeddings de los vinos en un vector store para poder buscar por similitud.

## Objetivo

Persistir los embeddings de los vinos en un vector store para poder buscar por similitud.

## Teoría

- **Vector store**: almacena documentos junto con su embedding y metadatos. Permite búsqueda por similitud (coseno, euclidiana).
- **`VectorStore` de Spring AI**: interfaz unificada; implementaciones (SimpleInMemory, PgVector, Chroma, etc.).
- **`Document`**: objeto de Spring AI con `id`, `content`, `metadata` y `embedding`.
- **Metadatos**: guardamos el `wineId` como metadata para luego volver a la BD relacional y traer los datos completos.
- **Indexación**: proceso de generar embeddings para todos los vinos y guardarlos en el vector store.

## Alcance de implementación

- Elegir e implementar un vector store (empezar con uno simple y migrar si hace falta).
- Servicio `WineEmbeddingService` que recorra los vinos y guarde un `Document` por cada uno.
- Endpoint o `CommandLineRunner` que dispare la indexación.

## Endpoints

```bash
curl -X POST http://localhost:8080/api/wines/index
```

Dispara la generación de embeddings para todos los vinos del catálogo y los guarda en el vector store.

## `Document` de Spring AI

Cada `Document` contiene:

- `id`: identificador único (puede derivarse del `wineId`).
- `content`: el texto del documento del vino (ver Hito 7).
- `metadata`: pares clave/valor que enlazan de vuelta a la BD relacional.
- `embedding`: el vector generado por `EmbeddingModel`.

## Metadatos útiles

```text
wineId
type
price
stockAvailable
foodPairings
region
country
```

> **Regla**: el vector store guarda el documento generado y metadatos, pero la BD relacional sigue siendo la fuente de verdad. El vector store siempre debe poder reconstruirse desde la BD.

## Inline vs externo

Para aprendizaje se puede empezar con un vector store en memoria (`SimpleVectorStore`). En producción se migraría a PostgreSQL + pgvector u otra base vectorial dedicada.

## Resultado esperado

El vector store contiene un documento por vino, con su embedding y `wineId` en metadata.

## Verificación manual

Consultar el tamaño del vector store y buscar por similitud un texto de prueba ("tinto suave para carne") mostrando los ids recuperados.

```bash
curl -X POST http://localhost:8080/api/wines/index
```