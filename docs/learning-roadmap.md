# Learning Roadmap - AI Wine Recommendation Assistant

This roadmap is for learning Spring Boot, Spring AI, Ollama, embeddings, vector search, and RAG step by step.

The goal is not to build everything at once. Each milestone adds one small idea, one small piece of implementation, and one Markdown document that explains what was learned.

No application code should be implemented from this roadmap step. This file only defines the learning plan and documentation convention.

---

## Project Idea

The final system will be an AI assistant that recommends wines from a catalog.

Users should be able to ask natural language questions such as:

- "What wine should I serve with grilled meat?"
- "Recommend a wine for pasta with tomato sauce under 20 dollars."
- "I want a soft red wine for someone who does not usually drink wine."
- "Which wine from my catalog pairs well with seafood?"

The catalog database will contain wines with fields such as:

- `name`
- `type`
- `grapeVariety`
- `region`
- `country`
- `year`
- `price`
- `tasteNotes`
- `body`
- `acidity`
- `sweetness`
- `foodPairings`
- `description`
- `label`
- `stockAvailable`

The final assistant should use:

- A relational database as the source of truth.
- Embeddings to represent wines semantically.
- A vector store for similarity search.
- Ollama as the local LLM provider.
- Spring AI as the integration layer.
- RAG to answer using wines from the catalog.

---

## Target Architecture

```text
User
  |
  v
Spring Boot REST API
  |
  +--> Relational Database
  |      Source of truth for wine records
  |
  +--> Spring AI ChatClient
  |      Sends prompts to Ollama
  |
  +--> Spring AI EmbeddingModel
  |      Creates vectors from wine descriptions and user questions
  |
  +--> Vector Store
         Stores wine documents and supports semantic search

RAG flow:
1. User asks a question.
2. The application searches the vector store for relevant wines.
3. The application loads the real wine records from the relational database.
4. The application sends the selected wines as context to the LLM.
5. The LLM answers using only the retrieved catalog information.
```

---

## Documentation Naming Convention

Each milestone should generate one Markdown file under `docs/`.

Use a numeric prefix, English file names, and kebab-case:

| Milestone | File | Topic |
| --- | --- | --- |
| 1 | `01-overview.md` | Project overview and architecture |
| 2 | `02-wine-catalog-model.md` | Wine catalog model |
| 3 | `03-basic-rest-endpoint.md` | Basic REST endpoint |
| 4 | `04-chat-client-ollama.md` | Ollama chatbot with `ChatClient` |
| 5 | `05-system-prompts.md` | System prompts |
| 6 | `06-basic-recommendation-without-rag.md` | Recommendation without RAG |
| 7 | `07-embeddings-concept.md` | Embeddings and wine document representation |
| 8 | `08-generate-embeddings.md` | Generate embeddings for wine records |
| 9 | `09-vector-store.md` | Store embeddings in a vector store |
| 10 | `10-semantic-search.md` | Semantic search over wines |
| 11 | `11-rag-recommendation.md` | Basic RAG recommendation endpoint |
| 12 | `12-sql-filters.md` | SQL filters and structured constraints |
| 13 | `13-structured-output.md` | Structured recommendation output |
| 14 | `14-tests.md` | Tests |
| 15 | `15-final-docs-and-future-improvements.md` | Final documentation and future improvements |

Rule: after completing a milestone, create or update its Markdown file with the theory, implementation notes, testing instructions, and lessons learned.

---

## Milestone 1 - Basic Project Overview and Architecture

### 1. Goal

Understand what the assistant will do and how the main pieces fit together before writing code.

### 2. Theory

- A large language model can generate text, but it does not automatically know the local wine catalog.
- Ollama runs LLMs locally and exposes them through an HTTP API.
- Spring AI gives Spring-friendly abstractions for chat, embeddings, and vector stores.
- A relational database is the source of truth for wine data.
- Embeddings are numeric representations of text meaning.
- A vector store searches texts by meaning instead of exact words.
- RAG means retrieving relevant data first, then giving that data to the LLM as context.

