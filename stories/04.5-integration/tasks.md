# Tasks - Story 04.5: Integrate IngestionService with DocumentResource

## Phase 1 — Wire Ingestion to Document Creation

- [ ] Inject `IngestionService` into `DocumentResource`.

- [ ] Update `createDocument()` flow:
  - Persist document via `DocumentService.save(documentDTO)`
  - After successful save, call `ingestionService.ingestDocument(savedId)`
  - Keep controller logic minimal (no chunking logic in controller)

- [ ] Verify manual DB behavior after POST:
  - `document` row is created
  - `document_chunk` rows are created
  - `embedding` is non-null for chunks

- [ ] Remove manual `createdAt` assignment from ingestion flow:
  - Do not set `DocumentChunk.createdAt` in code
  - Rely on DB `DEFAULT now()`

- [ ] Build and run application; confirm no errors.

---

## Phase 2 — Enforce Idempotent Ingestion

- [ ] Choose and implement one idempotency strategy in `IngestionService`:

  **Option A (Recommended):**
  - Delete existing `document_chunk` rows for a document before re-ingesting

  **OR Option B:**
  - If chunks already exist, skip ingestion and log

- [ ] Implement idempotency in `ingestDocument(Long documentId)` prior to chunk creation.

- [ ] Add logs for ingestion lifecycle:
  - document id
  - chunk count produced
  - embedding model used
  - skip condition (already ingested / empty content)

- [ ] Add test verifying idempotency:
  - Repeated ingestion does not create duplicates (consistent with chosen strategy)

- [ ] Build and run tests.

---

## Phase 3 — Add Explicit Ingestion Endpoint

- [ ] Add endpoint to `DocumentResource`:
  - `POST /api/documents/{id}/ingest`

- [ ] Endpoint behavior:
  - Validate document exists (404 if not)
  - Invoke `ingestionService.ingestDocument(id)`
  - Return `200 OK` (MVP synchronous)

- [ ] Verify endpoint requires JWT authentication (same as other endpoints).

- [ ] Add integration test:
  - Create document
  - Call ingest endpoint
  - Assert chunks exist and embeddings stored
  - Assert idempotency behavior on repeated calls

- [ ] Build and run tests.

---

## Phase 4 — Secure and Reduce API Surface

- [ ] Update `GET /api/documents` to avoid returning full `content` by default:
  - Implement lightweight list DTO: `id`, `title`, `createdAt`
  - OR remove/disable list endpoint if not needed for MVP

- [ ] Verify all document endpoints are authenticated (JWT):
  - POST /api/documents
  - PUT /api/documents/{id}
  - PATCH /api/documents/{id}
  - GET /api/documents
  - GET /api/documents/{id}
  - DELETE /api/documents/{id}
  - POST /api/documents/{id}/ingest

- [ ] Ensure delete cleans up chunks:
  - Either cascade delete or explicit delete of `document_chunk` rows
  - Add integration test verifying no orphaned chunks after delete

- [ ] Build and run tests.

---

## Phase 5 — Final Verification

- [ ] POST document triggers ingestion and creates chunks with embeddings.
- [ ] Re-ingestion behaves deterministically and does not duplicate chunks.
- [ ] GET endpoints never trigger ingestion.
- [ ] List endpoint does not expose full content (or is removed).
- [ ] DELETE removes associated chunks.
- [ ] Application boots successfully and all tests pass.
- [ ] No unrelated modules modified.