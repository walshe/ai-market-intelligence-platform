package com.walshe.aimarket.service.impl;

import com.walshe.aimarket.ai.embedding.EmbeddingClient;
import com.walshe.aimarket.ai.llm.CompletionResponse;
import com.walshe.aimarket.ai.llm.LLMCompletionClient;
import com.walshe.aimarket.domain.DocumentChunk;
import com.walshe.aimarket.service.EmbeddingService;
import com.walshe.aimarket.service.PromptBuilderService;
import com.walshe.aimarket.service.RetrievalService;
import com.walshe.aimarket.service.dto.AnalysisResponseDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Flux;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalysisServiceImplTest {

    @Mock
    private EmbeddingClient embeddingClient;

    @Mock
    private EmbeddingService embeddingService;

    @Mock
    private RetrievalService retrievalService;

    @Mock
    private PromptBuilderService promptBuilderService;

    @Mock
    private LLMCompletionClient llmClient;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private AnalysisServiceImpl analysisService;

    @Test
    void analyze_shouldInvokeAllServicesAndReturnDTO() throws Exception {
        // Given
        String query = "financial analysis for Q4";
        float[] mockEmbedding = new float[]{0.1f, 0.2f};
        List<DocumentChunk> mockChunks = Collections.emptyList();
        String mockPrompt = "System: ... Context: ... Query: " + query;
        String mockJsonResponse = "{\"summary\":\"Good\",\"riskFactors\":[],\"confidenceScore\":0.9}";

        CompletionResponse mockLlmResult = new CompletionResponse(
            mockJsonResponse,
            40,
            60,
            "gpt-4",
            "openai",
            100L
        );

        when(embeddingClient.generateEmbedding(eq(query))).thenReturn(mockEmbedding);
        when(retrievalService.retrieveSimilar(eq(mockEmbedding), eq(5))).thenReturn(mockChunks);
        when(promptBuilderService.buildPrompt(query, mockChunks)).thenReturn(mockPrompt);
        when(llmClient.complete(eq(mockPrompt), eq(null))).thenReturn(mockLlmResult);

        // Mock JSON parsing
        JsonNode mockRootNode = mock(JsonNode.class);
        JsonNode mockSummaryNode = mock(JsonNode.class);
        JsonNode mockRiskNode = mock(JsonNode.class);
        JsonNode mockConfNode = mock(JsonNode.class);

        when(objectMapper.readTree(mockJsonResponse)).thenReturn(mockRootNode);
        when(mockRootNode.isObject()).thenReturn(true);
        when(mockRootNode.get("summary")).thenReturn(mockSummaryNode);
        when(mockSummaryNode.isTextual()).thenReturn(true);
        when(mockSummaryNode.asText("")).thenReturn("Good");
        when(mockRootNode.get("riskFactors")).thenReturn(mockRiskNode);
        when(mockRiskNode.isArray()).thenReturn(true);
        when(mockRiskNode.elements()).thenReturn(Collections.emptyIterator());
        when(mockRootNode.get("confidenceScore")).thenReturn(mockConfNode);
        when(mockConfNode.isNumber()).thenReturn(true);
        when(mockConfNode.asDouble()).thenReturn(0.9);

        // When
        AnalysisResponseDTO result = analysisService.analyze(query, 5);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.summary()).isEqualTo("Good");
        assertThat(result.confidenceScore()).isEqualTo(0.9);
        assertThat(result.modelUsed()).isEqualTo("gpt-4");
        assertThat(result.tokensUsed()).isEqualTo(100);

        verify(embeddingClient).generateEmbedding(eq(query));
        verify(retrievalService).retrieveSimilar(eq(mockEmbedding), eq(5));
        verify(promptBuilderService).buildPrompt(query, mockChunks);
        verify(llmClient).complete(eq(mockPrompt), eq(null));
    }

    @Test
    void streamAnalysis_shouldStreamSseEvents() {
        // Given
        String query = "financial analysis for Q4";
        float[] mockEmbedding = new float[]{0.1f, 0.2f};
        List<DocumentChunk> mockChunks = Collections.emptyList();
        String mockPrompt = "System: ... Context: ... Query: " + query;
        String correlationId = "test-id";

        when(embeddingClient.generateEmbedding(eq(query))).thenReturn(mockEmbedding);
        when(retrievalService.retrieveSimilar(eq(mockEmbedding), any())).thenReturn(mockChunks);
        when(promptBuilderService.buildStreamingPrompt(eq(query), eq(mockChunks))).thenReturn(mockPrompt);
        when(llmClient.streamCompletion(eq(mockPrompt), eq(correlationId))).thenReturn(Flux.just("token1", "token2"));

        // When
        Flux<ServerSentEvent<String>> result = analysisService.streamAnalysis(query, null, correlationId);

        // Then
        List<ServerSentEvent<String>> events = result.collectList().block();
        assertThat(events).hasSize(3);
        assertThat(events.get(0).event()).isEqualTo("token");
        assertThat(events.get(0).data()).isEqualTo("token1");
        assertThat(events.get(1).event()).isEqualTo("token");
        assertThat(events.get(1).data()).isEqualTo("token2");
        assertThat(events.get(2).event()).isEqualTo("done");

        verify(embeddingClient).generateEmbedding(eq(query));
        verify(retrievalService).retrieveSimilar(eq(mockEmbedding), any());
        verify(promptBuilderService).buildStreamingPrompt(eq(query), eq(mockChunks));
        verify(llmClient).streamCompletion(eq(mockPrompt), eq(correlationId));
    }
}
