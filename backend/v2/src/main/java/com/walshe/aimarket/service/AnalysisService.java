package com.walshe.aimarket.service;

import com.walshe.aimarket.service.dto.AnalysisResponseDTO;

/**
 * Orchestration service for RAG analysis flow.
 */
public interface AnalysisService {

    /**
     * Perform analysis by embedding query, retrieving context and calling LLM.
     *
     * @param query user query string
     * @param topK optional override for retrieval count
     * @return structured analysis response
     */
    AnalysisResponseDTO analyze(String query, Integer topK);

    /**
     * Perform analysis with correlation tracking.
     *
     * @param query user query string
     * @param topK optional override for retrieval count
     * @param correlationId (optional) the correlation ID for grouping calls
     * @return structured analysis response
     */
    default AnalysisResponseDTO analyze(String query, Integer topK, String correlationId) {
        return analyze(query, topK);
    }
}
