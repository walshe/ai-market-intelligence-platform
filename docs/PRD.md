1. Overview

The AI Market Intelligence Platform is a backend service that integrates structured market data and unstructured textual data using Retrieval-Augmented Generation (RAG) patterns to provide AI-assisted decision support for financial analysis.

The system demonstrates:

LLM integration within a secure backend

Cost-aware AI inference

Observability and governance

Production-style architecture using Spring Boot and PostgreSQL

This is a prototype focused on architectural patterns rather than predictive accuracy.

2. Goals
Primary Goals

Demonstrate event-driven or structured ingestion of financial documents/data

Implement RAG-based contextual query answering

Track token usage and estimated cost per request

Provide authenticated API endpoints

Containerize and document deployment

Non-Goals (Important)

No predictive trading engine

No backtesting engine

No UI-heavy frontend

No multi-tenancy

No fine-tuned model training

This prevents you from drifting.

3. Core Features (v1 Scope)
3.1 Authentication

JWT-based auth

Roles: ADMIN, USER

Per-user query tracking

3.2 Document Ingestion

Upload textual financial content (news, reports)

Store metadata

Generate embeddings

Persist to pgvector

3.3 Structured Data Storage

Store basic OHLC market data (optional v1.1)

3.4 RAG Query Endpoint

POST /analysis

Flow:

Authenticate user

Retrieve top-k relevant documents

Construct prompt

Call LLM

Return structured response

Response format:

{
  "summary": "...",
  "riskFactors": ["..."],
  "confidenceScore": 0.0-1.0,
  "modelUsed": "...",
  "tokensUsed": ...
}

3.5 Cost Governance

Track per request:

Input tokens

Output tokens

Estimated USD cost

Model used

Timestamp

Expose:

GET /metrics/cost

3.6 Observability

Structured logging

Latency tracking

Error tracking

Cost aggregation per day

4. Architecture
Application Type

Modular monolith (Spring Boot)

Components

Ingestion Controller

Query Controller

LLM Wrapper Service

Embedding Service

Retrieval Service

Cost Tracking Service

Security Layer (JWT)

Persistence Layer (Postgres + pgvector)

Optional v1.1:

Kafka ingestion

5. Technical Stack

Java 17+

Spring Boot (JHipster-generated)

PostgreSQL + pgvector

OpenAI or Anthropic API

Docker

Micrometer (optional)

Swagger/OpenAPI

6. Definition of Done (Critical)

Project is considered complete when:

Authenticated user can upload document

Document embeddings are stored

User can query contextual analysis

LLM response is structured

Token usage and cost are logged

Docker setup runs system locally

README explains architecture and tradeoffs
