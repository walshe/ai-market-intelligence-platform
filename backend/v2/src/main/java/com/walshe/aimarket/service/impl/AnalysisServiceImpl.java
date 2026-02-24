package com.walshe.aimarket.service.impl;

import com.walshe.aimarket.domain.DocumentChunk;
import com.walshe.aimarket.service.AnalysisService;
import com.walshe.aimarket.service.EmbeddingService;
import com.walshe.aimarket.service.RetrievalService;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of {@link AnalysisService} for RAG flow orchestration.
 */
@Service
@Transactional(readOnly = true)
class AnalysisServiceImpl implements AnalysisService {

    private final EmbeddingService embeddingService;
    private final RetrievalService retrievalService;

    AnalysisServiceImpl(EmbeddingService embeddingService, RetrievalService retrievalService) {
        this.embeddingService = embeddingService;
        this.retrievalService = retrievalService;
    }

    @Override
    public void analyze(String query, Integer topK) {
        // Step 1: Embed the user query
        float[] queryEmbedding = embeddingService.embed(query);

        // Step 2: Retrieve similar chunks (part of Phase 2 logic to verify flow)
        List<DocumentChunk> similarChunks = retrievalService.retrieveSimilar(queryEmbedding, topK);

        // Future phases will build prompt and call LLM
    }
}
