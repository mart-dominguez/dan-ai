# 6. End-to-End Example

This file wires together everything from the previous files into a single
working walkthrough for the query:

> "wine under $20 that goes well with meat"

Prerequisites: read [01-concepts.md](01-concepts.md) through
[05-query-understanding.md](05-query-understanding.md) first.

## 6.1 What the user sees

```bash
curl "http://localhost:8080/api/wines/search?q=wine%20under%20%2420%20that%20goes%20well%20with%20meat"
```

```json
{
  "wineId": 1,
  "name": "Malbec Reserva",
  "price": 18.50,
  "reason": "A medium-body Malbec whose food pairings include grilled meat, ..."
}
```

## 6.2 What happens internally

```
1. Controller receives ?q=...
2. WineSearchService.search(q)
   2a. Phase 1 LLM call  -> SearchCriteria { semanticQuery, maxPrice=20, type=null }
   2b. Build SearchRequest with query=semanticQuery, filter="price <= 20.0"
   2c. vectorStore.similaritySearch(...) (no LLM)
   2d. Map docs -> wineIds -> wineRepository.findAllById(...)
3. Controller calls ChatClient with retrieved wines as context
4. Phase 2 LLM call writes the structured recommendation
5. Return JSON to the user
```

Two LLM calls, one vector search, one JPA read.

## 6.3 Service layer

```java
@Service
public class WineSearchService {

    private final ChatClient chatClient;
    private final VectorStore vectorStore;
    private final WineRepository wineRepository;

    public List<Wine> search(String userQuery) {
        // ---- Phase 1: LLM extracts structured intent ----
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

        // ---- Build the vector query ----
        var requestBuilder = SearchRequest.builder()
            .query(criteria.semanticQuery())
            .topK(8)
            .similarityThreshold(0.60);

        // ---- Build the metadata filter expression ----
        var filters = new ArrayList<String>();
        if (criteria.maxPrice() != null)
            filters.add("price <= " + criteria.maxPrice());
        if (criteria.minPrice() != null)
            filters.add("price >= " + criteria.minPrice());
        if (criteria.type() != null)
            filters.add("type == '" + criteria.type() + "'");
        if (criteria.minYear() != null)
            filters.add("year >= " + criteria.minYear());
        if (Boolean.TRUE.equals(criteria.inStock()))
            filters.add("stockAvailable > 0");
        if (!filters.isEmpty())
            requestBuilder.filterExpression(String.join(" && ", filters));

        // ---- Phase 2 (retrieve): no LLM involved ----
        List<Document> docs = vectorStore.similaritySearch(requestBuilder.build());

        List<Long> wineIds = docs.stream()
            .map(d -> (Long) d.getMetadata().get("wineId"))
            .toList();

        return wineRepository.findAllById(wineIds);
    }
}
```

See [05-query-understanding.md](05-query-understanding.md) for details on the
extraction call and [04-semantic-search.md](04-semantic-search.md) for the
filter building.

## 6.4 Controller

```java
@RestController
@RequestMapping("/api/wines")
public class WineController {

    private final WineSearchService searchService;
    private final ChatClient chatClient;

    @GetMapping("/search")
    public RecommendationResponse search(@RequestParam String q) {
        List<Wine> wines = searchService.search(q);          // retrieve

        return chatClient.prompt()                           // generate
            .system("""
                You are a sommelier. Recommend ONLY wines from the context.
                Never invent wines not in the list. Include name, price
                and the reason it fits the request.
                """)
            .user(u -> u.text("""
                User question: {question}
                Available wines:
                {wines}
                """)
                .param("question", q)
                .param("wines", formatWines(wines)))
            .call()
            .entity(RecommendationResponse.class);
    }

    private String formatWines(List<Wine> wines) {
        return wines.stream().map(w -> """
            - %s (%s, %s). $%s. Pairs with: %s. %s
            """.formatted(w.getName(), w.getType(), w.getRegion(),
                          w.getPrice(), w.getFoodPairings(), w.getDescription()))
            .collect(joining("\n"));
    }
}
```

The `.entity(RecommendationResponse.class)` part is structured output —
milestone 13 of [../learning-roadmap.md](../learning-roadmap.md).

## 6.5 Tracing the example with real catalog rows

Two candidate wines from
[02-wine-catalog-record.md](02-wine-catalog-record.md):

| id | name | price | food_pairings | affects |
| --- | --- | --- | --- | --- |
| 1 | Malbec Reserva | 18.50 | grilled meat, empanadas, hard cheese | passes both |
| 2 | Malbec Gran Lines | 45.00 | lamb, ribeye, aged cheese | fails price filter |

Phase-by-phase outcome:

1. **Phase 1 (LLM)** parses `"wine under $20 that goes well with meat"` into
   `SearchCriteria { semanticQuery="wine that goes well with meat",
   maxPrice=20.0, type=null }`.
2. **Retrieve** embeds the semantic query, searches the vector store. Both
   wines score well on meaning (both mention meat).
3. **Filter** `price <= 20.0` discards wine 2. Remaining: `[1]`.
4. **JPA** reads the full row for id 1 from PostgreSQL.
5. **Phase 2 (LLM)** receives wine 1 as context and returns the structured
   recommendation shown in §6.1.

## 6.6 What could go wrong, briefly

| Symptom | Likely cause | Where to look |
| --- | --- | --- |
| Answer names a wine not in the catalog | Phase 2 system prompt too loose | §6.4 system prompt |
| Wrong wines return (e.g. a $45 wine) | Filter not applied, or extracted `maxPrice` is null | [05-query-understanding.md §5.8](05-query-understanding.md) |
| No wines at all | `similarityThreshold` too high, or `topK` too low and filter removed all | [04-semantic-search.md §4.6](04-semantic-search.md) |
| Slow responses | Two large LLM calls; extraction model too big | [05-query-understanding.md §5.7](05-query-understanding.md) |
| Bad filter syntax exception | Generated expression not supported by the backend | [04-semantic-search.md §4.7](04-semantic-search.md) — fall back to SQL |

## 6.7 Closing the loop

This example is the smallest complete RAG flow:

- Embeddings describe wine meaning ([03-vector-store-record.md](03-vector-store-record.md)).
- A vector DB searches by meaning ([04-semantic-search.md](04-semantic-search.md)).
- Metadata filters apply hard business rules without re-embedding.
- An LLM parses the user's intent into filters
  ([05-query-understanding.md](05-query-understanding.md)).
- A second LLM writes the grounded recommendation.

Everything in the guide reduces to one rule:

> **Embed descriptive text. Filter on structured fields. Ground the LLM in
> retrieved rows.**

Back to: [README.md](README.md).