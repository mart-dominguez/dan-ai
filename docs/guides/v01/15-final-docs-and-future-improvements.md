# 15 - Final Docs And Future Improvements

The project now has a first learning implementation of the complete flow:

1. Catalog data in a relational database.
2. REST endpoints for wines.
3. Ollama chat through Spring AI.
4. Wine document generation.
5. Embeddings through Spring AI.
6. In-memory vector search.
7. RAG recommendation endpoint.
8. Structured JSON response.

## Future Improvements

- Replace the in-memory vector index with PostgreSQL + pgvector.
- Reindex automatically when wines change.
- Add real structured output parsing from the LLM.
- Add reranking after vector retrieval.
- Add streaming recommendations.
- Add evaluation tests for recommendation quality.
- Add admin endpoints for catalog management.

## Manual End-To-End Check

```bash
ollama pull qwen3:14b
ollama pull nomic-embed-text
./mvnw spring-boot:run
curl -X POST http://localhost:8080/api/wines/index
curl -X POST http://localhost:8080/api/recommend \
  -H "Content-Type: application/json" \
  -d '{"message":"Recommend a wine for pasta with tomato sauce under 20 dollars.","maxPrice":20,"stockAvailable":true}'
```
