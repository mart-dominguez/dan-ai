# 4. Semantic Search With Filters

This file shows how to run a search that combines **meaning** (vector
similarity) with **hard filters** (metadata). It is the retrieve phase of
RAG.

Prerequisites: [01-concepts.md](01-concepts.md),
[03-vector-store-record.md](03-vector-store-record.md).

## 4.1 Two concerns in one user query

Real wine requests mix soft intent and hard rules:

> "Which wine pairs well with seafood under 20 dollars?"

- *Soft intent*: "pairs well with seafood" → semantic search.
- *Hard rule*: "under 20 dollars" → metadata filter `price <= 20`.

The vector store handles the first; metadata filters (or a post-filter in
PostgreSQL) handle the second. They are **independent** operations.

## 4.2 The Spring AI `SearchRequest`

```java
var request = SearchRequest.builder()
    .query("wine that pairs well with seafood")   // meaning
    .topK(5)
    .similarityThreshold(0.60)
    .filterExpression("price <= 20.0")             // hard rule
    .build();

List<Document> docs = vectorStore.similaritySearch(request);
```

What happens at runtime, step by step:

1. Spring AI calls `EmbeddingModel.embed(query)` → query vector.
2. The vector DB computes cosine similarity against every wine-document
   vector.
3. Documents whose `metadata.price > 20` are discarded.
4. The remaining documents are sorted by similarity and the top 5 are
   returned.
5. The LLM **is not involved** in any of the above.

## 4.3 Mapping results back to PostgreSQL rows

The vector store returns `Document`s, not `Wine`s. The full, exact business
record must come from PostgreSQL via `wineId`:

```java
List<Long> wineIds = docs.stream()
    .map(d -> (Long) d.getMetadata().get("wineId"))
    .toList();

List<Wine> wines = wineRepository.findAllById(wineIds);
```

This matters because:

- The embedded `content` is a *summary*; the user wants real fields (full
  name, exact price, region spelling) from PostgreSQL.
- The LLM's final answer should be grounded in the source of truth, not in a
  text approximation that might lag behind the DB.

## 4.4 Filter expression syntax

Spring AI's `filterExpression` uses a small DSL over metadata. Common forms:

| Filter | Expression |
| --- | --- |
| Price at most 20 | `price <= 20.0` |
| Price between 10 and 30 | `price >= 10.0 && price <= 30.0` |
| Red only | `type == 'Red'` |
| In stock | `stockAvailable > 0` |
| From 2020 onward | `year >= 2020` |
| Red under 15 in stock | `type == 'Red' && price <= 15.0 && stockAvailable > 0` |

If your backing store does not support these expressions, the alternative is a
plain `WHERE` in a JPA query against `wineRepository`, post-retrieval. That is
slightly less efficient but always available.

## 4.5 Worked example: "wine under $20 that goes well with meat"

Catalog state (two candidate wines; see
[02-wine-catalog-record.md](02-wine-catalog-record.md)):

| id | name | price | food_pairings |
| --- | --- | --- | --- |
| 1 | Malbec Reserva | 18.50 | grilled meat, empanadas, hard cheese |
| 2 | Malbec Gran Lines | 45.00 | lamb, ribeye, aged cheese |

Both have "meat" in their meaning, so both score well on semantic similarity.
The filter `price <= 20.0` discards #2. The final retrieved set is `[wine 1]`.

This is exactly the case where a pure embedding-based search would be wrong:
a $45 wine can be "closer" to the query text than a $18 wine, and the user
would get a recommendation they cannot afford.

## 4.6 Tuning knobs

| Knob | Effect | Typical value |
| --- | --- | --- |
| `topK` | How many candidates to return | 5–8 |
| `similarityThreshold` | Minimum cosine similarity to keep | 0.55–0.70 |
| Filter narrowness | How many candidates survive the hard rules | depends on query |

If `topK` is too low you may filter away all matches after the hard rules; if
it is too high you pay more re-read cost. A common pattern is to set a higher
`topK` for the vector query and let filters narrow it down.

## 4.7 When filters should live in SQL instead

Prefer SQL (via `WineRepository`) when:

- The filter expression DSL cannot express the condition (e.g. `region ILIKE`).
- You want to join to another table (e.g. supplier name).
- You need exact counts or pagination.

You then do similarity search first, fetch `wineId`s, and run a final JPA
query with the extra predicates. This costs one extra DB round trip but
keeps business logic where it belongs.

## 4.8 What this does NOT do

This file covers **retrieval only**. There is no LLM call that touches the
user's question. That changes in two places:

- [05-query-understanding.md](05-query-understanding.md): an LLM call parses
  the free-text query *into* the `filterExpression` used here.
- [06-end-to-end-example.md](06-end-to-end-example.md): a second LLM call
  writes the final answer using the retrieved wines.

Next: [05-query-understanding.md](05-query-understanding.md) — extracting the
filter criteria from the user's own words.