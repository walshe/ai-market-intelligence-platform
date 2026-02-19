# Implementation Plan - Story 02: Schema & Persistence

## 1. Objective

Extend the base JHipster schema to support `pgvector` and ensure persistence aligns with the architectural design.

This story introduces:

- pgvector extension
- Database-managed timestamps (`DEFAULT now()`)
- Vector storage (`vector(1536)`)
- IVFFlat cosine index
- Data integrity constraint on `(document_id, chunk_index)`

This story does NOT introduce retrieval logic, AI services, or application-layer business logic.

---

## 2. Requirements Traceability

This plan implements the following requirements:

- Enable pgvector extension.
- Enforce DB-managed timestamps.
- Add `document_chunk.embedding` as `vector(1536)`.
- Create an IVFFlat cosine index on `document_chunk.embedding`.
- Add unique constraint on `(document_id, chunk_index)`.
- Verify application boots without Liquibase errors.

---

## 3. Migration Strategy

All database changes will be implemented in a single new Liquibase changelog file containing multiple ordered changeSets.

- Target directory:
  `backend/v2/src/main/resources/config/liquibase/changelog/`

- File naming convention:
  `YYYYMMDDHHMMSS_story_02_schema_updates.xml`

Rules:

- Each logical change must be implemented as its own `<changeSet>`.
- PostgreSQL-specific features (pgvector type, IVFFlat index) must be implemented using `<sql>` blocks.
- ChangeSets must be ordered to ensure dependencies apply cleanly.

---

## 4. Implementation Phases

### Phase 1 — Enable pgvector

Goal: Activate vector support in PostgreSQL.

ChangeSet:
- Execute: `CREATE EXTENSION IF NOT EXISTS vector;`

Constraints:
- Must be idempotent.
- Must not fail if extension already exists.

---

### Phase 2 — Enforce Database-Managed Timestamps

Goal: Move timestamp authority from application layer to database.

DB Steps (Liquibase):
- Alter `document.created_at`:
  - Ensure type is `TIMESTAMPTZ` (only if currently different)
  - Set `NOT NULL`
  - Set `DEFAULT now()`

- Alter `document_chunk.created_at`:
  - Ensure type is `TIMESTAMPTZ` (only if currently different)
  - Set `NOT NULL`
  - Set `DEFAULT now()`

Application Steps:
- Remove any `@PrePersist` timestamp initialization logic for `createdAt`.
- Retain entity mapping with `updatable = false`.

---

### Phase 3 — Add Data Integrity Constraint

Goal: Prevent duplicate chunk indexes per document.

ChangeSet:
- Add unique constraint on `(document_id, chunk_index)`.

Constraint name:
- `uq_document_chunk_doc_index`

---

### Phase 4 — Add Embedding Column

Goal: Enable vector storage for chunk embeddings.

ChangeSet:
- Add column on `document_chunk`:
  - `embedding vector(1536)`

Implementation notes:
- Must use raw SQL in Liquibase (pgvector type support).
- Column may be nullable for v1 (embeddings are populated in later stories).

---

### Phase 5 — Create IVFFlat Cosine Index

Goal: Enable approximate nearest neighbor search on embeddings.

ChangeSet:
- Create index `idx_document_chunk_embedding` using IVFFlat cosine ops.

Index definition (SQL):

    CREATE INDEX idx_document_chunk_embedding
    ON document_chunk
    USING ivfflat (embedding vector_cosine_ops)
    WITH (lists = 50);

Constraints:
- Must use `vector_cosine_ops`.
- Must use `USING ivfflat`.
- `lists` is fixed to 50 for moderate-scale workload.
- Must not modify unrelated indexes.

---

## 5. Verification Plan

### Pre-check
- PostgreSQL is running.
- Docker image includes pgvector support.

### Execution
- Run `./mvnw liquibase:update` from `backend/v2`, OR start the application to trigger automatic migration.

### Post-check
Verify:

- pgvector extension is installed.
- `document.created_at` is `TIMESTAMPTZ NOT NULL DEFAULT now()`.
- `document_chunk.created_at` is `TIMESTAMPTZ NOT NULL DEFAULT now()`.
- Unique constraint exists on `(document_id, chunk_index)`.
- `document_chunk.embedding` exists with type `vector(1536)`.
- `idx_document_chunk_embedding` exists and uses IVFFlat with `vector_cosine_ops`.
- Application boots without Liquibase errors.
- No `@PrePersist` timestamp logic remains.

---

## 6. Completion Criteria

Story 02 is complete when:

- All changeSets execute successfully.
- Schema matches the requirements exactly.
- Application builds and starts.
- No unrelated modules were modified.
- No cross-story refactoring was introduced.
