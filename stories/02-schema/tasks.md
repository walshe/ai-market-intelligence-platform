# Tasks - Story 02: Schema & Persistence

## Phase 1 — Enable pgvector

- [ ] Create new Liquibase changelog file:
      `YYYYMMDDHHMMSS_story_02_schema_updates.xml`

- [ ] Add changeSet to execute:
      `CREATE EXTENSION IF NOT EXISTS vector;`

- [ ] Verify Liquibase file is correctly registered in master changelog.

---

## Phase 2 — Enforce Database-Managed Timestamps

- [ ] Add changeSet to alter `document.created_at`:
      - Ensure type is `TIMESTAMPTZ`
      - Set `NOT NULL`
      - Set `DEFAULT now()`

- [ ] Add changeSet to alter `document_chunk.created_at`:
      - Ensure type is `TIMESTAMPTZ`
      - Set `NOT NULL`
      - Set `DEFAULT now()`

- [ ] Remove any `@PrePersist` logic initializing `createdAt` in:
      - `Document`
      - `DocumentChunk`

- [ ] Confirm entity fields retain:
      `@Column(nullable = false, updatable = false)`

---

## Phase 3 — Add Data Integrity Constraint

- [ ] Add changeSet to create unique constraint:
      `(document_id, chunk_index)`

- [ ] Name constraint:
      `uq_document_chunk_doc_index`

- [ ] Verify constraint exists in database.

---

## Phase 4 — Add Embedding Column

- [ ] Add changeSet using raw SQL to add column:
      `embedding vector(1536)` to `document_chunk`

- [ ] Ensure column is nullable for v1.

- [ ] Verify column exists with correct type in database.

---

## Phase 5 — Create IVFFlat Cosine Index

- [ ] Add changeSet using raw SQL to create index:
      `idx_document_chunk_embedding`

- [ ] Ensure index definition:
      - Uses `USING ivfflat`
      - Uses `vector_cosine_ops`
      - Uses `WITH (lists = 50)`

- [ ] Verify index exists and uses IVFFlat.

---

## Final Verification

- [ ] Run `./mvnw liquibase:update` OR start application.
- [ ] Confirm application boots without Liquibase errors.
- [ ] Confirm no unrelated modules were modified.
- [ ] Confirm schema matches requirements exactly.
