package com.walshe.aimarket.service;

import com.walshe.aimarket.domain.DocumentChunk;
import java.util.List;

/**
 * Orchestration service for RAG analysis flow.
 */
public interface AnalysisService {

    /**
     * Perform analysis by embedding query, retrieving context and calling LLM.
     *
     * @param query user query string
     * @param topK optional override for retrieval count
     * @return analysis result (placeholder for now)
     */
    void analyze(String query, Integer topK);
}
