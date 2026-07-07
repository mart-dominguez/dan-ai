# 06 - Basic Recommendation Without RAG

`POST /api/recommend/basic` demonstrates the simple approach: read all wines, place them in the prompt, and ask the LLM to recommend.

## Endpoint

```bash
curl -X POST http://localhost:8080/api/recommend/basic \
  -H "Content-Type: application/json" \
  -d '{"message":"What wine should I serve with grilled meat?"}'
```

## Theory

This works for a tiny catalog but does not scale. The prompt grows with every wine and can exceed the model context window.

This milestone exists to make the need for RAG obvious.
