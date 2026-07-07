# 11 - RAG Recommendation

`POST /api/recommend` uses semantic search before calling the LLM.

## Endpoint

```bash
curl -X POST http://localhost:8080/api/recommend \
  -H "Content-Type: application/json" \
  -d '{"message":"Which wine from my catalog pairs well with seafood?","topK":3}'
```

## Theory

RAG has two steps:

1. Retrieve relevant wines from the vector store.
2. Generate an answer using only those wines as prompt context.

This keeps the answer grounded in the catalog.
