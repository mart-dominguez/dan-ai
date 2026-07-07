# 10 - Semantic Search

`SemanticWineSearchService` searches the vector index with a natural language query.

## Endpoint

```bash
curl "http://localhost:8080/api/wines/search?q=soft%20red%20wine%20for%20a%20beginner"
```

Run indexing first:

```bash
curl -X POST http://localhost:8080/api/wines/index
```

## Theory

The user query is embedded and compared with each wine embedding by cosine similarity.

The result includes the real wine data loaded from the relational database, plus a similarity score.
