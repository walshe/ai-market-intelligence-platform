
# Story 04: Retrieval & RAG

## Description

Implement Retrieval-Augmented Generation (RAG) capabilities that allow authenticated users to submit an analysis query and receive a structured, context-grounded response.

This story introduces:

- Vector similarity retrieval over stored embeddings
- Prompt construction using retrieved document chunks
- LLM-based response generation
- An authenticated REST endpoint for analysis queries

This story does NOT implement cost aggregation dashboards (Story 05).

---

## Requirements

- [ ] Implement `RetrievalService` to fetch top-k relevant `DocumentChunk` records using cosine similarity against `document_chunk.embedding`.
  - Must use pgvector cosine operator.
  - `topK` must be configurable (default value defined in application properties).

- [ ] Implement query embedding flow:
  - Use existing `EmbeddingService` (Story 03) to embed user queries.
  - Query embedding must use the same embedding model used during ingestion.

- [ ] Implement `PromptBuilderService` to construct a deterministic prompt that includes:
  - System instruction defining financial analysis role.
  - Ordered retrieved context chunks.
  - User query.
  - Explicit instruction to return structured JSON.

- [ ] Implement `LlmClient` interface and `OpenAiLlmClient` implementation for generation.
  - Must abstract provider-specific logic.
  - Must return:
    - Generated content
    - Model used
    - Token usage (from generation call)

- [ ] Implement authenticated endpoint:
  - `POST /api/v1/analysis`
  - Must require valid JWT.
  - Must accept query input (and optional topK override).
  - Must return structured response as defined below.

- [ ] Structured response format (must match exactly):

```json
{
  "summary": "...",
  "riskFactors": ["..."],
  "confidenceScore": 0.0,
  "modelUsed": "...",
  "tokensUsed": 0
}
````

* [ ] Ensure embeddings are NOT regenerated during retrieval (only query embedding is generated).

* [ ] Provide integration test verifying full RAG flow:

  * Query embedding
  * Vector retrieval
  * Prompt construction
  * LLM generation
  * Structured response mapping

---

## Acceptance Criteria

* Query embedding is generated using the configured embedding model.
* Retrieval uses cosine similarity over `vector(1536)` embeddings.
* Top-k retrieval is configurable and defaults correctly.
* Prompt includes ordered context chunks and user query.
* LLM response is parsed into the required structured JSON format.
* Token usage returned reflects generation call usage.
* `POST /api/v1/analysis` returns HTTP 200 for valid requests.
* Endpoint is protected by JWT authentication.
* Integration tests pass.
* Application builds and runs without unrelated changes.

---

## Non-Goals

* No re-ranking logic.
* No hybrid keyword + vector search.
* No agent workflows.
* No cost aggregation dashboard.
* No UI features.