### 3. Implementation Scope

- Create this roadmap.
- Define the documentation naming convention.
- Draw the initial architecture.
- Do not implement application code yet.

### 4. Expected Result

The project has a clear learning plan and a shared architecture vocabulary.

### 5. How to Test

Read the roadmap and verify that every milestone is small, clear, and has the required six sections.

### 6. Documentation to Generate

`01-overview.md` - project overview, architecture diagram, glossary, and final goal.

---

## Milestone 2 - Simple Wine Catalog Model

### 1. Goal

Create the basic wine catalog data model that will become the source of truth.

### 2. Theory

- A JPA entity maps a Java class to a relational table.
- A repository is the data access layer for reading and writing entities.
- The relational database stores the real business data.
- Embeddings and vector stores are secondary indexes, not the source of truth.
- The wine model should contain both structured fields, such as price and stock, and descriptive fields, such as taste notes and food pairings.

### 3. Implementation Scope

- Add persistence dependencies if they are not present yet.
- Create a `Wine` entity.
- Create a `WineRepository`.
- Use a simple database for learning, such as H2.
- Load a small sample catalog.

### 4. Expected Result

The application can store and read wine records from a relational database.

### 5. How to Test

- Start the application.
- Verify that sample wines are loaded.
- Check the records through logs, the H2 console, or a temporary repository call.

### 6. Documentation to Generate

`02-wine-catalog-model.md` - entity fields, repository role, database choice, and sample data explanation.

---

## Milestone 3 - Simple Spring Boot REST Endpoint

### 1. Goal

Expose the wine catalog through a simple REST API, without AI.

### 2. Theory

- A REST controller exposes HTTP endpoints.
- A service layer keeps business logic out of the controller.
- A DTO can separate the API response from the database entity.
- This step proves that the catalog works before adding AI.

### 3. Implementation Scope

- Create `WineService`.
- Create `WineController`.
- Add `GET /api/wines`.
- Add `GET /api/wines/{id}`.
- Optionally create a simple `WineResponse` DTO.

### 4. Expected Result

The API returns wine catalog data as JSON.

### 5. How to Test

Call:

```bash
curl http://localhost:8080/api/wines
curl http://localhost:8080/api/wines/1
```

Verify that the responses contain real catalog records.

### 6. Documentation to Generate

`03-basic-rest-endpoint.md` - controller, service, repository flow, endpoint examples, and manual tests.

---

## Milestone 4 - Simple Ollama Chatbot Using `ChatClient`

### 1. Goal

Connect Spring Boot to Ollama through Spring AI and create a basic chatbot endpoint.

### 2. Theory

- `ChatClient` is Spring AI's high-level API for sending prompts to a chat model.
- Ollama runs the model locally.
- Spring Boot can auto-configure the Ollama chat model from properties.
- A user message is sent to the model and the model returns generated text.
- At this stage, the model is not connected to the wine catalog.

### 3. Implementation Scope

- Configure Ollama properties.
- Make sure the selected chat model is installed in Ollama.
- Create or adapt a chat endpoint, such as `POST /api/chat`.
- Send the user's message to `ChatClient`.

### 4. Expected Result

The application can send a message to a local Ollama model and return the answer.

### 5. How to Test

Call:

```bash
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '{"message":"Hello, what are you?"}'
```

Verify that Ollama returns a generated response.

### 6. Documentation to Generate

`04-chat-client-ollama.md` - Ollama setup, Spring AI configuration, `ChatClient`, and prompt-response flow.

---

## Milestone 5 - Prompt Customization With System Prompts

### 1. Goal

Teach the assistant to behave like a wine recommendation assistant by using a system prompt.

### 2. Theory

- A system prompt defines the assistant's role and rules.
- A user prompt contains the user's current question.
- A good system prompt can control tone, language, and boundaries.
- The model still does not know the catalog unless catalog data is provided.
- A system prompt helps behavior, but it does not replace retrieval.

### 3. Implementation Scope

