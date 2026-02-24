package com.walshe.aimarket.service.impl;

import com.walshe.aimarket.domain.DocumentChunk;
import com.walshe.aimarket.service.EmbeddingService;
import com.walshe.aimarket.service.LlmClient;
import com.walshe.aimarket.service.PromptBuilderService;
import com.walshe.aimarket.service.RetrievalService;
import com.walshe.aimarket.service.dto.AnalysisResponseDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalysisServiceImplTest {

    @Mock
    private EmbeddingService embeddingService;

    @Mock
    private RetrievalService retrievalService;

    @Mock
    private PromptBuilderService promptBuilderService;

    @Mock
    private LlmClient llmClient;

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

        LlmClient.LlmResult mockLlmResult = new LlmClient.LlmResult(mockJsonResponse, "gpt-4", 100);

        when(embeddingService.embed(query)).thenReturn(mockEmbedding);
        when(retrievalService.retrieveSimilar(eq(mockEmbedding), eq(5))).thenReturn(mockChunks);
        when(promptBuilderService.buildPrompt(query, mockChunks)).thenReturn(mockPrompt);
        when(llmClient.generate(mockPrompt)).thenReturn(mockLlmResult);

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

        verify(embeddingService).embed(query);
        verify(retrievalService).retrieveSimilar(eq(mockEmbedding), eq(5));
        verify(promptBuilderService).buildPrompt(query, mockChunks);
        verify(llmClient).generate(mockPrompt);
    }
}
