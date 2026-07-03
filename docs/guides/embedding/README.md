# Embedding, Vector Search, and RAG Guide

This guide explains how embeddings, a vector database, and Retrieval-Augmented
Generation (RAG) work together in the `dan-ia` wine recommendation project.

It is split into focused documents so you can read them in order or jump to a
specific topic. Each file links to the others when a concept depends on them.

## Reading order

| # | File | Topic |
| --- | --- | --- |
| 1 | [01-concepts.md](01-concepts.md) | What embeddings, vector DBs, and RAG are, and who does what. |
| 2 | [02-wine-catalog-record.md](02-wine-catalog-record.md) | The PostgreSQL source of truth: schema and sample row. |
| 3 | [03-vector-store-record.md](03-vector-store-record.md) | What gets stored in the vector DB: content, embedding, metadata. |
| 4 | [04-semantic-search.md](04-semantic-search.md) | Running a semantic search with hard metadata filters. |
| 5 | [05-query-understanding.md](05-query-understanding.md) | Using the LLM to extract structured intent from free text. |
| 6 | [06-end-to-end-example.md](06-end-to-end-example.md) | Full walkthrough: "wine under $20 that goes well with meat". |

## Prerequisites

Before this guide, you should be comfortable with the chat flow described in
[../01-chat-client.md](../01-chat-client.md), in particular the roles of
`ChatClient`, the Ollama chat model, and the system prompt.

## Scope

This guide covers the conceptual and data-model layer that supports milestones
7–12 of [../learning-roadmap.md](../learning-roadmap.md):

- Embeddings and wine document representation.
- Storing embeddings in a vector store.
- Semantic search over wines.
- Combining semantic search with structured filters (price, stock, type).
- Extracting filter intent from a user prompt via the LLM.