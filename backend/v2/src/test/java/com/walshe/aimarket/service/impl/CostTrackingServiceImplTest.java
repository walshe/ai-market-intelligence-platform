package com.walshe.aimarket.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.walshe.aimarket.config.AiPricingProperties;
import com.walshe.aimarket.domain.CostLog;
import com.walshe.aimarket.repository.CostLogRepository;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CostTrackingServiceImplTest {

    @Mock
    private CostLogRepository costLogRepository;

    private AiPricingProperties aiPricingProperties;

    private CostTrackingServiceImpl costTrackingService;

    @BeforeEach
    void setUp() {
        aiPricingProperties = new AiPricingProperties();
        Map<String, AiPricingProperties.ModelPricing> models = new HashMap<>();

        AiPricingProperties.ModelPricing embeddingModel = new AiPricingProperties.ModelPricing();
        embeddingModel.setEmbeddingCostPer1kTokens(new BigDecimal("0.00002"));
        models.put("text-embedding-3-small", embeddingModel);

        AiPricingProperties.ModelPricing completionModel = new AiPricingProperties.ModelPricing();
        completionModel.setInputCostPer1kTokens(new BigDecimal("0.00015"));
        completionModel.setOutputCostPer1kTokens(new BigDecimal("0.00060"));
        models.put("gpt-4o-mini", completionModel);

        aiPricingProperties.setModels(models);

        costTrackingService = new CostTrackingServiceImpl(costLogRepository, aiPricingProperties);
    }

    @Test
    void shouldCalculateEmbeddingCostCorrectly() {
        when(costLogRepository.save(any(CostLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CostLog result = costTrackingService.logEmbeddingUsage("text-embedding-3-small", 1000, 1L, "corr-1");

        assertThat(result).isNotNull();
        assertThat(result.getCallType()).isEqualTo(CostLog.CallType.EMBEDDING);
        assertThat(result.getEstimatedUsdCost()).isEqualByComparingTo("0.00002");
        assertThat(result.getTotalTokens()).isEqualTo(1000);
        assertThat(result.getDocumentId()).isEqualTo(1L);
        assertThat(result.getCorrelationId()).isEqualTo("corr-1");
    }

    @Test
    void shouldCalculateCompletionCostCorrectly() {
        when(costLogRepository.save(any(CostLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CostLog result = costTrackingService.logCompletionUsage("gpt-4o-mini", 1000, 2000, "corr-2");

        assertThat(result).isNotNull();
        assertThat(result.getCallType()).isEqualTo(CostLog.CallType.COMPLETION);
        // (1000/1000 * 0.00015) + (2000/1000 * 0.00060) = 0.00015 + 0.00120 = 0.00135
        assertThat(result.getEstimatedUsdCost()).isEqualByComparingTo("0.00135");
        assertThat(result.getTotalTokens()).isEqualTo(3000);
        assertThat(result.getCorrelationId()).isEqualTo("corr-2");
    }

    @Test
    void shouldHandleMissingPricingGracefully() {
        when(costLogRepository.save(any(CostLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CostLog result = costTrackingService.logEmbeddingUsage("unknown-model", 1000, null, null);

        assertThat(result).isNotNull();
        assertThat(result.getEstimatedUsdCost()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void shouldNotPropagateExceptionOnRepositoryFailure() {
        when(costLogRepository.save(any(CostLog.class))).thenThrow(new RuntimeException("DB error"));

        CostLog result = costTrackingService.logEmbeddingUsage("text-embedding-3-small", 1000, null, null);

        assertThat(result).isNull();
        verify(costLogRepository).save(any(CostLog.class));
    }
}
