# 01 - Overview

The project is an AI wine recommendation assistant built with Spring Boot, Spring AI, Ollama, embeddings, vector search, and RAG.

The source of truth is the relational wine catalog. AI features are added around that catalog, not instead of it.

## Architecture

```text
HTTP API
  -> wines table through JPA
  -> ChatClient for Ollama chat
  -> EmbeddingModel for Ollama embeddings
  -> in-memory vector index for semantic wine search
```

The vector index stores searchable wine documents and points back to the real wine rows through `wineId`.

## Key Idea

RAG means retrieve first, then generate. The assistant should recommend wines from retrieved catalog context instead of inventing wines from model memory.

## Manual Check

Read `docs/guides/v01/learning-roadmap.md` and confirm each milestone adds one idea.
