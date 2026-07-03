# 5. Query Understanding: Extract Intent With the LLM

This file covers the **first** LLM call in the RAG flow: turning the user's
free-text question into structured filter criteria that
[04-semantic-search.md](04-semantic-search.md) can use.

## 5.1 Why we need this

In [04-semantic-search.md](04-semantic-search.md) the filter was hardcoded:

```java
.filterExpression("price <= 20.0")
```

That works for a demo, but a real user types things like:

- "cheap Malbec for the weekend"
- "something under 20 bucks that goes with steak"
- "a red I can age, at most 30 dollars"
- "whatever you have in stock for pasta"

Hardcoding a filter per endpoint cannot keep up. The natural solution is to
let the LLM **read the question** and emit a JSON object your code can turn
into a `filterExpression`.

This is the standard production pattern, sometimes called **query planning**
or **query understanding**.

## 5.2 The two-phase flow

```
User: "wine under $20 that goes well with meat"
            |
            v
  [Phase 1: LLM extracts intent]   <-- Ollama qwen3:14b, structured output
            |
            v
  SearchCriteria {
      semanticQuery: "wine that goes well with meat",
      maxPrice: 20.0,
      type: null
  }
            |
            +--> vectorStore.similaritySearch(semanticQuery)
            +--> filterExpression from maxPrice / type / ...
            |
            v
  [Phase 2: LLM generates answer]   <-- Ollama qwen3:14b, retrieved wines as context
```

Two LLM calls, with a deterministic search in between. Phase 1 is short and
structured (parse intent, not prose). Phase 2 writes the final recommendation.

## 5.3 The criteria record

```java
public record SearchCriteria(
    String  semanticQuery,   // "wine that goes well with meat"
    Double  maxPrice,        // 20.0
    Double  minPrice,        // null
    String  type,            // "Red" | null
    Integer minYear,         // null
    Boolean inStock          // true only if explicitly required
) {}
```

This is the `WineSearchCriteria` object mentioned in roadmap milestone 12.
The only change from a hand-built criteria is that we let the LLM populate it.

## 5.4 Phase 1: extraction call

```java
SearchCriteria criteria = chatClient.prompt()
    .system("""
        Extract wine search criteria from the user question.
        Return ONLY a JSON object.
        - semanticQuery: the tasting/food/feeling intent as a phrase.
          Never include price, year or stock in this field.
        - maxPrice, minPrice: numeric bounds in dollars, or null.
        - type: "Red", "White", "Rose", "Sparkling" or null.
        - minYear: integer or null.
        - inStock: true only if the user explicitly requires stock.
        """)
    .user(userQuery)
    .call()
    .entity(SearchCriteria.class);
```

Key points:

- Spring AI's `.entity(SearchCriteria.class)` maps the model output to the
  record (structured output — milestone 13).
- The system prompt forbids putting numeric constraints into `semanticQuery`,
  which keeps the vector query clean.
- The LLM normalizes messy user input ("under 20 bucks", "~$20", "less than
  twenty") into `20.0`.

## 5.5 Building the filter expression

```java
var filters = new ArrayList<String>();
if (criteria.maxPrice() != null)  filters.add("price <= " + criteria.maxPrice());
if (criteria.minPrice() != null)  filters.add("price >= " + criteria.minPrice());
if (criteria.type()   != null)    filters.add("type == '" + criteria.type() + "'");
if (criteria.minYear() != null)   filters.add("year >= " + criteria.minYear());
if (Boolean.TRUE.equals(criteria.inStock()))
                                  filters.add("stockAvailable > 0");

var requestBuilder = SearchRequest.builder()
    .query(criteria.semanticQuery())
    .topK(8)
    .similarityThreshold(0.60);

if (!filters.isEmpty())
    requestBuilder.filterExpression(String.join(" && ", filters));

List<Document> docs = vectorStore.similaritySearch(requestBuilder.build());
```

The vector query is always built from the cleaned `semanticQuery`; filters are
assembled from the structured criteria. They never enter the embedding.

## 5.6 Capturing inferred intent

The system prompt can include defaults so the LLM fills gaps:

| User says | Extracted as |
| --- | --- |
| "cheap" | `maxPrice: 15` (prompt-supplied default) |
| "premium" | `minPrice: 40` (prompt-supplied default) |
| "from last decade" | `minYear: 2015` |
| "whatever you have" | no price filter, `inStock: true` |
| "something red" | `type: "Red"` |
| "for the weekend, keep it cheap" | `maxPrice: 15`, `semanticQuery: "wine for the weekend"` |

Without extraction these inferences would have to live as brittle regexes in
Java. With extraction they are one sentence in the system prompt.

## 5.7 Cost and latency

Two LLM calls per request. On a local Ollama, the extraction call is typically
1–2 seconds of added latency because the input and output are short. If that
becomes a problem:

- Run extraction with a **smaller model** (`qwen3:1.7b`) — it is a simple
  parse task, not creative generation.
- Add a **fast path**: a quick regex checks whether the query contains words
  like "under", "cheaper", "stock"; if not, skip extraction and search with
  no filters.
- Cache extraction results for identical repeated queries.

## 5.8 Failure modes and mitigations

| Failure | Mitigation |
| --- | --- |
| LLM puts "under 20" into `semanticQuery` despite instructions | Stronger system prompt; reject/fix at the `WineDocumentMapper` stage; add a sanity regex on the field. |
| LLM invents `type: "Orange"` (not in the catalog) | Constrain the prompt to a fixed enum; reject unknown values and retry without the filter. |
| LLM omits an obvious price cap | Default `maxPrice` to `null` and rely on the user to complain; log the raw output for review. |
| Extraction returns malformed JSON | Use `.entity(...)` (Spring AI retries/parses); fall back to no-filter search on parse failure. |

## 5.9 Where this fits in the roadmap

- Milestone 12 asks for a `WineSearchCriteria` with optional filters. You are
  fulfilling that requirement by populating the criteria from an LLM call
  instead of from manual parsing — same shape, more flexible source.
- Milestone 13 (structured output) is exactly the `.entity(SearchCriteria.class)`
  call above.

Next: [06-end-to-end-example.md](06-end-to-end-example.md) — phases 1 and 2
wired together with a real controller.