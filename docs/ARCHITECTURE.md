# AI Market Intelligence Platform — Architecture Overview

## Architectural Style

The system is implemented as a modular monolith using Spring Boot (JHipster-generated).

Rationale:
- Faster iteration than microservices
- Reduced operational complexity
- Clear separation of concerns within service layer
- Appropriate for prototype scope
- Easier local development and testing

Microservices were intentionally avoided in v1 to reduce orchestration overhead.

---

## High-Level Architecture

                ┌────────────────────────────┐
                │        REST Layer          │
                │  (JWT Authenticated APIs)  │
                └──────────────┬─────────────┘
                               │
                ┌──────────────┴─────────────┐
                │        Service Layer        │
                │                             │
                │  IngestionService           │
                │  ChunkingService            │
                │  EmbeddingService           │
                │  RetrievalService           │
                │  PromptBuilderService       │
                │  LlmClient (interface)      │
                │  OpenAiLlmClient            │
                │  CostTrackingService        │
                └──────────────┬─────────────┘
                               │
                ┌──────────────┴─────────────┐
                │      Persistence Layer      │
                │                             │
                │  document                   │
                │  document_chunk             │
                │  (pgvector cosine index)    │
                └──────────────┬─────────────┘
                               │
                ┌──────────────┴─────────────┐
                │     PostgreSQL + pgvector  │
                └────────────────────────────┘

---

## Core Data Model

### document
- id (UUID)
- title
- content
- created_at (DB default)

### document_chunk
- id (UUID)
- document_id (FK)
- chunk_index
- chunk_text
- embedding (vector(1536))
- embedding_model
- created_at (DB default)

Embeddings are stored per chunk to improve retrieval granularity and reduce prompt token overhead.

Note: v1 uses Long identifiers due to single-database modular monolith scope.

---

## Retrieval-Augmented Generation (RAG) Flow

1. User submits analysis query.
2. Query embedding generated.
3. Top-k document_chunk rows retrieved using cosine similarity.
4. PromptBuilder constructs contextual prompt.
5. LLM inference executed.
6. Token usage recorded.
7. Structured response returned.

---

## Design Tradeoffs

- **Modular monolith** chosen for development velocity and clarity.
- **PostgreSQL + pgvector** chosen over Milvus to reduce infrastructure complexity.
- **Chunk-level embeddings** for retrieval precision and realistic RAG implementation.
- **UUID identifiers** for distributed-system friendliness.
- **DB-managed timestamps** for consistency and reduced application-layer duplication.
- **Provider abstraction layer** to avoid vendor lock-in.
- **Cost tracking** implemented to reflect real-world AI operational constraints.

---

## Non-Goals (v1)

- No multi-tenancy
- No predictive trading engine
- No backtesting
- No UI frontend
- No microservice split
- No fine-tuning of models
