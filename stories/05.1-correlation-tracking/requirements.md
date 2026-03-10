# Story 5.1: Correlation Tracking for AI Operations

## Description

Introduce correlation ID propagation across AI pipeline operations to allow grouping of related LLM calls within a single request lifecycle.

This enables traceability of costs and model usage across multi-step AI workflows such as Retrieval-Augmented Generation (RAG), where a single user request may involve multiple LLM invocations.

The correlation ID will allow linking:

- Query embedding generation
- Vector search operations
- Final completion generation
- Associated cost tracking entries

This improves observability, debugging capability, and cost attribution per request.

---

# Architectural Context

The `/analysis` workflow currently performs multiple AI operations:

1. Query embedding generation
2. Vector similarity search
3. Final LLM completion

Each of these operations may generate separate `CostLog` entries. Without correlation tracking, these entries cannot be reliably grouped into a single logical request.

Introducing a correlation ID enables the system to associate multiple cost events with the same request lifecycle.

---

# Requirements

## A) Correlation ID Generation

- [ ] Generate a unique correlation ID for each `/analysis` request.

Requirements:

- Must be globally unique.
- UUID is acceptable.
- Generated at the beginning of the request lifecycle.

---

## B) Correlation ID Propagation

- [ ] Pass the correlation ID through the analysis workflow.

The correlation ID must be available to:

- Query embedding generation
- Completion generation
- Cost tracking service

Propagation may occur via:

- method parameters
- request-scoped context
- thread-local context (acceptable for MVP)

---

## C) CostLog Integration

- [ ] Populate the `correlationId` field in `CostLog` entries when available.

Specifically:

| Operation | callType | correlationId |
|----------|----------|---------------|
| Query embedding | EMBEDDING | populated |
| Completion call | COMPLETION | populated |
| Document ingestion embedding | EMBEDDING | null (not request scoped) |

Document ingestion may omit correlation IDs since it is not tied to a user analysis request.

---

## D) Logging Integration

- [ ] Include the correlation ID in application logs for the `/analysis` workflow.

This allows:

- tracing request execution
- correlating logs with cost records

Example log:

```

analysis request started correlationId=abc123

```

---

## E) Metrics Endpoint Enhancement

- [ ] Extend the cost metrics endpoint to optionally filter by correlation ID.

Example:

```

GET /api/v1/metrics/cost?correlationId=abc123

```

Expected behavior:

- Return cost entries associated with the specified correlation ID.

This feature is optional for the MVP if it significantly increases implementation complexity.

---

# Non-Goals

This story does **not** include:

- distributed tracing
- OpenTelemetry integration
- cross-service propagation
- request budgeting or rate limiting

These may be addressed in future observability stories.

---

# Acceptance Criteria

- Each `/analysis` request generates a unique correlation ID.
- The correlation ID is propagated through the AI processing pipeline.
- CostLog entries for query embedding and completion calls contain the correlation ID.
- Document ingestion cost logs do not require correlation IDs.
- Logs include the correlation ID for traceability.
- System behavior remains unchanged aside from improved observability.
- Application boots successfully and all tests pass.
