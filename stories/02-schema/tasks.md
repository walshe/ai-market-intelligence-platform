# Tasks - Story 02: Schema & Persistence

## Phase 1 — Enable pgvector

- [x] Create new Liquibase changelog file:
      `YYYYMMDDHHMMSS_story_02_schema_updates.xml`

- [x] Add changeSet to execute:
      `CREATE EXTENSION IF NOT EXISTS vector;`

- [x] Verify Liquibase file is correctly registered in master changelog.

---

## Phase 2 — Enforce Database-Managed Timestamps
- [x] Add changeSet to alter `document.created_at`:
      - Ensure type is `TIMESTAMPTZ`
      - Set `NOT NULL`
      - Set `DEFAULT now()`
- [x] Add changeSet to alter `document_chunk.created_at`:
      - Ensure type is `TIMESTAMPTZ`
      - Set `NOT NULL`
      - Set `DEFAULT now()`
- [x] Remove any `@PrePersist` logic initializing `createdAt` in:
      - `Document`
      - `DocumentChunk`
- [x] Confirm entity fields retain:
      `@Column(nullable = false, updatable = false)`
---
## Phase 3 — Add Data Integrity Constraint
- [x] Add changeSet to create unique constraint:
      `(document_id, chunk_index)`
- [x] Name constraint:
      `uq_document_chunk_doc_index`
- [x] Verify constraint exists in database.

---

## Phase 4 — Add Embedding Column

- [x] Add changeSet using raw SQL to add column:
      `embedding vector(1536)` to `document_chunk`

- [x] Ensure column is nullable for v1.

- [x] Verify column exists with correct type in database.

---

## Phase 5 — Create IVFFlat Cosine Index

- [x] Add changeSet using raw SQL to create index:
      `idx_document_chunk_embedding`

- [x] Ensure index definition:
      - Uses `USING ivfflat`
      - Uses `vector_cosine_ops`
      - Uses `WITH (lists = 50)`

- [x] Verify index exists and uses IVFFlat.

---

## Final Verification

- [x] Run `./mvnw liquibase:update` OR start application.
- [x] Confirm application boots without Liquibase errors.
- [x] Confirm no unrelated modules were modified.
- [x] Confirm schema matches requirements exactly.
