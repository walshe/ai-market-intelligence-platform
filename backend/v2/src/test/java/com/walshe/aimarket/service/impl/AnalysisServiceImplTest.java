package com.walshe.aimarket.service.impl;

import com.walshe.aimarket.service.EmbeddingService;
import com.walshe.aimarket.service.RetrievalService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalysisServiceImplTest {

    @Mock
    private EmbeddingService embeddingService;

    @Mock
    private RetrievalService retrievalService;

    @InjectMocks
    private AnalysisServiceImpl analysisService;

    @Test
    void analyze_shouldInvokeEmbeddingAndRetrieval() {
        // Given
        String query = "financial analysis for Q4";
        float[] mockEmbedding = new float[]{0.1f, 0.2f};
        when(embeddingService.embed(query)).thenReturn(mockEmbedding);

        // When
        analysisService.analyze(query, 5);

        // Then
        verify(embeddingService).embed(query);
        verify(retrievalService).retrieveSimilar(eq(mockEmbedding), eq(5));
    }

    @Test
    void analyze_shouldWorkWithNullTopK() {
        // Given
        String query = "financial analysis for Q4";
        float[] mockEmbedding = new float[]{0.1f, 0.2f};
        when(embeddingService.embed(query)).thenReturn(mockEmbedding);

        // When
        analysisService.analyze(query, null);

        // Then
        verify(embeddingService).embed(query);
        verify(retrievalService).retrieveSimilar(eq(mockEmbedding), eq(null));
    }
}
