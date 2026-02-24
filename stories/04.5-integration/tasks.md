# Tasks - Story 04.5: Integrate IngestionService with DocumentResource

## Phase 1 — Wire Ingestion to Document Creation (Completed)

- [x] Inject `IngestionService` into `DocumentResource`.

- [x] Update `createDocument()` flow:
  - Persist document via `DocumentService.save(documentDTO)`
  - After successful save, call `ingestionService.ingestDocument(savedId)`
  - Keep controller logic minimal (no chunking logic in controller)

- [x] Verify manual DB behavior after POST:
  - `document` row is created
  - `document_chunk` rows are created
  - `embedding` is non-null for chunks

- [x] Remove manual `createdAt` assignment from ingestion flow:
  - Do not set `DocumentChunk.createdAt` in code
  - Rely on DB `DEFAULT now()`

- [x] Build and run application; confirm no errors.

---

## Phase 2 — Enforce Idempotent Ingestion (Completed)

- [x] Implement idempotency strategy in `IngestionService`:

  Strategy chosen:
  - Delete existing `document_chunk` rows for a document before re-ingesting

- [x] Ensure deletion happens before chunk recreation.

- [x] Add logs for ingestion lifecycle:
  - document id
  - chunk count produced
  - embedding model used
  - skip condition (empty content)

- [x] Add test verifying idempotency:
  - Repeated ingestion does not create duplicates

- [x] Build and run tests.

---

## Phase 3 — Update Behavior Policy (MVP Simplification) (Completed)

Goal: Avoid accidental re-embedding and cost amplification.

- [x] Do NOT trigger ingestion on:
  - PUT /api/documents/{id}
  - PATCH /api/documents/{id}

- [x] Document this behavior clearly in code comments:
  - Updates modify stored content only.
  - Re-ingestion requires explicit internal invocation (if ever needed).

- [x] Add test verifying:
  - Updating a document does NOT create new chunks automatically.

- [x] Confirm repeated updates do not affect `document_chunk`.

---

## Phase 4 — Secure and Reduce API Surface

- [x] Update `GET /api/documents` to avoid returning full `content` by default:
  - Implement lightweight list DTO: `id`, `title`, `createdAt`
  - Exclude `content`

- [x] Ensure all document endpoints require JWT authentication:
  - POST /api/documents
  - PUT /api/documents/{id}
  - PATCH /api/documents/{id}
  - GET /api/documents
  - GET /api/documents/{id}
  - DELETE /api/documents/{id}

- [x] Ensure delete cleans up chunks:
  - Cascade delete OR explicit delete of `document_chunk`
  - Add integration test verifying no orphaned chunks

- [x] Build and run tests.

---

## Phase 5 — Final Verification

- [x] POST document triggers ingestion and creates chunks with embeddings.
- [x] Repeated ingestion (internal call) behaves deterministically.
- [x] PUT/PATCH do NOT re-trigger ingestion.
- [x] GET endpoints never trigger ingestion.
- [x] List endpoint does not expose full content.
- [x] DELETE removes associated chunks.
- [ ] Application boots successfully and all tests pass.
- [x] No unrelated modules modified.