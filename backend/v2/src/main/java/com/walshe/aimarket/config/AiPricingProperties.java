package com.walshe.aimarket.config;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for AI model pricing.
 */
@Configuration
@ConfigurationProperties(prefix = "ai.pricing", ignoreUnknownFields = true)
public class AiPricingProperties {

    private Map<String, ModelPricing> models = new HashMap<>();

    public Map<String, ModelPricing> getModels() {
        return models;
    }

    public void setModels(Map<String, ModelPricing> models) {
        this.models = models;
    }

    public static class ModelPricing {

        private BigDecimal inputCostPer1kTokens = BigDecimal.ZERO;
        private BigDecimal outputCostPer1kTokens = BigDecimal.ZERO;
        private BigDecimal embeddingCostPer1kTokens = BigDecimal.ZERO;

        public BigDecimal getInputCostPer1kTokens() {
            return inputCostPer1kTokens;
        }

        public void setInputCostPer1kTokens(BigDecimal inputCostPer1kTokens) {
            this.inputCostPer1kTokens = inputCostPer1kTokens;
        }

        public BigDecimal getOutputCostPer1kTokens() {
            return outputCostPer1kTokens;
        }

        public void setOutputCostPer1kTokens(BigDecimal outputCostPer1kTokens) {
            this.outputCostPer1kTokens = outputCostPer1kTokens;
        }

        public BigDecimal getEmbeddingCostPer1kTokens() {
            return embeddingCostPer1kTokens;
        }

        public void setEmbeddingCostPer1kTokens(BigDecimal embeddingCostPer1kTokens) {
            this.embeddingCostPer1kTokens = embeddingCostPer1kTokens;
        }
    }
}
