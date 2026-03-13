# Story 06.1: Asynchronous Cost Tracking via Kafka

## Description

The current cost tracking implementation in `CostTrackingService` is synchronous. While it is wrapped in try-catch blocks to prevent workflow failure, it still executes within the same thread as the primary LLM call. This story aims to decouple cost logging from the main request/response cycle by using Kafka as an asynchronous message broker.

By moving to an asynchronous model, we further ensure that any latency or temporary unavailability of the database does not impact the responsiveness of document ingestion or RAG analysis.

---

## Architectural Context

- **Producer**: `CostTrackingService` will now publish an event to a Kafka topic instead of directly saving to the database.
- **Broker**: Kafka will receive and store these events.
- **Consumer**: A new Kafka consumer component will listen to the topic and persist the `CostLog` entries to PostgreSQL.
- **Data Model**: The `CostLog` entity remains unchanged.

---

## Requirements

### A) Kafka Infrastructure Setup

- [ ] Ensure Kafka is available in the development environment (via `docker-compose.yml`).
- [ ] Define the Kafka topic name for cost tracking (e.g., `ai-cost-logs`) in `application.yml`.
- [ ] Configure standard Kafka producer and consumer properties (bootstrap servers, serializers/deserializers).

---

### B) Cost tracking Event (DTO)

- [ ] Create a `CostLogEvent` record or class to represent the data to be published.
  - This should contain all fields necessary to reconstruct a `CostLog` entry.

---

### C) Asynchronous Producer

- [ ] Refactor `CostTrackingService` (or provide a new implementation) to publish `CostLogEvent` messages.
- [ ] The publishing must be "fire-and-forget" from the perspective of the main workflow.
- [ ] Ensure that failures in publishing to Kafka are logged but do not throw exceptions to the caller.

---

### D) Kafka Consumer

- [ ] Implement a Kafka consumer that listens to the `ai-cost-logs` topic.
- [ ] The consumer should:
  - Deserialize the `CostLogEvent`.
  - Calculate the cost (if not already calculated by the producer).
  - Persist the `CostLog` to the database using `CostLogRepository`.
- [ ] Implement basic error handling and retries for the consumer (e.g., if the database is temporarily down).

---

### E) Refactoring Existing Services

- [ ] `EmbeddingService` and `LlmClient` should continue to use `CostTrackingService` without knowing it has become asynchronous.
- [ ] Verify that the `correlationId` and `documentId` are correctly passed through the event.

---

### F) Testing & Verification

- [ ] **Integration Tests**:
  - Use `Testcontainers` for both PostgreSQL and Kafka.
  - Verify that calling `logEmbeddingUsage` or `logCompletionUsage` eventually results in a `CostLog` entry in the database.
  - Test scenario where Kafka is temporarily unavailable (verify no impact on main workflow).
- [ ] **End-to-End Verification**:
  - Run a document ingestion or an analysis request.
  - Confirm `CostLog` entries are created in the background.

---

## Acceptance Criteria

- All `CostLog` entries are persisted via Kafka asynchronous flows.
- Primary LLM workflows (ingestion, RAG) are decoupled from database persistence of cost logs.
- Kafka producer is configured for high availability/resilience (e.g., appropriate acks, retries).
- The `GET /api/v1/metrics/cost` endpoint still returns accurate data based on persisted logs.
- Integration tests prove that logs are eventually consistent with LLM calls.
- Application boots successfully and all tests pass.
