# 1. Concepts: Embeddings, Vector DBs, and RAG

This file explains the three building blocks of the wine recommendation
assistant and, most importantly, **who is responsible for what** at runtime.

It is the conceptual foundation for the rest of this guide. If you only read
one file, read this one.

## 1.1 The problem we are solving

A large language model (LLM) can generate text, but it does **not** know your
wine catalog. If you ask *"which Malbec pairs with grilled meat?"* without
giving it any context, it will answer from its training data — which may invent
wines, wrong prices, or nonexistent vintages.

The assistant needs a way to:

1. Search the real catalog by **meaning** (not just exact keywords).
2. **Ground** the LLM answer in catalog records that actually exist.

That is what embeddings, a vector database, and RAG provide.

## 1.2 Embeddings

An **embedding** is a vector of numbers (~700–1500 floats) that represents the
*meaning* of a piece of text. Similar texts produce vectors that are close in
high-dimensional space.

Key points:

- Embeddings are produced by an **embedding model**, which is a *different*
  model from the chat model. In this project the chat model is `qwen3:14b`
  (Ollama); the embedding model will typically be `nomic-embed-text` (Ollama).
- Spring AI exposes embeddings through the `EmbeddingModel` interface.
- Producing an embedding is **not** an LLM call — the embedding model does not
  generate text, it only converts text to a vector.
- Embeddings are good at **meaning** and bad at **numbers and ranges**.
  A query like *"under 15 dollars"* is a hard numeric constraint, not a
  semantic one. That distinction drives the whole guide (see
  [04-semantic-search.md](04-semantic-search.md)).

## 1.3 Vector database

A **vector database** stores documents together with their embeddings and
supports **similarity search**: given a query vector, return the stored
documents whose vectors are closest to it (usually by cosine similarity).

In this project the candidate stores are:

- **pgvector** (a PostgreSQL extension) — most natural, since the relational
  source of truth is already PostgreSQL. Embeddings live in a separate
  column you do not touch from business code.
- An in-memory store for quick learning experiments.

Spring AI abstracts this behind the `VectorStore` interface, so switching
backends later does not change business code.

Each document in the vector store has three parts:

| Part | Holds | Used for |
| --- | --- | --- |
| `content` | The natural-language wine description | Embedded once; the text that carries meaning |
| `embedding` | The vector from the embedding model | Similarity math |
| `metadata` | Structured fields: `wineId`, `price`, `type`, `stock`, `year` | Hard filtering without re-embedding |

See [03-vector-store-record.md](03-vector-store-record.md) for the concrete
shape of one document.

## 1.4 RAG (Retrieval-Augmented Generation)

RAG combines the two pieces above with the chat model. It has **two phases**:

```
User question
    |
    v
[Retrieve]  --> vectorStore.similaritySearch(...)   (no LLM)
    |              uses EmbeddingModel for the query vector
    v
retrieved wines (real PostgreSQL rows)
    |
    v
[Generate]  --> ChatClient.prompt()...              (LLM, qwen3:14b)
    |              wines are passed as context
    v
final answer to the user
```

The LLM only participates in the **generate** step. During retrieval it is
not involved — the vector database does the similarity math, and Spring AI
orchestrates the calls.

## 1.5 Who does what — quick reference

| Step | Component | LLM involved? |
| --- | --- | --- |
| Create embeddings for wine documents | `EmbeddingModel` → Ollama `nomic-embed-text` | No (different model, not the chat LLM) |
| Create embedding for the user query | `EmbeddingModel` → Ollama `nomic-embed-text` | No |
| Similarity search (find nearest wines) | `VectorStore` → vector DB | No |
| Apply hard filters (price, stock) | Vector DB metadata filter or JPA `WHERE` | No |
| Read full wine records | `WineRepository` (Spring Data JPA) | No |
| Generate the final answer | `ChatClient` → Ollama `qwen3:14b` | Yes |

Notice the LLM only writes the final answer. Everything before that is
deterministic code + the embedding model. That keeps retrieval cheap,
repeatable, and easy to test.

## 1.6 Why split "meaning" and "structured filters"

A field should be **embedded** if the model "talks about" it (taste notes,
food pairings, body, description). A field should stay as **metadata / SQL**
if you would write a `WHERE` for it (price < 15, stock > 0, type = 'Red',
year >= 2020).

Benefits:

- Embeddings stay stable across business changes. A price drop does **not**
  invalidate the vector.
- Hard numeric and enum constraints are **exact**, not fuzzy by meaning.
- Tests are deterministic: a `WHERE price <= 20` either passes or not.

This split is the core of [04-semantic-search.md](04-semantic-search.md) and
underlies the two-LLM-call pattern in
[05-query-understanding.md](05-query-understanding.md).

## 1.7 Where this fits in the roadmap

These concepts map directly to milestones 7–12 of
[../learning-roadmap.md](../learning-roadmap.md):

| Milestone | Concept covered here |
| --- | --- |
| 7 – Embeddings concept | §1.2, §1.3 |
| 8 – Generate embeddings | §1.2 (embedding model) |
| 9 – Store embeddings | §1.3, [03-vector-store-record.md](03-vector-store-record.md) |
| 10 – Semantic search | [04-semantic-search.md](04-semantic-search.md) |
| 11 – RAG recommendation | §1.4, [06-end-to-end-example.md](06-end-to-end-example.md) |
| 12 – SQL/metadata filters | [04-semantic-search.md](04-semantic-search.md), [05-query-understanding.md](05-query-understanding.md) |

Next: [02-wine-catalog-record.md](02-wine-catalog-record.md) — the relational
source of truth.