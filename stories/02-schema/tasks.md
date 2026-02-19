# Tasks - Story 02: Schema & Persistence

- [x] Verify PostgreSQL is running with `pgvector/pgvector:pg15` using `docker-compose up -d`.
- [x] Create `backend/java/src/main/resources/config/liquibase/changelog/20260217213000_story_02_schema_updates.xml`.
- [x] Add `<changeSet>` to enable `pgvector` extension: `CREATE EXTENSION IF NOT EXISTS vector;`.
- [x] Add `<changeSet>` to add `created_at TIMESTAMPTZ DEFAULT now() NOT NULL` to `document` table.
- [x] Add `<changeSet>` to add `created_at TIMESTAMPTZ DEFAULT now() NOT NULL` to `document_chunk` table.
- [x] Add `<changeSet>` to add `embedding vector(1536)` to `document_chunk` table.
- [x] Add `<changeSet>` to create ivfflat index:

      CREATE INDEX idx_document_chunk_embedding
      ON document_chunk
      USING ivfflat (embedding vector_cosine_ops)
      WITH (lists = 100);

- [x] Register the new changelog in `backend/java/src/main/resources/config/liquibase/master.xml`.
- [x] Ensure application is not running before executing Liquibase
- [x] Run `./mvnw liquibase:update` from the `backend/java` directory.
- [x] Verify `created_at` and `embedding` columns exist in the database with correct types and defaults.
- [x] Verify `idx_document_chunk_embedding` index exists and uses `ivfflat`.
- [x] Confirm application boots successfully without Liquibase errors.
