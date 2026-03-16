package com.walshe.aimarket.ai.embedding;

import java.util.List;

/**
 * Client interface for generating text embeddings.
 */
public interface EmbeddingClient {
    /**
     * Generates an embedding for the given text.
     *
     * @param text the text to embed.
     * @return the embedding vector.
     */
    float[] generateEmbedding(String text);
}
