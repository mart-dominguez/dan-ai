# 09 - Vector Store

The first implementation uses `WineVectorStore`, an in-memory educational vector index.

## Endpoint

```bash
curl -X POST http://localhost:8080/api/wines/index
```

## Theory

The vector store keeps:

- the generated wine document text
- the embedding vector
- metadata such as `wineId`, type, price, region, and stock

The metadata connects vector search results back to relational records.

For production, this in-memory store can later be replaced with PostgreSQL + pgvector or another vector database.
