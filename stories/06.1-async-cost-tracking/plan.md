# Plan: Story 06.1 — Asynchronous Cost Tracking via Kafka

## 1. Goal
Decouple the cost tracking persistence from the main LLM call path by introducing Kafka as an asynchronous message broker. This ensures that database latency or unavailability does not impact the responsiveness of document ingestion or RAG analysis.

## 2. Infrastructure Changes
- **Docker Compose**: Add Zookeeper and Kafka services to `docker-compose.yml`.
- **Spring Boot Configuration**:
    - Add Kafka dependencies to `pom.xml`.
    - Configure `spring.kafka.bootstrap-servers` in `application.yml`.
    - Define topic `ai-cost-logs`.
    - Configure JSON serializers/deserializers for event messages.

## 3. Implementation Steps

### Phase 1: Event Definition
- Create `CostLogEvent` (Java record) to hold all data required for a `CostLog` entry.

### Phase 2: Producer Implementation
- Refactor `CostTrackingService` to use `KafkaTemplate`.
- Update `logEmbeddingUsage` and `logCompletionUsage` to publish `CostLogEvent` instead of calling `CostLogRepository.save()`.
- Implement robust error handling (try-catch around `send()`) to maintain "fire-and-forget" behavior.

### Phase 3: Consumer Implementation
- Create `CostLogConsumer` with `@KafkaListener`.
- The consumer will:
    - Receive `CostLogEvent`.
    - Map it to a `CostLog` entity.
    - Persist the entity via `CostLogRepository`.

### Phase 4: Verification & Refinement
- Update integration tests to use `Testcontainers` for Kafka.
- Ensure `CostTrackingServiceImpl` still calculates costs correctly or move calculation logic to a shared component if needed.
- Verify that the metrics endpoint still aggregates correctly from the database.

## 4. Risks & Mitigations
- **Kafka Unavailability**: The producer must not block or throw exceptions if Kafka is down. Mitigation: Comprehensive try-catch and logging in the producer.
- **Event Loss**: If Kafka is down, events may be lost. Mitigation: For cost tracking, best-effort is acceptable for v1, but logs will indicate failures.
- **Out-of-Order Events**: Cost logs are mostly independent. Mitigation: Not a concern for this specific use case.
