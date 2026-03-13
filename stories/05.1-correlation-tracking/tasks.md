# Tasks - Story 5.1: Correlation Tracking for AI Operations

## Phase 1 — Generate Correlation ID for Analysis Requests

- [ ] Update the `/analysis` controller (e.g., `AnalysisResource`).

- [ ] At the start of the request lifecycle, generate a correlation ID:

```

String correlationId = UUID.randomUUID().toString();

```

- [ ] Pass the generated `correlationId` to the analysis service.

- [ ] Add a log entry when the request starts:

```

analysis request started correlationId=<id>

```

- [ ] Build and run application; confirm no errors.

---

## Phase 2 — Propagate Correlation ID Through Analysis Workflow

- [ ] Update `AnalysisService` to accept `String correlationId`.

Example:

```

analyzeQuery(String query, String correlationId)

```

- [ ] Pass `correlationId` when invoking:

  - query embedding generation (`EmbeddingService`)
  - completion generation (`CompletionService` or equivalent)

- [ ] Update method signatures in affected services to accept `correlationId`.

Example:

```

generateEmbedding(String text, String correlationId)
generateCompletion(..., String correlationId)

```

- [ ] Ensure correlation ID is propagated through all AI-related service calls.

- [ ] Build and run application; confirm workflow still operates normally.

---

## Phase 3 — Integrate Correlation ID into Cost Tracking

- [ ] Update `CostTrackingService` methods to accept correlationId.

Example:

```

logEmbeddingUsage(modelName, inputTokens, documentId, correlationId)

logCompletionUsage(modelName, inputTokens, outputTokens, correlationId)

```

- [ ] Ensure `CostLog.correlationId` is populated when provided.

- [ ] Ensure ingestion-related embedding calls pass:

```

correlationId = null

```

- [ ] Verify that CostLog entries for `/analysis` contain the correlationId.

- [ ] Verify ingestion CostLog entries contain `null`.

---

## Phase 4 — Add Correlation ID Logging

- [ ] Add structured logging in `AnalysisService`:

Start:

```

analysis request started correlationId=<id>

```

Embedding stage (optional):

```

query embedding started correlationId=<id>

```

Completion stage (optional):

```

completion generation started correlationId=<id>

```

End:

```

analysis request completed correlationId=<id>

```

- [ ] Verify correlation IDs appear consistently in logs.

---

## Phase 5 — Optional Metrics Endpoint Enhancement

- [ ] Update `GET /api/v1/metrics/cost` to accept optional query parameter:

```

correlationId

```

- [ ] If correlationId is present:

  - return cost records associated with that correlationId.

- [ ] Add repository method:

```

List<CostLog> findByCorrelationId(String correlationId);

```

- [ ] Verify endpoint behavior:

Example:

```

GET /api/v1/metrics/cost?correlationId=<id>

```

returns the associated cost entries.

---

## Phase 6 — Testing

### Unit Tests

- [ ] Verify correlationId generation logic.
- [ ] Verify CostTrackingService persists correlationId correctly.

### Integration Tests

- [ ] Execute `/analysis` request.

Verify:

- embedding CostLog entry contains correlationId
- completion CostLog entry contains same correlationId

- [ ] Execute document ingestion.

Verify:

- ingestion CostLog entries have `null` correlationId.

---

## Phase 7 — Final Verification

- [ ] `/analysis` generates a unique correlation ID.
- [ ] Correlation ID propagates through embedding and completion services.
- [ ] CostLog entries for analysis share the same correlationId.
- [ ] Document ingestion CostLog entries do not contain correlationId.
- [ ] Logs include correlationId for traceability.
- [ ] Optional metrics endpoint filtering works when enabled.
- [ ] Application builds successfully and all tests pass.
- [ ] No unrelated modules modified.
