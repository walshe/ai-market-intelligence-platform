You’re right — I misapplied the “Phase 1 only” pattern. For **Story 04**, `tasks.md` should cover **all phases in the story plan**, and then when you *execute* you’ll say “implement Phase 1 only”.

Here’s a full **tasks.md for the entire Story 04 (all phases)**, aligned 1:1 with the `plan.md`.

```markdown
# Tasks - Story 04: Retrieval & RAG

## Phase 1 — RetrievalService (Cosine Similarity)

- [ ] Create `RetrievalService` interface with method:
      `List<DocumentChunk> retrieveSimilar(float[] queryEmbedding, int topK);`
      - Document behavior: cosine similarity, ordered highest similarity first.

- [ ] Add configuration for default topK (e.g., 5) under application properties.
      - Bind into configuration properties class.

- [ ] Implement `RetrievalServiceImpl`:
      - Uses repository native query.
      - Falls back to configured default topK when topK <= 0.
      - Does NOT generate embeddings and does NOT call LLM.

- [ ] Add native cosine similarity query to `DocumentChunkRepository` using pgvector cosine operator and LIMIT topK.

- [ ] Unit test for retrieval:
      - Persists sample data (Document + DocumentChunks with embeddings).
      - Verifies ordering and topK behavior.
      - Verifies defaultTopK fallback works.

- [ ] Verify application builds and tests pass.

---

## Phase 2 — Query Embedding Flow

- [ ] Add orchestration code that calls existing `EmbeddingService` to embed the user query.
      - Ensure embedding model used is the configured one.
      - Ensure only the query is embedded (no regeneration of stored chunk embeddings).

- [ ] Unit test verifying `EmbeddingService.embed(query)` is invoked during analysis flow orchestration.

---

## Phase 3 — PromptBuilderService

- [ ] Create `PromptBuilderService` interface.

- [ ] Implement `PromptBuilderServiceImpl` to build deterministic prompt including:
      - System instruction (financial analysis assistant role)
      - Ordered context chunks (numbered)
      - User query
      - Explicit instruction to return structured JSON matching the contract

- [ ] Unit tests for prompt builder:
      - Same inputs produce same prompt
      - Context ordering preserved
      - Contains query and JSON instruction

- [ ] Verify application builds and tests pass.

---

## Phase 4 — LlmClient & OpenAiLlmClient

- [ ] Create `LlmClient` interface for generation.

- [ ] Create response object returned by `LlmClient` containing:
      - generated content (string)
      - model used (string)
      - tokens used (int; from generation call)

- [ ] Implement `OpenAiLlmClient`:
      - Uses configured base URL + API key
      - Calls OpenAI chat/completions endpoint
      - Extracts token usage from provider response
      - Returns `LlmClient` response object

- [ ] Unit tests for `OpenAiLlmClient` using mocked HTTP client:
      - Happy path parses content + usage
      - Empty/invalid response throws controlled error

- [ ] Verify application builds and tests pass.

---

## Phase 5 — POST /api/v1/analysis Endpoint

- [ ] Create request DTO:
      - `query` (required)
      - `topK` (optional)

- [ ] Create response DTO matching contract exactly:
      - `summary`
      - `riskFactors`
      - `confidenceScore`
      - `modelUsed`
      - `tokensUsed`

- [ ] Implement orchestration service (e.g., `AnalysisService`) performing:
      1) embed query
      2) retrieve topK chunks
      3) build prompt
      4) call LLM
      5) parse JSON response
      6) map to response DTO (including modelUsed + tokensUsed)

- [ ] Implement controller endpoint `POST /api/v1/analysis`:
      - JWT-protected
      - Validates request body
      - Delegates to orchestration service
      - Returns HTTP 200 with response DTO

- [ ] Implement JSON parsing/validation of LLM output:
      - Must enforce required fields
      - If parsing fails, return controlled error response (simple 5xx acceptable for MVP)

- [ ] Verify application builds and endpoint boots.

---

## Phase 6 — Integration Testing

- [ ] Create end-to-end integration test:
      - Inserts a Document + multiple DocumentChunks with embeddings
      - Calls `POST /api/v1/analysis` with valid JWT
      - Asserts 200 response
      - Asserts all response fields exist and types are correct
      - Asserts tokensUsed and modelUsed are populated
      - Ensures retrieval path is exercised (non-empty context)

- [ ] Confirm tests pass and application boots without errors.

---

## Final Verification

- [ ] Confirm retrieval uses cosine similarity and configurable topK.
- [ ] Confirm only query embedding is generated at query time.
- [ ] Confirm prompt construction is deterministic and includes ordered context.
- [ ] Confirm response contract matches exactly.
- [ ] Confirm no schema changes were introduced.
- [ ] Confirm no non-goal features were implemented (no re-ranking, no hybrid search, no agents, no cost dashboard).
- [ ] Confirm application builds successfully.
```
