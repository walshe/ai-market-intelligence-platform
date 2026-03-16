package com.walshe.aimarket.service;

import com.walshe.aimarket.domain.CostLog;

/**
 * Service for tracking LLM usage costs.
 */
public interface CostTrackingService {
    /**
     * Logs usage for an embedding call.
     *
     * @param modelName the name of the model used
     * @param inputTokens the number of input tokens
     * @param documentId (optional) the document ID associated with this call
     * @param correlationId (optional) the correlation ID for grouping calls
     * @param provider (optional) the provider name
     * @param latencyMs (optional) the latency of the request in milliseconds
     * @return the created CostLog entry
     */
    CostLog logEmbeddingUsage(String modelName, Integer inputTokens, Long documentId, String correlationId, String provider, Long latencyMs);

    /**
     * Logs usage for a completion call.
     *
     * @param modelName the name of the model used
     * @param inputTokens the number of input tokens
     * @param outputTokens the number of output tokens
     * @param correlationId (optional) the correlation ID for grouping calls
     * @param provider (optional) the provider name
     * @param latencyMs (optional) the latency of the request in milliseconds
     * @return the created CostLog entry
     */
    CostLog logCompletionUsage(String modelName, Integer inputTokens, Integer outputTokens, String correlationId, String provider, Long latencyMs);
}
