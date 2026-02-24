```markdown
# Story 04.5: Integrate IngestionService with DocumentResource

## Description

Wire the existing `IngestionService` into the `DocumentResource` lifecycle so that when a document is created (and optionally when updated), the document is automatically chunked, embedded, and persisted into `document_chunk` for downstream RAG queries.

This story also tightens the exposed surface area of the `DocumentResource` endpoints to reduce accidental data exposure and to prevent expensive ingestion from being triggered via unsafe or overly-broad operations.

---

## Requirements

### A) Ingestion Integration

- [ ] On successful `POST /api/documents` (document creation), the system must trigger ingestion:
  - Persist the `Document` (existing behavior).
  - Invoke `IngestionService.ingestDocument(documentId)` after persistence.
  - Result: `document_chunk` rows are created with embeddings.

- [ ] Define ingestion behavior for updates:
  - `PUT /api/documents/{id}` and `PATCH /api/documents/{id}` must NOT automatically re-ingest by default (to avoid accidental re-embedding costs and duplicate chunks).
  - If update-driven ingestion is required, it must be explicit via a dedicated endpoint (see section B).

- [ ] Ingestion must be idempotent with respect to a document:
  - Before ingesting, the system must ensure it will not create duplicate chunk rows for the same document.
  - Minimum acceptable behavior for MVP:
    - Delete existing `document_chunk` rows for the document before re-ingesting, OR
    - Skip ingestion if chunks already exist for the document.
  - The chosen behavior must be deterministic and documented.

- [ ] Ingestion must be transactional:
  - Either all chunks for a document are persisted successfully, or none are.

- [ ] `created_at` must be database-managed:
  - Ingestion must NOT set `createdAt` manually on `DocumentChunk`.
  - The DB `DEFAULT now()` must populate `created_at`.

---

### B) Explicit Ingestion Endpoint (Recommended)

- [ ] Add an explicit ingestion endpoint to `DocumentResource`:

  - `POST /api/documents/{id}/ingest`

  Behavior:
  - Triggers ingestion for an existing document.
  - Returns an appropriate success response (200 OK or 202 Accepted).
  - Used for re-ingestion after edits, without coupling ingestion to update operations.

- [ ] Guardrails:
  - Endpoint must be authenticated (JWT).
  - If document does not exist, return 404.
  - If document content is empty, ingestion is a no-op (200 OK).

---

### C) Security and API Surface

- [ ] Lock down document listing exposure:
  - `GET /api/documents` should NOT return full document content by default (content may be large and sensitive).
  - For MVP, one of the following must be implemented:
    - Return a lightweight DTO (id, title, createdAt) for list view, OR
    - Remove/disable the list endpoint if not required for the demo.

- [ ] Ensure all document endpoints require authentication (JWT):
  - `POST /api/documents`
  - `PUT /api/documents/{id}`
  - `PATCH /api/documents/{id}`
  - `GET /api/documents`
  - `GET /api/documents/{id}`
  - `DELETE /api/documents/{id}`
  - `POST /api/documents/{id}/ingest` (new)

- [ ] Ensure ingestion is not triggerable via unsafe operations:
  - `GET` endpoints must never trigger ingestion.
  - `DELETE` must clean up associated chunks (either via cascade or explicit delete) to avoid orphaned embeddings.

---

### D) Observability

- [ ] Add logging for ingestion lifecycle:
  - Document id
  - Number of chunks produced
  - Embedding model used
  - Any ingestion skip condition (e.g., already ingested, empty content)

---

### E) Testing

- [ ] Add an integration test verifying:
  - `POST /api/documents` results in:
    - One persisted `Document`
    - Multiple persisted `DocumentChunk` rows with embeddings
  - If explicit ingest endpoint is implemented:
    - `POST /api/documents/{id}/ingest` triggers chunk creation for an existing document

- [ ] Add a test verifying no duplicate chunks are created on repeated ingestion attempts, consistent with the chosen idempotency strategy.

---

## Acceptance Criteria

- Creating a document via `POST /api/documents` results in chunk rows persisted in `document_chunk` with non-null embeddings.
- Ingestion cannot be accidentally triggered by `GET` operations.
- Updates do not silently cause expensive re-ingestion unless explicitly invoked via `/ingest`.
- Document listing does not expose full document content by default (or list endpoint removed).
- All relevant endpoints require JWT authentication.
- Integration tests pass and application boots successfully.

---

## Non-Goals

- No UI changes.
- No cost aggregation dashboards (Story 05).
- No advanced ingestion pipelines (async queues, retries, batch embeddings).
- No multi-tenancy or ownership-based row filtering beyond JWT authentication.
```
