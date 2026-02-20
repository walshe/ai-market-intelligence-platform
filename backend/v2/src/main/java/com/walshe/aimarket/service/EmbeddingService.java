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
     * Gets the name of the model being used by this service.
     *
     * @return the model name.
     */
    String getModelName();
}
