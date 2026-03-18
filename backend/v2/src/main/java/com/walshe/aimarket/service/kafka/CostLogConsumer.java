package com.walshe.aimarket.service.kafka;

import com.walshe.aimarket.config.AiPricingProperties;
import com.walshe.aimarket.domain.CostLog;
import com.walshe.aimarket.repository.CostLogRepository;
import com.walshe.aimarket.service.dto.CostLogEvent;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Consumer for Kafka events related to LLM cost logging.
 */
@Service
public class CostLogConsumer {

    private final Logger log = LoggerFactory.getLogger(CostLogConsumer.class);

    private final CostLogRepository costLogRepository;
    private final AiPricingProperties aiPricingProperties;

    public CostLogConsumer(CostLogRepository costLogRepository, AiPricingProperties aiPricingProperties) {
        this.costLogRepository = costLogRepository;
        this.aiPricingProperties = aiPricingProperties;
    }

    @KafkaListener(topics = "${application.kafka.topics.cost-logs:ai-cost-logs}", groupId = "${spring.kafka.consumer.group-id:ai-market-intelligence}")
    @Transactional
    public void consume(CostLogEvent event) {
        log.debug("Consumed CostLogEvent: {}", event);
        try {
            if (event.correlationId() != null) {
                String corrId = event.correlationId().toString();
                CostLog.CallType callType = CostLog.CallType.valueOf(event.callType());
                if (costLogRepository.existsByCorrelationIdAndCallType(corrId, callType)) {
                    log.info("Skipping duplicate CostLogEvent for correlationId: {} and callType: {}", corrId, callType);
                    return;
                }
            }

            CostLog costLog = new CostLog();
            costLog.setCallType(CostLog.CallType.valueOf(event.callType()));
            costLog.setModelName(event.modelName());
            costLog.setInputTokens(event.inputTokens());
            costLog.setOutputTokens(event.outputTokens());
            costLog.setDocumentId(event.documentId());
            costLog.setCorrelationId(event.correlationId() != null ? event.correlationId().toString() : null);
            costLog.setProvider(event.provider());
            costLog.setLatencyMs(event.latencyMs());

            int totalTokens = event.inputTokens() + (event.outputTokens() != null ? event.outputTokens() : 0);
            costLog.setTotalTokens(totalTokens);

            BigDecimal cost;
            if ("EMBEDDING".equals(event.callType())) {
                cost = calculateEmbeddingCost(event.modelName(), event.inputTokens());
            } else {
                cost = calculateCompletionCost(event.modelName(), event.inputTokens(), event.outputTokens());
            }
            costLog.setEstimatedUsdCost(cost);

            costLogRepository.save(costLog);
            log.debug("Saved CostLog to database for correlationId: {}", costLog.getCorrelationId());
        } catch (Exception e) {
            log.error("Failed to process CostLogEvent", e);
        }
    }

    private BigDecimal calculateEmbeddingCost(String modelName, Integer tokens) {
        AiPricingProperties.ModelPricing pricing = aiPricingProperties.getModels().get(modelName);
        if (pricing == null) {
            // Try prefix matching
            pricing = aiPricingProperties.getModels().entrySet().stream()
                .filter(entry -> modelName.startsWith(entry.getKey()))
                .map(java.util.Map.Entry::getValue)
                .findFirst()
                .orElse(null);
        }

        if (pricing == null || pricing.getEmbeddingCostPer1kTokens() == null) {
            log.warn("No embedding pricing found for model: {}", modelName);
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(tokens)
            .divide(BigDecimal.valueOf(1000), 10, RoundingMode.HALF_UP)
            .multiply(pricing.getEmbeddingCostPer1kTokens());
    }

    private BigDecimal calculateCompletionCost(String modelName, Integer inputTokens, Integer outputTokens) {
        AiPricingProperties.ModelPricing pricing = aiPricingProperties.getModels().get(modelName);
        if (pricing == null) {
            // Try prefix matching (e.g., "gpt-4o-mini-2024-07-18" should match "gpt-4o-mini")
            pricing = aiPricingProperties.getModels().entrySet().stream()
                .filter(entry -> modelName.startsWith(entry.getKey()))
                .map(java.util.Map.Entry::getValue)
                .findFirst()
                .orElse(null);
        }

        if (pricing == null) {
            log.warn("No pricing found for model: {}", modelName);
            return BigDecimal.ZERO;
        }

        BigDecimal inputCost = BigDecimal.ZERO;
        if (pricing.getInputCostPer1kTokens() != null) {
            inputCost = BigDecimal.valueOf(inputTokens)
                .divide(BigDecimal.valueOf(1000), 10, RoundingMode.HALF_UP)
                .multiply(pricing.getInputCostPer1kTokens());
        }

        BigDecimal outputCost = BigDecimal.ZERO;
        if (pricing.getOutputCostPer1kTokens() != null && outputTokens != null) {
            outputCost = BigDecimal.valueOf(outputTokens)
                .divide(BigDecimal.valueOf(1000), 10, RoundingMode.HALF_UP)
                .multiply(pricing.getOutputCostPer1kTokens());
        }

        return inputCost.add(outputCost);
    }
}
