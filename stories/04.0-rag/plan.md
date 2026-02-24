````markdown
# Implementation Plan - Story 04: Retrieval & RAG

## 1. Objective

Implement Retrieval-Augmented Generation (RAG) capabilities that allow authenticated users to submit an analysis query and receive a structured, context-grounded response.

This story introduces:

- Query embedding generation
- Cosine-similarity vector retrieval via pgvector
- Deterministic prompt construction
- LLM-based response generation
- Authenticated REST endpoint: `POST /api/v1/analysis`
- Structured JSON response mapping

This story does NOT introduce cost aggregation dashboards (Story 05), hybrid search, re-ranking, or agent workflows.

---

## 2. Requirements Traceability

This implementation must satisfy:

- Configurable top-k cosine similarity retrieval
- Use of existing `EmbeddingService` for query embedding
- Deterministic `PromptBuilderService`
- Provider-abstracted `LlmClient`
- Structured response contract enforcement
- JWT-protected endpoint
- Full RAG integration test

---

## 3. Architectural Alignment

The design must preserve clear service boundaries:

- `EmbeddingService` → generates query embeddings (no DB logic)
- `RetrievalService` → performs vector similarity search only
- `PromptBuilderService` → builds prompt text only
- `LlmClient` → performs outbound LLM generation only
- Controller / Orchestrator → coordinates flow

Provider-specific logic must be isolated behind `LlmClient`.

No service should perform responsibilities outside its domain.

---

## 4. Implementation Phases

---

### Phase 1 — RetrievalService (Cosine Similarity)

**Goal:** Retrieve top-k `DocumentChunk` records ordered by cosine similarity.

#### Requirements:

- Use pgvector cosine similarity operator.
- Query against `document_chunk.embedding`.
- Results must be ordered highest similarity first.
- `topK` must be configurable via application properties.
- Default `topK` must be defined (e.g., 5).

#### Steps:

1. Define `RetrievalService` interface:
   - Input: `float[] queryEmbedding`, `int topK`
   - Output: ordered `List<DocumentChunk>`

2. Implement native query using pgvector cosine operator.

3. Bind default `topK` from configuration.

#### Deliverables:

- `RetrievalService` interface
- Implementation class
- Repository native query
- Unit test validating ordering + topK behavior

---

### Phase 2 — Query Embedding Flow

**Goal:** Generate embedding for user query.

#### Requirements:

- Must use existing `EmbeddingService`.
- Must use the same embedding model as ingestion.
- Must NOT regenerate stored chunk embeddings.

#### Steps:

1. Inject `EmbeddingService` into orchestration layer.
2. Call `embed(query)` to generate `float[]`.

#### Deliverables:

- Verified embedding call in analysis flow
- Unit test verifying embedding invocation

---

### Phase 3 — PromptBuilderService

**Goal:** Build deterministic prompt from context + query.

#### Requirements:

Prompt must include:

- System instruction defining financial analysis assistant role.
- Retrieved context chunks in correct order.
- User query.
- Explicit instruction to return structured JSON.

Prompt must be deterministic:
- Same inputs → same output.

#### Steps:

1. Define `PromptBuilderService` interface.
2. Implement prompt template.
3. Preserve chunk ordering in output.

#### Deliverables:

- `PromptBuilderService` implementation
- Unit tests validating:
  - Context ordering
  - Presence of query
  - Presence of JSON output instruction

---

### Phase 4 — LlmClient & OpenAiLlmClient

**Goal:** Generate structured response from LLM.

#### Requirements:

- Define `LlmClient` abstraction.
- Implement `OpenAiLlmClient`.
- Must return:
  - Generated content
  - Model used
  - Token usage (from generation call only)

#### Steps:

1. Define `LlmClient` interface:
   - Input: prompt text
   - Output: response object containing content + usage metadata

2. Implement OpenAI chat/completion call.
3. Extract token usage from provider response.
4. Return structured result object.

#### Deliverables:

- `LlmClient` interface
- `OpenAiLlmClient` implementation
- Unit test with mocked provider response

---

### Phase 5 — POST /api/v1/analysis Endpoint

**Goal:** Implement full RAG orchestration.

#### Flow:

1. Authenticate request via JWT.
2. Validate request body:
   - Required: `query`
   - Optional: `topK`
3. Generate query embedding.
4. Retrieve top-k chunks via `RetrievalService`.
5. Build prompt via `PromptBuilderService`.
6. Call `LlmClient`.
7. Parse and validate JSON response.
8. Return structured API response.

#### Structured Response Contract:

```json
{
  "summary": "...",
  "riskFactors": ["..."],
  "confidenceScore": 0.0,
  "modelUsed": "...",
  "tokensUsed": 0
}
````

If JSON parsing fails:

* Return controlled error response (simple 5xx acceptable for MVP).

#### Deliverables:

* Request DTO
* Response DTO
* Controller endpoint
* Orchestration method (e.g., `AnalysisService`)

---

### Phase 6 — Integration Testing

**Goal:** Validate full RAG cycle.

Test must verify:

* Query embedding is generated
* Cosine retrieval returns ordered chunks
* Prompt includes context + query
* LLM client is invoked
* Structured JSON response returned
* HTTP 200 for valid JWT request

#### Deliverables:

* End-to-end integration test
* Verified application boot without errors

---

## 5. Configuration Requirements

* `topK` default must be configurable via application properties.
* Embedding model must be consistent with ingestion model.
* OpenAI API key must be externally configured (environment variable).

---

## 6. Non-Goals

This story must NOT:

* Introduce re-ranking
* Implement hybrid search
* Add agent workflows
* Add cost aggregation dashboard
* Modify database schema

---

## 7. Completion Criteria

Story 04 is complete when:

* `POST /api/v1/analysis` returns structured JSON per contract.
* Cosine similarity retrieval works with configurable topK.
* Query embedding is generated using configured model.
* Token usage is captured from generation call.
* Integration tests pass.
* Application builds and runs successfully.

```

If you're ready, next step is generating the aligned `tasks.md` from this plan.
```
