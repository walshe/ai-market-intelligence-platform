# Story 02: Schema & Persistence

## Description

Extend the base JHipster schema to support `pgvector` and ensure persistence aligns with the architectural design defined in `ARCHITECTURE.md`.

This story establishes:

- Vector storage using pgvector
- Approximate nearest neighbor indexing (IVFFlat)
- Database-managed timestamps
- Data integrity constraints for chunked document storage

This story does NOT introduce retrieval logic, AI calls, or business logic.

---

## Requirements

- [ ] Add idempotent Liquibase migration to enable the `pgvector` extension:
  - `CREATE EXTENSION IF NOT EXISTS vector;`

- [ ] Ensure `document` table:
  - Uses existing primary key (Long ID)
  - Contains `created_at` column:
    - Type must be `TIMESTAMPTZ`
    - Must be `NOT NULL`
    - Must use `DEFAULT now()`
  - Application-level timestamp initialization (`@PrePersist`) must be removed.

- [ ] Ensure `document_chunk` table:
  - Uses existing primary key (Long ID)
  - Contains foreign key to `document(id)`
  - Contains `chunk_index` (NOT NULL)
  - Contains `chunk_text` (NOT NULL)
  - Contains `embedding_model` (nullable VARCHAR)
  - Contains `created_at`:
    - Type must be `TIMESTAMPTZ`
    - Must be `NOT NULL`
    - Must use `DEFAULT now()`
  - Must define unique constraint on `(document_id, chunk_index)`

- [ ] Add `embedding` column to `document_chunk`:
  - Type must be `vector(1536)`
  - Must be created using raw SQL in Liquibase (pgvector type support)

- [ ] Create IVFFlat index on `document_chunk.embedding`:
  - Must use `USING ivfflat`
  - Must use `vector_cosine_ops`
  - Must define a `lists` parameter appropriate for moderate-scale workloads (e.g., 50)

- [ ] Ensure schema aligns with the data model defined in `ARCHITECTURE.md`, except that IDs remain Long for v1 scope.

- [ ] Verify that the application boots successfully and Liquibase migrations run without errors.

---

## Acceptance Criteria

- `pgvector` extension is active in the database.
- `document.created_at` is:
  - `TIMESTAMPTZ`
  - `NOT NULL`
  - `DEFAULT now()`
- `document_chunk.created_at` is:
  - `TIMESTAMPTZ`
  - `NOT NULL`
  - `DEFAULT now()`
- Unique constraint exists on `(document_id, chunk_index)`.
- `document_chunk.embedding` exists and uses type `vector(1536)`.
- IVFFlat cosine index exists on `document_chunk.embedding`.
- No application-level `@PrePersist` timestamp initialization remains.
- Application starts without Liquibase errors.
