package com.walshe.aimarket.service.impl;

import com.walshe.aimarket.domain.CostLog;
import com.walshe.aimarket.service.CostTrackingService;
import com.walshe.aimarket.service.dto.CostLogEvent;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of {@link CostTrackingService}.
 */
@Service
@Transactional
public class CostTrackingServiceImpl implements CostTrackingService {

    private final Logger log = LoggerFactory.getLogger(CostTrackingServiceImpl.class);

    private final KafkaTemplate<String, CostLogEvent> kafkaTemplate;
    private final String topic;

    public CostTrackingServiceImpl(
        KafkaTemplate<String, CostLogEvent> kafkaTemplate,
        @Value("${application.kafka.topics.cost-logs:ai-cost-logs}") String topic
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    @Override
    public CostLog logEmbeddingUsage(String modelName, Integer inputTokens, Long documentId, String correlationId) {
        log.debug("Logging embedding usage for model: {}, tokens: {}", modelName, inputTokens);
        try {
            CostLogEvent event = new CostLogEvent(
                "EMBEDDING",
                modelName,
                inputTokens,
                null,
                documentId,
                correlationId != null ? UUID.fromString(correlationId) : null
            );
            log.debug("[DEBUG_LOG] Sending embedding CostLogEvent: {}", event);
            kafkaTemplate.send(topic, event);
            log.debug("[DEBUG_LOG] Embedding event sent successfully");
        } catch (Exception e) {
            log.error("Failed to send embedding usage event to Kafka", e);
        }
        return null;
    }

    @Override
    public CostLog logCompletionUsage(String modelName, Integer inputTokens, Integer outputTokens, String correlationId) {
        log.debug("Logging completion usage for model: {}, input tokens: {}, output tokens: {}", modelName, inputTokens, outputTokens);
        try {
            CostLogEvent event = new CostLogEvent(
                "COMPLETION",
                modelName,
                inputTokens,
                outputTokens,
                null,
                correlationId != null ? UUID.fromString(correlationId) : null
            );
            log.debug("[DEBUG_LOG] Sending completion CostLogEvent: {}", event);
            kafkaTemplate.send(topic, event);
            log.debug("[DEBUG_LOG] Completion event sent successfully");
        } catch (Exception e) {
            log.error("Failed to send completion usage event to Kafka", e);
        }
        return null;
    }
}
