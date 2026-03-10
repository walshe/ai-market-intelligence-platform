# Plan - Story 05: Cost Governance

## Objective

Introduce a system-wide mechanism to track LLM usage and estimated cost across:

- Document ingestion (embedding generation)
- Query processing (query embedding)
- Final analysis response (completion)

The implementation will provide persistent cost logs and a metrics endpoint for governance.

---

# Phase 1 — Introduce CostLog Data Model

## Goal
Create the persistence layer required for cost tracking.

### Tasks

- Create `CostLog` entity.

Fields:

- id (Long, PK)
- callType (ENUM: EMBEDDING | COMPLETION)
- modelName (String)
- inputTokens (Integer)
- outputTokens (Integer, nullable for embeddings)
- totalTokens (Integer)
- estimatedUsdCost (BigDecimal)
- documentId (Long, nullable)
- correlationId (String, nullable)
- createdAt (Instant, DB default)

### Database

- Create corresponding table `cost_log`.

Important:

- `created_at` must use DB `DEFAULT now()`.
- Index on:
  - `model_name`
  - `call_type`
  - `created_at`

### Repository

Create:

```

CostLogRepository

```

Extending:

```

JpaRepository<CostLog, Long>

````

Add aggregation queries required for metrics endpoint.

### Verification

- Application boots
- Table created successfully
- Repository loads correctly

---

# Phase 2 — Pricing Configuration

## Goal
Externalize model pricing so costs can be calculated dynamically.

### Tasks

Create configuration structure in `application.yml`.

Example:

```yaml
ai:
  pricing:
    models:
      text-embedding-3-small:
        embeddingCostPer1kTokens: 0.00002
      gpt-4o-mini:
        inputCostPer1kTokens: 0.00015
        outputCostPer1kTokens: 0.00060
````

### Implementation

Create configuration binding class:

```
AiPricingProperties
```

Responsibilities:

* Load model pricing
* Provide lookup by model name
* Provide pricing for:

    * embedding calls
    * completion calls

### Verification

* Config loads correctly
* Model pricing accessible via service

---

# Phase 3 — Implement CostTrackingService

## Goal

Centralize cost calculation and persistence.

### Responsibilities

`CostTrackingService` must:

* Calculate estimated USD cost
* Persist CostLog entries
* Be reusable across LLM invocation services

### Methods

Example methods:

```
logEmbeddingUsage(modelName, inputTokens, documentId, correlationId)

logCompletionUsage(modelName, inputTokens, outputTokens, correlationId)
```

### Cost Calculation

Use pricing configuration:

```
cost = (tokens / 1000) * pricePer1k
```

Completion cost:

```
inputCost + outputCost
```

Embedding cost:

```
inputCost only
```

### Failure Handling

If persistence fails:

* Log error
* Do not propagate exception to caller

### Verification

* Unit tests validate:

    * correct cost calculation
    * correct persistence

---

# Phase 4 — Integrate with EmbeddingService

## Goal

Ensure all embedding calls are tracked.

### Tasks

Modify `EmbeddingService`:

After successful embedding response:

* Extract token usage if available
* Call:

```
costTrackingService.logEmbeddingUsage(...)
```

### Context

During:

* document ingestion → pass `documentId`
* query embedding → `documentId = null`

Optionally generate `correlationId` for analysis requests.

### Verification

* Embedding call creates CostLog entry
* ingestion produces N CostLog rows (one per chunk)

---

# Phase 5 — Integrate with Completion Service

## Goal

Track final LLM responses used for analysis.

### Tasks

Modify LLM completion client / service.

After successful completion response:

* Extract:

    * input tokens
    * output tokens
    * model name

Call:

```
costTrackingService.logCompletionUsage(...)
```

Include correlationId if available.

### Verification

Running `/analysis` produces:

* 1 embedding CostLog
* 1 completion CostLog

---

# Phase 6 — Implement Metrics Endpoint

## Goal

Expose cost data for governance and observability.

### Endpoint

```
GET /api/v1/metrics/cost
```

### Responsibilities

Return aggregated metrics:

* total cost
* cost by model
* cost by call type

Example response:

```
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
```

### Implementation

Create:

```
MetricsResource
```

Use repository aggregation queries.

### Verification

Endpoint returns correct aggregated values.

---

# Phase 7 — Testing

## Unit Tests

Test:

* cost calculation logic
* pricing configuration loading
* CostTrackingService persistence

## Integration Tests

Verify:

* ingestion creates embedding CostLog entries
* analysis creates embedding + completion entries
* metrics endpoint returns expected totals

---

# Phase 8 — Final Verification

Confirm the following system behavior:

* Document ingestion logs embedding costs
* Query embedding logs embedding costs
* Final LLM responses log completion costs
* `/analysis` generates two cost entries
* `/metrics/cost` aggregates cost data correctly
* Cost logging failures do not break application workflows
* Application boots successfully and all tests pass