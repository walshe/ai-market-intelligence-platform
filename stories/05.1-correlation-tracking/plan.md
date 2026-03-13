
# Plan - Story 5.1: Correlation Tracking for AI Operations

## Objective

Introduce correlation ID tracking across the `/analysis` request lifecycle to allow grouping of related AI operations and cost tracking entries.

The correlation ID will link:

- query embedding generation
- vector search execution
- completion generation
- cost tracking entries

This improves observability and enables tracing of all LLM activity associated with a single analysis request.

---

# Phase 1 — Introduce Correlation ID Generation

## Goal

Generate a unique correlation ID at the start of each `/analysis` request.

### Implementation

- Generate a UUID when `/analysis` endpoint is invoked.
- The correlation ID must represent the lifecycle of the entire analysis request.

Example:

```

UUID correlationId = UUID.randomUUID().toString();

```

### Integration Point

Correlation ID generation should occur in:

```

AnalysisResource

```

or whichever controller handles `/analysis`.

### Verification

- `/analysis` requests produce a correlation ID.
- Correlation ID is visible in logs.

---

# Phase 2 — Propagate Correlation ID Through Workflow

## Goal

Ensure correlation ID is available across the full analysis pipeline.

### Approach

For the MVP, propagate correlation ID using **method parameters**.

This keeps the implementation explicit and avoids hidden thread-local complexity.

### Propagation Path

```

AnalysisResource
↓
AnalysisService
↓
EmbeddingService
↓
CompletionService
↓
CostTrackingService

```

Each layer receives:

```

String correlationId

```

### Verification

- Correlation ID flows from controller through all AI operations.

---

# Phase 3 — Integrate Correlation ID into Cost Tracking

## Goal

Attach correlation IDs to cost records associated with analysis operations.

### Implementation

Update:

```

CostTrackingService

```

Methods:

```

logEmbeddingUsage(modelName, inputTokens, documentId, correlationId)

logCompletionUsage(modelName, inputTokens, outputTokens, correlationId)

```

### Behavior

For `/analysis` requests:

| Operation | correlationId |
|----------|---------------|
| Query embedding | populated |
| Completion | populated |

For ingestion operations:

| Operation | correlationId |
|----------|---------------|
| Document chunk embedding | null |

### Verification

- `/analysis` generates multiple CostLog entries sharing the same correlationId.
- Ingestion CostLog entries contain `null` correlationId.

---

# Phase 4 — Logging Integration

## Goal

Improve observability by including correlation IDs in logs.

### Implementation

Add structured logs to:

```

AnalysisService

```

Example:

```

analysis request started correlationId=abc123

```

Optional logs:

```

query embedding started correlationId=abc123
completion call started correlationId=abc123
analysis request completed correlationId=abc123

```

### Verification

- Logs show correlation ID for all major analysis steps.

---

# Phase 5 — Optional Metrics Endpoint Enhancement

## Goal

Allow cost metrics to be filtered by correlation ID.

### Endpoint

Extend existing endpoint:

```

GET /api/v1/metrics/cost

```

Optional query parameter:

```

correlationId

```

Example:

```

GET /api/v1/metrics/cost?correlationId=abc123

```

### Behavior

If correlationId provided:

- return cost records matching that correlationId.

If absent:

- return normal aggregated metrics.

### Repository

Add repository method:

```

findByCorrelationId(String correlationId)

```

### Verification

- Endpoint returns filtered results when correlationId is supplied.

---

# Phase 6 — Testing

## Unit Tests

Test:

- correlation ID generation
- propagation through services
- CostTrackingService stores correlationId correctly

## Integration Tests

Test `/analysis` workflow:

Verify:

- query embedding CostLog contains correlationId
- completion CostLog contains same correlationId
- multiple CostLog entries share the same correlationId

Verify ingestion:

- ingestion CostLog entries have null correlationId.

---

# Phase 7 — Final Verification

Confirm system behavior:

- `/analysis` generates a unique correlation ID.
- Correlation ID is propagated through AI pipeline.
- CostLog entries for analysis share the same correlationId.
- Ingestion operations do not use correlationId.
- Logs include correlation IDs for traceability.
- Metrics endpoint optionally filters by correlationId.
- Application builds successfully and tests pass.