- Define a default system prompt for a helpful sommelier assistant.
- Configure `ChatClient` to use that system prompt.
- Keep the same basic chat endpoint.

### 4. Expected Result

The assistant answers in the expected role and style.

### 5. How to Test

Ask:

```text
Recommend a red wine for a beginner.
```

Verify that the answer sounds like a wine assistant and uses the requested language and style.

### 6. Documentation to Generate

`05-system-prompts.md` - system prompt vs user prompt, prompt design, and limitations.

---

## Milestone 6 - Wine Recommendation Without RAG, Using Only Prompt Context

### 1. Goal

Build a first recommendation endpoint by placing a small catalog directly inside the prompt.

### 2. Theory

- Prompt context is extra information included with the user's question.
- The LLM can answer using catalog data if the catalog is included in the prompt.
- This approach is simple but does not scale because prompts have token limits.
- Passing the whole catalog becomes slow, expensive, and less reliable as the catalog grows.
- This milestone demonstrates why RAG is needed.

### 3. Implementation Scope

- Read a small list of wines from the relational database.
- Convert the wines to readable text.
- Build a prompt that includes the wine list and the user's question.
- Create `POST /api/recommend` for basic recommendations.

### 4. Expected Result

The assistant recommends wines that exist in the catalog, but only because the whole small catalog is included in the prompt.

### 5. How to Test

Ask:

```text
What wine should I serve with grilled meat?
```

Verify that the answer mentions wines from the sample catalog and does not invent wine names.

### 6. Documentation to Generate

`06-basic-recommendation-without-rag.md` - prompt context, catalog formatting, token limits, and why this does not scale.

---

## Milestone 7 - Embedding Concept and Wine Document Representation

### 1. Goal

Understand embeddings and design the text representation that will be embedded for each wine.

### 2. Theory

- An embedding is a vector of numbers that represents the meaning of text.
- Similar texts have vectors that are close to each other.
- A wine row must be converted into a natural language document before embedding.
- Good document text improves retrieval quality.
- Structured fields and descriptive fields should both be included when they help search.

### 3. Implementation Scope

- Design a `WineDocumentMapper` concept.
- Decide which fields are included in the searchable wine text.
- Define a consistent format for the wine document.
- Do not store embeddings yet.

### 4. Expected Result

There is a clear text document format for each wine.

Example:

```text
Name: Malbec Reserva.
Type: Red wine.
Grape variety: Malbec.
Region: Mendoza, Argentina.
Taste notes: plum, blackberry, soft tannins.
Body: medium.
Acidity: medium.
Sweetness: dry.
Food pairings: grilled meat, empanadas, hard cheese.
Description: Smooth red wine for casual dinners.
```

### 5. How to Test

Pick two or three sample wines and manually inspect their generated document text.

### 6. Documentation to Generate

`07-embeddings-concept.md` - embedding explanation, document design, and field selection.

---

## Milestone 8 - Generate Embeddings for Wine Records

### 1. Goal

Use Spring AI and Ollama to generate embeddings from wine documents.

### 2. Theory

- `EmbeddingModel` converts text into vectors.
- Ollama can run local embedding models, such as `nomic-embed-text`.
- The chat model and embedding model are usually different models.
- The vector dimension depends on the embedding model.
- At this step, generating and inspecting vectors is enough.

### 3. Implementation Scope

- Configure an Ollama embedding model.
- Inject Spring AI's `EmbeddingModel`.
- Generate an embedding for one wine document.
- Log or inspect the vector dimension.
- Do not introduce a vector store yet.

### 4. Expected Result

The application can turn a wine document into a numeric vector.

### 5. How to Test

Run a small manual flow that prints:

- Wine id.
- Wine document text.
- Embedding vector size.

The exact vector values do not need to be read manually.

### 6. Documentation to Generate

`08-generate-embeddings.md` - embedding model setup, generated vector shape, and difference between chat models and embedding models.

---

## Milestone 9 - Store Embeddings in a Vector Store

### 1. Goal

