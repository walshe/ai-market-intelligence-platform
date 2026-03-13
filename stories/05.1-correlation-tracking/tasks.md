# Tasks - Story 5.1: Correlation Tracking for AI Operations

## Phase 1 — Generate Correlation ID for Analysis Requests

- [x] Update the `/analysis` controller (e.g., `AnalysisResource`).

- [x] At the start of the request lifecycle, generate a correlation ID:

```

String correlationId = UUID.randomUUID().toString();

```

- [x] Pass the generated `correlationId` to the analysis service.

- [x] Add a log entry when the request starts:

```

analysis request started correlationId=<id>

```

- [x] Build and run application; confirm no errors.

---

## Phase 2 — Propagate Correlation ID Through Analysis Workflow

- [x] Update `AnalysisService` to accept `String correlationId`.

Example:

```

analyzeQuery(String query, String correlationId)

```

- [x] Pass `correlationId` when invoking:

  - query embedding generation (`EmbeddingService`)
  - completion generation (`CompletionService` or equivalent)

- [x] Update method signatures in affected services to accept `correlationId`.

Example:

```

generateEmbedding(String text, String correlationId)
generateCompletion(..., String correlationId)

```

- [x] Ensure correlation ID is propagated through all AI-related service calls.

- [x] Build and run application; confirm workflow still operates normally.

---

## Phase 3 — Integrate Correlation ID into Cost Tracking

- [x] Update `CostTrackingService` methods to accept correlationId.

Example:

```

logEmbeddingUsage(modelName, inputTokens, documentId, correlationId)

logCompletionUsage(modelName, inputTokens, outputTokens, correlationId)

```

- [x] Ensure `CostLog.correlationId` is populated when provided.

- [x] Ensure ingestion-related embedding calls pass:

```

correlationId = null

```

- [x] Verify that CostLog entries for `/analysis` contain the correlationId.

- [x] Verify ingestion CostLog entries contain `null`.

---

## Phase 4 — Add Correlation ID Logging

- [x] Add structured logging in `AnalysisService`:

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

- [x] Verify correlation IDs appear consistently in logs.

---

## Phase 5 — Optional Metrics Endpoint Enhancement

- [x] Update `GET /api/v1/metrics/cost` to accept optional query parameter:

```

correlationId

```

- [x] If correlationId is present:

  - return cost records associated with that correlationId.

- [x] Add repository method:

```

List<CostLog> findByCorrelationId(String correlationId);

```

- [x] Verify endpoint behavior:

Example:

```

GET /api/v1/metrics/cost?correlationId=<id>

```

returns the associated cost entries.

---

## Phase 6 — Testing

### Unit Tests

- [x] Verify correlationId generation logic.
- [x] Verify CostTrackingService persists correlationId correctly.

### Integration Tests

- [x] Execute `/analysis` request.

Verify:

- [x] embedding CostLog entry contains correlationId
- [x] completion CostLog entry contains same correlationId

- [x] Execute document ingestion.

Verify:

- [x] ingestion CostLog entries have `null` correlationId.

---

## Phase 7 — Final Verification

- [x] `/analysis` generates a unique correlation ID.
- [x] Correlation ID propagates through embedding and completion services.
- [x] CostLog entries for analysis share the same correlationId.
- [x] Document ingestion CostLog entries do not contain correlationId.
- [x] Logs include correlationId for traceability.
- [x] Optional metrics endpoint filtering works when enabled.
- [x] Application builds successfully and all tests pass.
- [x] No unrelated modules modified.
