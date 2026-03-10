package com.walshe.aimarket.service;

import java.util.List;

/**
 * Service interface for generating embeddings for text.
 */
public interface EmbeddingService {
    /**
     * Generates an embedding for the given text.
     *
     * @param text the text to embed.
     * @return the embedding vector as a list of doubles.
     */
    float[] embed(String text);

    /**
     * Generates an embedding for the given text with additional context for cost tracking.
     *
     * @param text the text to embed.
     * @param documentId (optional) the document ID associated with this call.
     * @param correlationId (optional) the correlation ID for grouping calls.
     * @return the embedding vector.
     */
    default float[] embed(String text, Long documentId, String correlationId) {
        return embed(text);
    }

    /**
     * Gets the name of the model being used by this service.
     *
     * @return the model name.
     */
    String getModelName();
}
