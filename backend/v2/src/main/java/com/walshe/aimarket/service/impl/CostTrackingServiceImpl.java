package com.walshe.aimarket.service.impl;

import com.walshe.aimarket.config.AiPricingProperties;
import com.walshe.aimarket.domain.CostLog;
import com.walshe.aimarket.repository.CostLogRepository;
import com.walshe.aimarket.service.CostTrackingService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of {@link CostTrackingService}.
 */
@Service
@Transactional
public class CostTrackingServiceImpl implements CostTrackingService {

    private final Logger log = LoggerFactory.getLogger(CostTrackingServiceImpl.class);

    private final CostLogRepository costLogRepository;
    private final AiPricingProperties aiPricingProperties;

    public CostTrackingServiceImpl(CostLogRepository costLogRepository, AiPricingProperties aiPricingProperties) {
        this.costLogRepository = costLogRepository;
        this.aiPricingProperties = aiPricingProperties;
    }

    @Override
    public CostLog logEmbeddingUsage(String modelName, Integer inputTokens, Long documentId, String correlationId) {
        log.debug("Logging embedding usage for model: {}, tokens: {}", modelName, inputTokens);
        try {
            BigDecimal cost = calculateEmbeddingCost(modelName, inputTokens);

            CostLog costLog = new CostLog();
            costLog.setCallType(CostLog.CallType.EMBEDDING);
            costLog.setModelName(modelName);
            costLog.setInputTokens(inputTokens);
            costLog.setTotalTokens(inputTokens);
            costLog.setEstimatedUsdCost(cost);
            costLog.setDocumentId(documentId);
            costLog.setCorrelationId(correlationId);

            return costLogRepository.save(costLog);
        } catch (Exception e) {
            log.error("Failed to log embedding usage", e);
            return null;
        }
    }

    @Override
    public CostLog logCompletionUsage(String modelName, Integer inputTokens, Integer outputTokens, String correlationId) {
        log.debug("Logging completion usage for model: {}, input tokens: {}, output tokens: {}", modelName, inputTokens, outputTokens);
        try {
            BigDecimal cost = calculateCompletionCost(modelName, inputTokens, outputTokens);

            CostLog costLog = new CostLog();
            costLog.setCallType(CostLog.CallType.COMPLETION);
            costLog.setModelName(modelName);
            costLog.setInputTokens(inputTokens);
            costLog.setOutputTokens(outputTokens);
            costLog.setTotalTokens(inputTokens + outputTokens);
            costLog.setEstimatedUsdCost(cost);
            costLog.setCorrelationId(correlationId);

            return costLogRepository.save(costLog);
        } catch (Exception e) {
            log.error("Failed to log completion usage", e);
            return null;
        }
    }

    private BigDecimal calculateEmbeddingCost(String modelName, Integer tokens) {
        AiPricingProperties.ModelPricing pricing = aiPricingProperties.getModels().get(modelName);
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
        if (pricing.getOutputCostPer1kTokens() != null) {
            outputCost = BigDecimal.valueOf(outputTokens)
                .divide(BigDecimal.valueOf(1000), 10, RoundingMode.HALF_UP)
                .multiply(pricing.getOutputCostPer1kTokens());
        }

        return inputCost.add(outputCost);
    }
}