Store wine documents and embeddings in a vector store so they can be searched later.

### 2. Theory

- A vector store stores documents and their embeddings.
- Spring AI provides a `VectorStore` abstraction.
- A `Document` contains content and metadata.
- Metadata should include `wineId` so search results can be connected back to the relational database.
- Indexing means generating embeddings for records and saving them into the vector store.

### 3. Implementation Scope

- Choose a simple vector store for learning.
- Create a service that indexes wine records.
- Store one document per wine.
- Include metadata such as `wineId`, `type`, `price`, and `stockAvailable`.

### 4. Expected Result

The vector store contains searchable wine documents.

### 5. How to Test

Run the indexing process and verify that:

- No errors occur.
- One document is stored per wine.
- Metadata is present.

### 6. Documentation to Generate

`09-vector-store.md` - vector store choice, Spring AI `Document`, metadata, and indexing flow.

---

## Milestone 10 - Perform Semantic Search Over Wines

### 1. Goal

Search the vector store with a natural language query and retrieve relevant wines.

### 2. Theory

- Semantic search compares the meaning of the user's query with the meaning of wine documents.
- The query is also converted into an embedding.
- `topK` controls how many results are returned.
- A similarity threshold can remove weak matches.
- Search results should be mapped back to real wine records using `wineId`.

### 3. Implementation Scope

- Create a semantic search service.
- Search the vector store from a user query.
- Read matching wine records from the relational database.
- Add a simple endpoint such as `GET /api/wines/search?q=...`.

### 4. Expected Result

Queries like "soft red wine for a beginner" return semantically relevant wines, even if the exact words do not match.

### 5. How to Test

Call:

```bash
curl "http://localhost:8080/api/wines/search?q=soft%20red%20wine%20for%20a%20beginner"
```

Verify that the returned wines make sense for the query.

### 6. Documentation to Generate

`10-semantic-search.md` - semantic search, `topK`, threshold, and mapping vector results back to database records.

---

## Milestone 11 - Build a Basic RAG Recommendation Endpoint

### 1. Goal

Combine semantic search and `ChatClient` so the assistant answers using retrieved catalog wines.

### 2. Theory

- RAG has two main phases: retrieve and generate.
- Retrieve means finding relevant wines from the vector store.
- Generate means asking the LLM to answer using those wines as context.
- The LLM should be instructed to recommend only wines present in the retrieved context.
- This avoids passing the full catalog and reduces hallucination.

### 3. Implementation Scope

- Update or create `POST /api/recommend`.
- Retrieve top matching wines for the user's question.
- Build a prompt with only those wines as context.
- Ask the LLM for a recommendation.
- Return the answer.

### 4. Expected Result

The assistant recommends wines from the catalog using RAG.

### 5. How to Test

Ask:

```text
Which wine from my catalog pairs well with seafood?
```

Verify that:

- The answer names existing catalog wines.
- The reason is based on retrieved wine fields.
- The answer does not invent unavailable wines.

### 6. Documentation to Generate

`11-rag-recommendation.md` - RAG flow, prompt context, retrieval results, and comparison with the non-RAG approach.

---

## Milestone 12 - Add SQL Filters Such as Price, Stock, Type, and Food Category

### 1. Goal

Combine semantic search with hard filters such as price, stock availability, and wine type.

### 2. Theory

- Semantic search is good for meaning, but not always enough for exact constraints.
- Price, stock, and type are structured filters.
- Structured filters can be handled through SQL, vector metadata filters, or a hybrid approach.
- The relational database remains the safest place for exact business constraints.
- User requests often mix soft intent and hard rules, such as "good for pasta" and "under 20 dollars".

### 3. Implementation Scope

- Define a simple `WineSearchCriteria`.
- Support optional filters such as max price, type, stock, and food category.
- Apply filters before or after semantic search, depending on the chosen approach.
- Keep the first version simple and explicit.

### 4. Expected Result

The assistant recommends relevant wines while respecting hard filters.

### 5. How to Test

Ask:

