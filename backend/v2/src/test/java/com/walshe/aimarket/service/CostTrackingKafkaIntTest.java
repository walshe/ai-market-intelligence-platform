package com.walshe.aimarket.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import com.walshe.aimarket.IntegrationTest;
import com.walshe.aimarket.domain.CostLog;
import com.walshe.aimarket.repository.CostLogRepository;
import com.walshe.aimarket.service.dto.CostLogEvent;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.TestPropertySource;

@IntegrationTest
@EmbeddedKafka(partitions = 1, topics = { "ai-cost-logs" })
@TestPropertySource(properties = {
    "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
    "application.kafka.topics.cost-logs=ai-cost-logs",
    "spring.kafka.producer.value-serializer=org.springframework.kafka.support.serializer.JsonSerializer",
    "spring.kafka.consumer.value-deserializer=org.springframework.kafka.support.serializer.JsonDeserializer",
    "spring.kafka.consumer.properties.spring.json.trusted.packages=com.walshe.aimarket.service.dto",
    "application.llm.provider=openai",
    "application.llm.openai.api-key=dummy",
    "application.llm.openai.model-name=gpt-4o-mini",
    "application.llm.openai.base-url=http://localhost",
    "ai.pricing.models.text-embedding-3-small.embedding-cost-per1k-tokens=0.00002",
    "ai.pricing.models.gpt-4o-mini.input-cost-per1k-tokens=0.00015",
    "ai.pricing.models.gpt-4o-mini.output-cost-per1k-tokens=0.0006",
    "application.llm.bedrock.region=us-east-1",
    "application.llm.bedrock.model-name=anthropic.claude-3-haiku-20240307-v1:0"
})
class CostTrackingKafkaIntTest {

    private final Logger log = LoggerFactory.getLogger(CostTrackingKafkaIntTest.class);

    @Autowired
    private CostTrackingService costTrackingService;

    @Autowired
    private CostLogRepository costLogRepository;

    @Test
    void shouldLogEmbeddingUsageAsynchronously() {
        log.info("[DEBUG_LOG] Starting shouldLogEmbeddingUsageAsynchronously");
        // Given
        int initialCount = costLogRepository.findAll().size();
        String modelName = "text-embedding-3-small";
        Integer inputTokens = 100;
        Long documentId = 1L;
        String correlationId = "550e8400-e29b-41d4-a716-446655440000";

        // When
        log.info("[DEBUG_LOG] Calling logEmbeddingUsage");
        costTrackingService.logEmbeddingUsage(modelName, inputTokens, documentId, correlationId, "openai", 123L);
        log.info("[DEBUG_LOG] Called logEmbeddingUsage");

        // Then
        log.info("[DEBUG_LOG] Awaiting results");
        await()
            .atMost(Duration.ofSeconds(10))
            .untilAsserted(() -> {
                List<CostLog> costLogs = costLogRepository.findAll();
                assertThat(costLogs).hasSize(initialCount + 1);
                CostLog lastLog = costLogs.get(costLogs.size() - 1);
                assertThat(lastLog.getCallType()).isEqualTo(CostLog.CallType.EMBEDDING);
                assertThat(lastLog.getModelName()).isEqualTo(modelName);
                assertThat(lastLog.getInputTokens()).isEqualTo(inputTokens);
                assertThat(lastLog.getDocumentId()).isEqualTo(documentId);
                assertThat(lastLog.getCorrelationId()).isEqualTo(correlationId);
                assertThat(lastLog.getProvider()).isEqualTo("openai");
                assertThat(lastLog.getLatencyMs()).isEqualTo(123L);
                assertThat(lastLog.getEstimatedUsdCost()).isGreaterThan(java.math.BigDecimal.ZERO);
            });
    }

    @Test
    void shouldLogCompletionUsageAsynchronously() {
        // Given
        int initialCount = costLogRepository.findAll().size();
        String modelName = "gpt-4o-mini";
        Integer inputTokens = 200;
        Integer outputTokens = 300;
        String correlationId = "660e8400-e29b-41d4-a716-446655440000";

        // When
        costTrackingService.logCompletionUsage(modelName, inputTokens, outputTokens, correlationId, "bedrock", 456L);

        // Then
        await()
            .atMost(Duration.ofSeconds(10))
            .untilAsserted(() -> {
                List<CostLog> costLogs = costLogRepository.findAll();
                assertThat(costLogs).hasSize(initialCount + 1);
                CostLog lastLog = costLogs.get(costLogs.size() - 1);
                assertThat(lastLog.getCallType()).isEqualTo(CostLog.CallType.COMPLETION);
                assertThat(lastLog.getModelName()).isEqualTo(modelName);
                assertThat(lastLog.getInputTokens()).isEqualTo(inputTokens);
                assertThat(lastLog.getOutputTokens()).isEqualTo(outputTokens);
                assertThat(lastLog.getCorrelationId()).isEqualTo(correlationId);
                assertThat(lastLog.getProvider()).isEqualTo("bedrock");
                assertThat(lastLog.getLatencyMs()).isEqualTo(456L);
                assertThat(lastLog.getEstimatedUsdCost()).isGreaterThan(java.math.BigDecimal.ZERO);
            });
    }

    @Test
    void shouldHandleVersionedModelNameWithPrefixMatch() {
        // Given
        int initialCount = costLogRepository.findAll().size();
        String versionedModelName = "gpt-4o-mini-2024-07-18";
        Integer inputTokens = 200;
        Integer outputTokens = 300;
        String correlationId = "770e8400-e29b-41d4-a716-446655440000";

        // When
        costTrackingService.logCompletionUsage(versionedModelName, inputTokens, outputTokens, correlationId, "openai", 456L);

        // Then
        await()
            .atMost(Duration.ofSeconds(10))
            .untilAsserted(() -> {
                List<CostLog> costLogs = costLogRepository.findAll();
                assertThat(costLogs).hasSize(initialCount + 1);
                CostLog lastLog = costLogs.get(costLogs.size() - 1);
                assertThat(lastLog.getModelName()).isEqualTo(versionedModelName);
                assertThat(lastLog.getEstimatedUsdCost()).isGreaterThan(java.math.BigDecimal.ZERO);
            });
    }

    @Test
    void shouldHandleVersionedEmbeddingModelNameWithPrefixMatch() {
        // Given
        int initialCount = costLogRepository.findAll().size();
        String versionedModelName = "text-embedding-3-small-v1";
        Integer inputTokens = 100;
        String correlationId = "880e8400-e29b-41d4-a716-446655440000";

        // When
        costTrackingService.logEmbeddingUsage(versionedModelName, inputTokens, null, correlationId, "openai", 123L);

        // Then
        await()
            .atMost(Duration.ofSeconds(10))
            .untilAsserted(() -> {
                List<CostLog> costLogs = costLogRepository.findAll();
                assertThat(costLogs).hasSize(initialCount + 1);
                CostLog lastLog = costLogs.get(costLogs.size() - 1);
                assertThat(lastLog.getModelName()).isEqualTo(versionedModelName);
                assertThat(lastLog.getEstimatedUsdCost()).isGreaterThan(java.math.BigDecimal.ZERO);
            });
    }
}
