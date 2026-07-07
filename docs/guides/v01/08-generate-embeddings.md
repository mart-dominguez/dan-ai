# 08 - Generate Embeddings

`WineEmbeddingService` uses Spring AI's `EmbeddingModel` to generate vectors from wine documents.

## Configuration

```properties
spring.ai.ollama.embedding.model=nomic-embed-text
```

The embedding model is separate from the chat model.

## Manual Check

Make sure the embedding model exists locally:

```bash
ollama pull nomic-embed-text
```

Then call:

```bash
curl http://localhost:8080/api/wines/1/embedding
```

The response includes the wine id, document text, and vector dimensions.
