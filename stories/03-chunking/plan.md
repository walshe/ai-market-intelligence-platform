# Implementation Plan - Story 03: Chunking & Embeddings

## 1. Objective

Implement the services responsible for:

- Splitting documents into deterministic, sentence-aware chunks
- Generating embeddings for each chunk via an LLM provider
- Persisting chunk + embedding records into `document_chunk`

This story introduces application-layer AI integration but does NOT implement retrieval or analysis endpoints.

---

## 2. Requirements Traceability

This plan implements the following requirements:

- Implement `ChunkingService`
- Implement `EmbeddingService`
- Persist `document_chunk` records with embeddings
- Ensure deterministic and simple chunking logic
- Verify embeddings are stored correctly
- Provide unit tests for chunking and embedding integration (mocked)

---

## 3. Architectural Alignment

The implementation must respect the service layering defined in `ARCHITECTURE.md`:

- IngestionService (or equivalent orchestrator)
- ChunkingService
- EmbeddingService
- Persistence via repository layer

The `EmbeddingService` must not directly call controllers.
The `ChunkingService` must not depend on database logic.
Separation of concerns must be preserved.

---

## 4. Implementation Phases

---

### Phase 1 — Implement ChunkingService

Goal:
Provide deterministic, sentence-aware document segmentation.

Design Constraints:

- Chunking must be deterministic (same input → same output).
- Use simple sentence-aware splitting (e.g., punctuation-based).
- Avoid external NLP dependencies for v1.
- Enforce a maximum character or token threshold per chunk.
- Preserve original ordering via `chunk_index`.

Deliverables:

- `ChunkingService` interface
- Concrete implementation (e.g., `SimpleChunkingService`)
- Unit tests validating:
  - Deterministic output
  - Correct chunk ordering
  - Proper boundary handling

No database writes in this phase.

---

### Phase 2 — Implement EmbeddingService

Goal:
Generate vector embeddings for chunk text using configured model.

Design Constraints:

- Create `EmbeddingService` interface.
- Implement provider-backed class (e.g., `OpenAiEmbeddingService`).
- Must accept plain text and return vector (float[] or List<Double>).
- Must not persist data directly.
- Must not contain retrieval logic.
- Model name must be configurable.

Deliverables:

- Interface definition
- Provider implementation
- Configuration binding for API key + model
- Unit tests with mocked provider client

No controller logic in this phase.

---

### Phase 3 — Orchestrate Chunk + Embedding Persistence

Goal:
Combine chunking + embedding generation into ingestion workflow.

Flow:

1. Accept document content.
2. Invoke `ChunkingService`.
3. For each chunk:
   - Generate embedding via `EmbeddingService`.
   - Persist `DocumentChunk` with:
     - `chunk_index`
     - `chunk_text`
     - `embedding_model`
     - `embedding`
     - `document` reference

Design Constraints:

- Preserve chunk ordering.
- Ensure embedding matches vector(1536).
- Ensure embedding is stored correctly in pgvector column.
- Ensure transactional consistency (single document ingestion).

Deliverables:
