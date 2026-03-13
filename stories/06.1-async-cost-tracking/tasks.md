# Tasks: Story 06.1 — Asynchronous Cost Tracking via Kafka

## 1. Infrastructure Setup

- [ ] Add Kafka dependencies to `backend/v2/pom.xml`.
    - `spring-kafka`
    - `spring-kafka-test` (for scope test)
- [ ] Configure Kafka in `backend/v2/src/main/resources/config/application.yml`.
    - Set `spring.kafka.bootstrap-servers` to `localhost:9092`.
    - Define topic name property: `application.kafka.topics.cost-logs: ai-cost-logs`.
    - Configure Producer JSON serializer.
    - Configure Consumer JSON deserializer (with trusted packages).
- [ ] Verify Kafka is running using `docker-compose up -d kafka`.

## 2. Event Definition (Phase 1)

- [ ] Create `CostLogEvent` record in `com.walshe.aimarket.service.dto`.
    - Fields: `callType` (String), `modelName`, `inputTokens`, `outputTokens`, `documentId`, `correlationId`.

## 3. Producer Implementation (Phase 2)

- [ ] Update `CostTrackingServiceImpl` to inject `KafkaTemplate<String, CostLogEvent>`.
- [ ] Refactor `logEmbeddingUsage` to:
    - Create `CostLogEvent` with `EMBEDDING` type.
    - Send event to Kafka topic using `kafkaTemplate.send()`.
    - Wrap in try-catch to log errors but never throw (fire-and-forget).
- [ ] Refactor `logCompletionUsage` to:
    - Create `CostLogEvent` with `COMPLETION` type.
    - Send event to Kafka topic.
    - Wrap in try-catch.
- [ ] Remove `CostLogRepository` dependency from `CostTrackingServiceImpl`.
- [ ] (Optional) Move cost calculation logic to a helper or keep it in the consumer to keep the producer extremely thin. *Decision: Calculation will happen in the consumer to minimize producer work.*

## 4. Consumer Implementation (Phase 3)

- [ ] Create `CostLogConsumer` in `com.walshe.aimarket.service.kafka`.
- [ ] Implement `@KafkaListener` method for `ai-cost-logs` topic.
- [ ] The consumer logic:
    - Receive `CostLogEvent`.
    - Calculate cost using `AiPricingProperties` (inject this into consumer).
    - Map event to `CostLog` entity.
    - Save to database using `CostLogRepository`.
- [ ] Ensure `@Transactional` is used in the consumer or a separate service method it calls.

## 5. Testing & Verification (Phase 4)

- [ ] Create/Update integration test `CostTrackingKafkaIT`.
    - Use `@EmbeddedKafka` or `Testcontainers` for Kafka.
    - Trigger `logEmbeddingUsage`.
    - Use `Awaitility` to verify that `CostLog` eventually appears in the database.
- [ ] Test Resilience:
    - Stop Kafka container.
    - Verify `DocumentResource` (ingestion) still returns 200 OK even if Kafka is down.
- [ ] Verify `MetricsResource` still returns correct totals after messages are consumed.

## 6. Cleanup & Documentation

- [ ] Update `README.md` if any new environment variables are required for Kafka.
- [ ] Ensure all code follows Spring Boot guidelines (Constructor injection, etc.).
