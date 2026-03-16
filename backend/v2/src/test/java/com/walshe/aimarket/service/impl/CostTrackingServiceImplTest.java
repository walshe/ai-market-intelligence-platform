package com.walshe.aimarket.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.walshe.aimarket.config.AiPricingProperties;
import com.walshe.aimarket.domain.CostLog;
import com.walshe.aimarket.service.dto.CostLogEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

@ExtendWith(MockitoExtension.class)
class CostTrackingServiceImplTest {

    @Mock
    private KafkaTemplate<String, CostLogEvent> kafkaTemplate;

    private CostTrackingServiceImpl costTrackingService;

    @BeforeEach
    void setUp() {
        costTrackingService = new CostTrackingServiceImpl(kafkaTemplate, "ai-cost-logs");
    }

    @Test
    void shouldSendEmbeddingUsageToKafka() {
        String correlationId = "550e8400-e29b-41d4-a716-446655440000";
        costTrackingService.logEmbeddingUsage("text-embedding-3-small", 1000, 1L, correlationId, "openai", 100L);

        ArgumentCaptor<CostLogEvent> eventCaptor = ArgumentCaptor.forClass(CostLogEvent.class);
        verify(kafkaTemplate).send(eq("ai-cost-logs"), eventCaptor.capture());

        CostLogEvent event = eventCaptor.getValue();
        assertThat(event.callType()).isEqualTo("EMBEDDING");
        assertThat(event.modelName()).isEqualTo("text-embedding-3-small");
        assertThat(event.inputTokens()).isEqualTo(1000);
        assertThat(event.documentId()).isEqualTo(1L);
        assertThat(event.correlationId().toString()).isEqualTo(correlationId);
        assertThat(event.provider()).isEqualTo("openai");
        assertThat(event.latencyMs()).isEqualTo(100L);
    }

    @Test
    void shouldSendCompletionUsageToKafka() {
        String correlationId = "550e8400-e29b-41d4-a716-446655440000";
        costTrackingService.logCompletionUsage("gpt-4o-mini", 1000, 2000, correlationId, "openai", 200L);

        ArgumentCaptor<CostLogEvent> eventCaptor = ArgumentCaptor.forClass(CostLogEvent.class);
        verify(kafkaTemplate).send(eq("ai-cost-logs"), eventCaptor.capture());

        CostLogEvent event = eventCaptor.getValue();
        assertThat(event.callType()).isEqualTo("COMPLETION");
        assertThat(event.modelName()).isEqualTo("gpt-4o-mini");
        assertThat(event.inputTokens()).isEqualTo(1000);
        assertThat(event.outputTokens()).isEqualTo(2000);
        assertThat(event.correlationId().toString()).isEqualTo(correlationId);
        assertThat(event.provider()).isEqualTo("openai");
        assertThat(event.latencyMs()).isEqualTo(200L);
    }

    @Test
    void shouldNotPropagateExceptionOnKafkaFailure() {
        when(kafkaTemplate.send(anyString(), any(CostLogEvent.class))).thenThrow(new RuntimeException("Kafka error"));

        costTrackingService.logEmbeddingUsage("text-embedding-3-small", 1000, null, null, null, null);

        verify(kafkaTemplate).send(anyString(), any(CostLogEvent.class));
    }
}
