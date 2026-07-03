# 3. Vector Store Record

This file shows how a `wine` row becomes a document in the vector store: what
gets embedded, what stays as metadata, and how to keep both sides in sync.

Prerequisite: [02-wine-catalog-record.md](02-wine-catalog-record.md).

## 3.1 The `WineDocumentMapper` idea

A vector store entry is built from three ingredients:

1. **`content`** — a natural-language description of the wine. This is what
   gets embedded. Only fields the model "talks about" should appear here.
2. **`embedding`** — the vector produced by the embedding model from
   `content`.
3. **`metadata`** — a key/value map of structured fields used for hard
   filtering. These are **not** embedded.

The role of a `WineDocumentMapper` (milestone 7) is to convert a `Wine`
entity into that triple. It is a pure, stateless transformation.

## 3.2 Choosing which fields go where

| Field | In `content`? | In `metadata`? | Rationale |
| --- | :---: | :---: | --- |
| `id` | no | yes (`wineId`) | Only needed to join back to PostgreSQL. |
| `name`, `description`, `taste_notes`, `food_pairings`, `body`, `acidity`, `sweetness` | yes | no | These carry meaning the user searches for. |
| `grape_variety`, `region`, `country` | yes | yes (optionally) | Useful both as meaning and as exact filters. |
| `type` | yes | yes | Same. |
| `price` | no | yes | Numeric range — must be exact, not fuzzy. |
| `stock_available` | no | yes | Boolean-ish filter (`> 0`). |
| `year` | no | yes | Numeric range. |

This split is the single most important design decision in the guide. It is
explained in [01-concepts.md §1.6](01-concepts.md).

## 3.3 Example: Malbec Reserva

Given the PostgreSQL row from
[02-wine-catalog-record.md §2.3](02-wine-catalog-record.md), the
`WineDocumentMapper` produces:

**Content (what gets embedded):**

```text
Name: Malbec Reserva. Type: Red wine. Grape variety: Malbec.
Region: Mendoza, Argentina. Taste notes: plum, blackberry, soft tannins, vanilla.
Body: medium. Acidity: medium. Sweetness: dry.
Food pairings: grilled meat, empanadas, hard cheese.
Description: Smooth red wine for casual dinners, aged 12 months in French oak.
```

Notice `price`, `year`, and `stock` are **not** in the content. They live
only in metadata.

**Embedding** (from `nomic-embed-text`):

```text
[0.0231, -0.1184, 0.4421, ..., -0.0733]   // ~768 floats
```

The exact numbers are not meaningful to a human; only their relative
distances to other vectors matter.

**Metadata:**

```json
{
  "wineId": 1,
  "type": "Red",
  "price": 18.50,
  "stockAvailable": 120,
  "year": 2021
}
```

**Full document stored in the vector DB** (Spring AI `Document` shape):

```json
{
  "id": "doc-wine-1",
  "content": "Name: Malbec Reserva. Type: Red wine. ...",
  "embedding": [0.0231, -0.1184, 0.4421, "..."],
  "metadata": {
    "wineId": 1,
    "type": "Red",
    "price": 18.50,
    "stockAvailable": 120,
    "year": 2021
  }
}
```

## 3.4 Keeping vector store and PostgreSQL in sync

| Change in PostgreSQL | Vector store action | Re-embed? |
| --- | --- | :---: |
| `price` changes | update `metadata.price` only | no |
| `stock_available` changes | update `metadata.stockAvailable` only | no |
| `description` / `taste_notes` change | rebuild `content`, re-embed, replace document | yes |
| New wine inserted | build document, embed, add to vector store | yes (once) |
| Wine deleted | delete document by `wineId` | no |

Example of a non-embedding update (price drop):

```java
var doc = vectorStore.get("doc-wine-1");          // or fetch by metadata wineId
doc.getMetadata().put("price", 16.00);
vectorStore.update(List.of(doc));                  // no EmbeddingModel call
```

Example of an embedding-required update (description rewrite):

```java
String newContent = wineDocumentMapper.toContent(updatedWine);
float[] newEmbedding = embeddingModel.embed(newContent);
var updated = new Document(newContent, Map.of("wineId", id, ...), newEmbedding);
vectorStore.update(List.of(updated));             // EmbeddingModel IS called
```

## 3.5 Why metadata matters for correctness

Imagine `price` were embedded inside `content` instead of stored as metadata:

- Query *"under 15 dollars"* would compare meaning, not arithmetic. A $14 wine
  and a $16 wine might have very different embeddings because surrounding
  text differs, producing unpredictable matches.
- Every price change would force a re-embedding.

Keeping `price` as metadata makes the business rule **deterministic**
(`metadata.price <= 20` either passes or not) and keeps the embedding stable
when business fields change. This is what makes the search in
[04-semantic-search.md](04-semantic-search.md) reliable.

## 3.6 pgvector note

If you use **pgvector**, the embedding lives in the same database as the row,
typically in a separate column:

```sql
CREATE TABLE wine (
    -- ... business columns from 02-wine-catalog-record.md ...
    embedding vector(768)   -- pgvector column, managed by indexing code only
);
```

Conceptually nothing changes: business code still reads and writes the
business columns; the embedding column is treated exactly like a vector store
entry. The "two stores" become one physical DB but two logical concerns.

Next: [04-semantic-search.md](04-semantic-search.md) — querying the vector
store with meaning + filters.