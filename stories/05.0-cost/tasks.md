# Tasks - Story 05: Cost Governance

## Phase 1 — Create CostLog Data Model

- [x] Create `CostLog` entity.

Fields:

- id (Long, PK)
- callType (ENUM: EMBEDDING | COMPLETION)
- modelName (String)
- inputTokens (Integer)
- outputTokens (Integer, nullable)
- totalTokens (Integer)
- estimatedUsdCost (BigDecimal)
- documentId (Long, nullable)
- correlationId (String, nullable)
- createdAt (Instant)

- [x] Ensure `createdAt` is **not set in code** and uses DB `DEFAULT now()`.

- [x] Create corresponding table `cost_log`.

- [x] Add indexes:
  - `model_name`
  - `call_type`
  - `created_at`

- [x] Create `CostLogRepository` extending:

```

JpaRepository<CostLog, Long>

````

- [x] Build application and confirm table creation and successful boot.

---

## Phase 2 — Implement Pricing Configuration

- [x] Add pricing configuration to `application.yml`.

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

* [x] Create `AiPricingProperties` configuration class.

Responsibilities:

* Load pricing configuration

* Provide lookup by model name

* [x] Verify configuration loads correctly during application startup.

---

## Phase 3 — Implement CostTrackingService

* [x] Create `CostTrackingService`.

Responsibilities:

* Calculate estimated USD cost

* Persist `CostLog` entries

* [x] Implement method:

```
logEmbeddingUsage(modelName, inputTokens, documentId, correlationId)
```

* [x] Implement method:

```
logCompletionUsage(modelName, inputTokens, outputTokens, correlationId)
```

* [x] Implement cost calculation:

Embedding:

```
(tokens / 1000) * embeddingCostPer1kTokens
```

Completion:

```
(inputTokens / 1000 * inputCostPer1kTokens)
+
(outputTokens / 1000 * outputCostPer1kTokens)
```

* [x] Ensure failures during cost logging:

  * are logged
  * do **not propagate exceptions**

* [x] Add unit tests for:

  * cost calculation
  * successful persistence

---

## Phase 4 — Integrate with EmbeddingService

* [x] Inject `CostTrackingService` into `EmbeddingService`.

* [x] After a successful embedding call:

  * Extract model name
  * Determine input token count (if available)

* [x] Call:

```
costTrackingService.logEmbeddingUsage(...)
```

* [x] Pass `documentId` during ingestion when available.

* [x] Pass `null` documentId during query embedding.

* [x] Verify ingestion produces **one CostLog entry per chunk embedding**.

---

## Phase 5 — Integrate with Completion Service

* [x] Inject `CostTrackingService` into the LLM completion client/service.

* [x] After successful completion response:

  * Extract:

    * model name
    * input tokens
    * output tokens

* [x] Call:

```
costTrackingService.logCompletionUsage(...)
```

* [x] Verify `/analysis` produces:

  * one embedding CostLog entry (query embedding)
  * one completion CostLog entry

---

## Phase 6 — Implement Metrics Endpoint

- [x] Create `MetricsResource`.

- [x] Implement endpoint:

```
GET /api/v1/metrics/cost
```

- [x] Implement repository queries for:

  * total USD cost
  * cost grouped by model
  * cost grouped by call type

- [x] Construct response object containing:

```
totalUsd
byModel
byCallType
```

- [x] Verify endpoint returns expected aggregated values.

---

## Phase 7 — Testing

### Unit Tests

- [x] Test pricing configuration loading.
- [x] Test cost calculation logic.
- [x] Test CostTrackingService persistence.

### Integration Tests

- [x] Verify document ingestion generates embedding CostLog entries.

- [x] Verify `/analysis` generates:

  * embedding CostLog entry
  * completion CostLog entry.

- [x] Verify `/metrics/cost` returns aggregated cost data.

---

## Phase 8 — Final Verification

- [x] Document ingestion logs embedding costs.
- [x] Query embedding logs embedding costs.
- [x] Completion calls log completion costs.
- [x] `/analysis` generates two cost entries.
- [x] `/metrics/cost` aggregates costs correctly.
- [x] Application builds successfully.
- [x] All tests pass.
- [x] No unrelated modules modified.

```

---

### One small architectural improvement you may consider later

A **future Story 06** could add:

```

correlationId propagation

```

So a single `/analysis` request groups:

```

query embedding
+
completion

```

under the same ID.

That makes **cost tracing per request extremely powerful** in production.

If you'd like, the next useful step would be showing **how to integrate this cleanly with Spring AI's token usage metadata**, because many developers implement cost tracking incorrectly there.
```