```text
Recommend a wine for pasta with tomato sauce under 20 dollars.
```

Verify that the answer only recommends wines that satisfy the price condition and are appropriate for the food.

### 6. Documentation to Generate

`12-sql-filters.md` - semantic vs structured filters, criteria object, SQL filtering, and tradeoffs.

---

## Milestone 13 - Improve Answer Format Using Structured Output

### 1. Goal

Return recommendations in a predictable structure instead of only free text.

### 2. Theory

- Free text is easy for humans but harder for applications to parse.
- Structured output asks the LLM to return data that matches a schema.
- Spring AI can map model output into Java records or classes.
- A structured response is useful for a frontend, tests, and validation.
- The model can still make mistakes, so validation is important.

### 3. Implementation Scope

- Define a recommendation response shape.
- Include fields such as `wineId`, `name`, `reason`, `pairingExplanation`, and `confidence`.
- Use Spring AI structured output support.
- Return JSON from the recommendation endpoint.

### 4. Expected Result

The recommendation endpoint returns predictable JSON.

### 5. How to Test

Call the recommendation endpoint and verify that:

- The response is valid JSON.
- Required fields are present.
- `wineId` refers to a real wine.
- The explanation is understandable.

### 6. Documentation to Generate

`13-structured-output.md` - structured output, schema design, validation, and frontend usefulness.

---

## Milestone 14 - Add Tests

### 1. Goal

Add focused tests for the catalog, search, and recommendation flow.

### 2. Theory

- Repository tests check persistence.
- Service tests check business logic.
- Controller tests check HTTP behavior.
- LLM calls are non-deterministic and should usually be mocked in automated tests.
- Integration tests with Ollama can exist, but they should be optional.
- Tests should verify behavior and structure, not exact LLM wording.

### 3. Implementation Scope

- Add tests for `WineRepository`.
- Add tests for catalog and search services.
- Add controller tests for REST endpoints.
- Mock AI dependencies where needed.
- Optionally add a manual integration test profile for Ollama.

### 4. Expected Result

The main project behavior can be verified with automated tests.

### 5. How to Test

Run:

```bash
./mvnw test
```

Verify that tests pass without requiring Ollama for the normal test suite.

### 6. Documentation to Generate

`14-tests.md` - testing strategy, mocks, integration tests, and LLM non-determinism.

---

## Milestone 15 - Add Final Documentation and Future Improvements

### 1. Goal

Consolidate the project documentation and identify next learning steps.

### 2. Theory

- A good learning project should explain not only what was built, but why each decision was made.
- RAG systems need maintenance: reindexing, data freshness, evaluation, and observability.
- Future improvements can include reranking, hybrid search, conversation memory, streaming responses, admin catalog tools, and better evaluation.

### 3. Implementation Scope

- Review all milestone documents.
- Update the project `README.md`.
- Add setup instructions for Java, Maven, Ollama, models, and database.
- Document all endpoints.
- Add a future improvements section.

### 4. Expected Result

The project is understandable and reproducible by another developer.

### 5. How to Test

Follow the documentation from a clean setup and verify that:

- The app starts.
- The catalog loads.
- Embeddings can be indexed.
- Semantic search works.
- RAG recommendations work.

### 6. Documentation to Generate

`15-final-docs-and-future-improvements.md` - final project summary, setup guide, endpoint index, and future roadmap.

---

## Learning Flow Summary

```text
1.  Understand the architecture.
2.  Create the wine catalog model.
3.  Expose the catalog through REST.
4.  Connect to Ollama with ChatClient.
5.  Guide the model with system prompts.
6.  Recommend with prompt context only.
7.  Learn embeddings and wine documents.
8.  Generate embeddings.
9.  Store embeddings.
10. Search wines semantically.
11. Build the RAG endpoint.
12. Add structured filters.
13. Return structured output.
14. Add tests.
15. Finish documentation and future improvements.
```

Main rule: each milestone should introduce one new concept. If a milestone feels too large during implementation, split it before moving forward.
