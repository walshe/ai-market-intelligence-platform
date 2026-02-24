# Implementation Plan - Story 04.5: Integrate IngestionService with DocumentResource

## 1. Objective

Integrate the existing `IngestionService` into the `DocumentResource` lifecycle so that document creation (and optionally explicit re-ingestion) results in deterministic chunking and embedding persistence.

This story also tightens endpoint behavior to:

- Prevent accidental re-ingestion and cost amplification
- Reduce API surface exposure of sensitive content
- Ensure ingestion is explicit, secure, and predictable

---

## 2. Requirements Traceability

This plan implements:

- Automatic ingestion on `POST /api/documents`
- Optional explicit ingestion endpoint
- Idempotent ingestion behavior
- Proper transactional boundaries
- Security hardening of document endpoints
- Logging and observability of ingestion lifecycle
- Integration tests verifying ingestion wiring

---

## 3. Architectural Alignment

The integration must preserve clean boundaries:

- `DocumentResource` → HTTP boundary only
- `DocumentService` → persistence logic
- `IngestionService` → orchestration of:
  - chunking
  - embedding
  - chunk persistence

Controller must not contain ingestion logic.

Ingestion must remain isolated in `IngestionService`.

---

## 4. Implementation Phases

---

### Phase 1 — Wire Ingestion to Document Creation

**Goal:** Ensure newly created documents are automatically ingested.

#### Steps:

1. Modify `DocumentResource.createDocument()`:
   - Persist document via `DocumentService.save()`
   - After successful persistence, invoke:
     `ingestionService.ingestDocument(documentDTO.getId())`

2. Ensure ingestion runs inside a transactional boundary.

3. Remove any manual setting of `createdAt` in `DocumentChunk`.
   - Let database `DEFAULT now()` handle timestamps.

4. Verify:
   - A document POST results in:
     - 1 `document`
     - N `document_chunk` rows
     - Non-null embeddings

#### Deliverables:

- Updated `DocumentResource`
- Verified ingestion invocation
- Manual DB verification
- Build passes

---

### Phase 2 — Enforce Idempotent Ingestion

**Goal:** Prevent duplicate chunks and accidental cost multiplication.

#### Strategy (choose one and implement consistently):

**Option A (Recommended for MVP):**
- Before ingesting:
  - Delete existing `document_chunk` rows for that document.

OR

**Option B:**
- If chunks already exist:
  - Skip ingestion and log event.

#### Steps:

1. Add logic in `IngestionService.ingestDocument()`:
   - Check for existing chunks.
   - Apply chosen strategy.

2. Add debug logging:
   - Document id
   - Number of chunks created
   - Embedding model used

#### Deliverables:

- Idempotent ingestion behavior
- Verified no duplicate chunks on repeated ingestion
- Unit/integration test verifying behavior

---

### Phase 3 — Add Explicit Ingestion Endpoint

**Goal:** Provide controlled re-ingestion capability.

#### Endpoint:

`POST /api/documents/{id}/ingest`

#### Behavior:

1. Validate document exists.
2. Invoke `ingestionService.ingestDocument(id)`.
3. Return:
   - `200 OK` (synchronous ingestion)
   OR
   - `202 Accepted` (if future async planned; MVP can use 200).

4. Must require JWT authentication.

#### Deliverables:

- New endpoint in `DocumentResource`
- Auth enforced
- Integration test verifying endpoint behavior

---

### Phase 4 — Secure and Reduce API Surface

**Goal:** Prevent excessive data exposure.

#### Steps:

1. Review `GET /api/documents`:
   - Modify to return lightweight DTO:
     - id
     - title
     - createdAt
   - Exclude full `content`.

OR

2. Remove list endpoint if not required for MVP demo.

3. Ensure all document endpoints require authentication:
   - POST
   - PUT
   - PATCH
   - GET
   - DELETE
   - ingest endpoint

4. Ensure:
   - `DELETE /api/documents/{id}` also removes related chunks
     (via cascade or explicit delete).

#### Deliverables:

- Reduced document exposure
- Verified auth on all endpoints
- No orphaned chunk rows

---

### Phase 5 — Observability & Logging

**Goal:** Make ingestion transparent and debuggable.

#### Steps:

Add structured logs for:

- Document id
- Chunk count
- Embedding model
- Skip conditions (empty content, already ingested)

Ensure logs are informative but not verbose.

---

### Phase 6 — Integration Testing

**Goal:** Validate full ingestion wiring.

#### Tests:

1. POST document:
   - Assert document persisted.
   - Assert chunk rows created.
   - Assert embeddings non-null.

2. POST `/documents/{id}/ingest`:
   - Assert idempotent behavior.
   - Assert no duplicate chunks.

3. DELETE document:
   - Assert chunk rows removed.

4. Confirm no ingestion triggered by:
   - GET endpoints
   - DELETE

#### Deliverables:

- Integration test suite covering ingestion wiring
- All tests pass
- Application boots successfully

---

## 5. Non-Goals

- No async ingestion queue
- No batch embedding optimization
- No cost aggregation enhancements
- No ownership-based multi-tenancy
- No UI changes

---

## 6. Completion Criteria

Story 04.5 is complete when:

- Document creation automatically triggers ingestion.
- Re-ingestion is explicit and controlled.
- No duplicate chunks are created.
- Ingestion is transactional and logged.
- API surface is hardened and authenticated.
- Integration tests pass.
- Application builds and runs cleanly.