# Story 03: Chunking & Embeddings

## Description
Implement the services responsible for breaking down documents into chunks and generating vector embeddings for each chunk.

## Requirements
- [ ] Implement `ChunkingService` to split documents into manageable segments (sentence-aware).
- [ ] Implement `EmbeddingService` to interface with an LLM provider (e.g., OpenAI) for generating embeddings.
- [ ] Ensure `document_chunk` records are persisted with their respective embeddings.
- [ ] Implement deterministic and simple chunking logic as per `PRD.md`.
- [ ] Verify that embeddings are stored correctly in the database.

## Acceptance Criteria
- Documents are successfully split into chunks upon ingestion.
- Embeddings are generated for each chunk using the configured model.
- Each `document_chunk` has a corresponding vector in the database.
- Unit tests verify the chunking logic and embedding service integration (mocked where appropriate).
