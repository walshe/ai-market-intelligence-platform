# AI Market Intelligence Platform — Master Plan

This document tracks the high-level implementation roadmap and prevents scope drift.

---

## Phase 0 — Foundation

- [ ] Create repo structure
- [ ] Add docker-compose.yml
- [ ] Add PRD.md
- [ ] Add ARCHITECTURE.md
- [ ] Add mise.toml
- [ ] Create JDL
- [ ] Generate JHipster backend
- [ ] Verify PostgreSQL dev profile
- [ ] Verify Swagger loads

---

## Phase 1 — Schema & Persistence

- [ ] Add Liquibase migration for:
  - pgvector extension
  - document.created_at DB default
  - document_chunk.created_at DB default
  - document_chunk.embedding vector(1536)
  - cosine index
- [ ] Verify app boots
- [ ] Verify schema matches architecture

---

## Phase 2 — Chunking & Embeddings

- [ ] Implement ChunkingService
- [ ] Implement EmbeddingService
- [ ] Persist chunk embeddings
- [ ] Verify embeddings stored correctly

---

## Phase 3 — Retrieval & RAG

- [ ] Implement RetrievalService
- [ ] Implement PromptBuilderService
- [ ] Implement LlmClient interface
- [ ] Implement OpenAiLlmClient
- [ ] Implement /analysis endpoint
- [ ] Verify end-to-end RAG flow

---

## Phase 4 — Cost Governance

- [ ] Create CostLog entity
- [ ] Track token usage
- [ ] Persist cost records
- [ ] Implement /metrics endpoint

---

## Phase 5 — Production Polish

- [ ] Structured logging
- [ ] Docker validation
- [ ] README finalization
- [ ] Testcontainers integration
- [ ] Record demo walkthrough

---

## Non-Goals (Locked)

- No multi-tenancy
- No UI
- No backtesting
- No microservices
- No fine-tuning
