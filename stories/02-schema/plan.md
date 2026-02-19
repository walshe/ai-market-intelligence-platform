# Implementation Plan - Story 02: Schema & Persistence

## 1. Objective
Extend the base JHipster schema to support `pgvector` and the specific entities required for the AI Market Intelligence Platform, ensuring all schema changes are managed via a single atomic Liquibase migration.

## 2. Requirements Traceability
- [ ] Add Liquibase migration to enable `pgvector` extension in PostgreSQL.
- [ ] Implement database defaults for `created_at` fields in `document` and `document_chunk` tables.
- [ ] Define the `embedding` column in `document_chunk` as `vector(1536)`.
- [ ] Create a ivfflat index (cosine similarity) on the `embedding` column.
- [ ] Ensure the schema aligns with the details in `ARCHITECTURE.md`.
- [ ] Verify that the application boots and Liquibase migrations run successfully.

## 3. Schema Migration Approach
All changes for Story 02 will be implemented in a single, atomic Liquibase XML changelog file located in `backend/java/src/main/resources/config/liquibase/changelog/`.

### 3.1 Step 1: Create Migration File
- Target File: `backend/java/src/main/resources/config/liquibase/changelog/20260217213000_story_02_schema_updates.xml`.
- The migration will use `<sql>` blocks for all PostgreSQL-specific operations to ensure compatibility with `pgvector` types and performance-optimized indexes.

### 3.2 Step 2: Migration Content (Atomic ChangeSets)
Do NOT actually put everything inside a single <changeSet>.


1.  **Enable pgvector:**
    - SQL: `CREATE EXTENSION IF NOT EXISTS vector;`

2.  **Update `document` Table:**
    - SQL: `ALTER TABLE document ADD COLUMN created_at TIMESTAMPTZ DEFAULT now() NOT NULL;`

3.  **Update `document_chunk` Table:**
    - SQL: `ALTER TABLE document_chunk ADD COLUMN created_at TIMESTAMPTZ DEFAULT now() NOT NULL;`
    - SQL: `ALTER TABLE document_chunk ADD COLUMN embedding vector(1536);`
    - *Note: Using `<sql>` for the `vector` type as Liquibase does not natively support it via its column DSL.*

4.  **Create Vector Index:**
    - SQL: `CREATE INDEX idx_document_chunk_embedding
ON document_chunk
USING ivfflat (embedding vector_cosine_ops)
WITH (lists = 100);`
    - *Note: ivfflat is chosen for its maturity, lower operational complexity, and predictable behavior in moderate-scale 
    - workloads. It provides approximate nearest neighbor search with configurable list tuning and is sufficient for 
    - this demo-scale RAG system. HNSW offers potentially higher recall at scale but introduces additional memory 
    - overhead and tuning considerations that are unnecessary for the current scope.

## 4. Verification Plan
- **Pre-check:** 
    - Verify PostgreSQL service is running with the `pgvector/pgvector:pg15` image as defined in the root-level `docker-compose.yml`.
- **Execution:** 
    - Run `./mvnw liquibase:update` (from `backend/java`) or start the application to trigger automatic migration.
- **Post-check:**
    - Verify `document` table contains `created_at` with type `TIMESTAMPTZ` and default `now()`.
    - Verify `document_chunk` table contains `created_at` (`TIMESTAMPTZ`) and `embedding` (`vector(1536)`).
    - Verify index `idx_document_chunk_embedding` exists and uses the `ivfflat` access method with `vector_cosine_ops`.
    - Check Liquibase `DATABASECHANGELOG` table to ensure the story 02 changeset was applied.
