# Tasks - Story 03: Chunking & Embeddings

## Phase 1 — Implement ChunkingService

- [x] Create `ChunkingService` interface.

- [x] Create concrete implementation (e.g., `SimpleChunkingService`).

- [x] Implement deterministic, sentence-aware splitting:
      - Split on punctuation boundaries.
      - Enforce maximum chunk length.
      - Preserve original text order.

- [x] Ensure chunking logic does NOT access database.

- [x] Write unit tests for `ChunkingService`:
      - Deterministic output for identical input.
      - Correct chunk ordering.
      - Proper handling of edge cases (short text, long text, punctuation boundaries).

- [x] Verify tests pass and application builds.

---

## Phase 2 — Implement EmbeddingService

- [x] Create `EmbeddingService` interface.

- [x] Implement provider-backed class (e.g., `OpenAiEmbeddingService`).

- [x] Implement method to:
      - Accept plain text input.
      - Return embedding vector (`float[]` or `List<Double>`).
      - Use configured embedding model.

- [x] Bind configuration properties:
      - API key
      - Model name

- [x] Ensure `EmbeddingService` does NOT:
      - Persist entities.
      - Call controllers.
      - Contain retrieval logic.

- [x] Write unit tests:
      - Mock provider client.
      - Verify embedding vector is returned correctly.
      - Verify correct model configuration is used.

- [x] Verify tests pass and application builds.

---

## Phase 3 — Orchestrate Chunk + Embedding Persistence

- [x] Integrate `ChunkingService` into ingestion workflow.

- [x] For each generated chunk:
      - [x] Generate embedding via `EmbeddingService`.
      - [x] Create `DocumentChunk` entity with:
            - [x] `chunk_index`
            - [x] `chunk_text`
            - [x] `embedding_model`
            - [x] `embedding`
            - [x] Associated `Document`

- [x] Persist `DocumentChunk` entities via repository layer.

- [x] Ensure chunk ordering is preserved via `chunk_index`.

- [x] Ensure embedding length matches `vector(1536)`.

- [x] Ensure ingestion runs within a transactional boundary.

- [x] Write integration test:
      - [x] Ingest sample document.
      - [x] Verify multiple `DocumentChunk` rows created.
      - [x] Verify embeddings stored.
      - [x] Verify `embedding_model` populated.
      - [x] Verify chunk ordering is correct.

- [x] Verify application boots and ingestion workflow executes successfully.

---

## Final Verification

- [x] Confirm no retrieval logic was introduced.
- [x] Confirm no schema changes were introduced.
- [x] Confirm all tests pass.
- [x] Confirm application builds successfully.
