# Story 05: Cost Governance

## Description

Implement a cost governance mechanism to track and persist usage metrics for all LLM invocations within the system.

This includes:

- Embedding calls during document ingestion
- Embedding calls during query processing
- Completion calls during final RAG response generation

The goal is to provide financial accountability, model-level visibility, and operational observability without impacting primary system responsiveness.

---

## Architectural Context

The system performs multiple LLM invocations across different workflows:

1. Document ingestion → multiple EMBEDDING calls (per chunk)
2. Query processing → one EMBEDDING call (query embedding)
3. Final RAG generation → one COMPLETION call

Cost tracking must consistently capture usage across all invocation types.

---

## Requirements

### A) CostLog Entity

- [ ] Create `CostLog` entity with the following fields:

  - id (PK)
  - callType (ENUM: EMBEDDING | COMPLETION)
  - modelName (string)
  - inputTokens (integer)
  - outputTokens (integer, nullable for embeddings)
  - totalTokens (integer)
  - estimatedUsdCost (decimal)
  - documentId (nullable, used for ingestion tracking)
  - correlationId (nullable, used to group analysis request calls)
  - createdAt (DB-managed timestamp)

- [ ] Ensure `created_at` uses DB `DEFAULT now()`.

---

### B) Pricing Configuration

- [ ] Implement model pricing configuration:

  - Pricing must be configurable per model.
  - Support:
    - input token cost per 1K tokens
    - output token cost per 1K tokens
    - embedding token cost per 1K tokens

- [ ] Pricing configuration must be externalized (application.yml).

---

### C) CostTrackingService

- [ ] Implement `CostTrackingService` responsible for:

  - Calculating estimated USD cost based on:
    - modelName
    - token usage
    - pricing configuration

  - Persisting `CostLog` entries

- [ ] CostTrackingService must not contain business logic.
- [ ] It must be reusable across:
  - EmbeddingService
  - LlmClient (completion)

---

### D) LLM Call Integration

- [ ] All LLM invocations must be wrapped or intercepted to ensure cost tracking is consistent.

  Specifically:

  - EmbeddingService:
    - Record a CostLog entry for each embedding call
    - callType = EMBEDDING

  - LlmClient / CompletionService:
    - Record a CostLog entry for each completion call
    - callType = COMPLETION

- [ ] Tracking must capture token usage directly from provider response where available.

- [ ] Tracking must occur after successful provider response.

---

### E) Metrics Endpoint

- [ ] Implement `GET /api/v1/metrics/cost`

The endpoint must provide:

- Total USD cost (all time)
- Cost grouped by:
  - modelName
  - callType (EMBEDDING vs COMPLETION)
- Optional time window filtering (if simple to implement)

Example response:

```json
{
  "totalUsd": 12.34,
  "byModel": {
    "gpt-4o-mini": 10.20,
    "text-embedding-3-small": 2.14
  },
  "byCallType": {
    "COMPLETION": 9.87,
    "EMBEDDING": 2.47
  }
}
````

---

### F) Resilience & Failure Handling

* [ ] Cost tracking must not block primary workflows.

If cost persistence fails:

* Log error
* Do not fail ingestion
* Do not fail analysis request

Tracking should be best-effort in MVP.

---

### G) Future Extension (Out of Scope for This Story)

* Asynchronous cost logging via:

  * Event publication
  * Kafka
  * ApplicationEvent
  * Queue-based persistence

* Correlation-based aggregation dashboards

* Per-user cost tracking

* Budget enforcement

These enhancements may be implemented in a later story.

---

## Acceptance Criteria

* Every embedding call results in a CostLog entry.
* Every completion call results in a CostLog entry.
* A single `/analysis` request results in:

  * One EMBEDDING CostLog (query embedding)
  * One COMPLETION CostLog
* Document ingestion results in:

  * N EMBEDDING CostLog entries (one per chunk)
* `GET /api/v1/metrics/cost` returns accurate aggregated cost information.
* Cost tracking failures do not prevent system operation.
* Application boots successfully and all tests pass.

