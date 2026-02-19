# Story 02: Schema & Persistence

## Description
Extend the base JHipster schema to support pgvector and the specific entities required for the AI Market Intelligence Platform.

## Requirements
- [ ] Add Liquibase migration to enable `pgvector` extension in PostgreSQL.
- [ ] Implement database defaults for `created_at` fields in `document` and `document_chunk` tables.
- [ ] Define the `embedding` column in `document_chunk` as `vector(1536)`.
- [ ] Create a HNSW or Ivfflat index (cosine similarity) on the `embedding` column.
- [ ] Ensure the schema aligns with the details in `ARCHITECTURE.md`.
- [ ] Verify that the application boots and Liquibase migrations run successfully.

## Acceptance Criteria
- `pgvector` extension is active in the database.
- `document` and `document_chunk` tables exist with correct columns and constraints.
- `document_chunk.embedding` uses the vector type.
- Vector search index is present.
- Application starts up without Liquibase errors.
