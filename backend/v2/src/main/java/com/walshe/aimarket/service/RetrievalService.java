package com.walshe.aimarket.service;

import com.walshe.aimarket.domain.DocumentChunk;
import java.util.List;

/**
 * Service to retrieve similar document chunks using pgvector cosine similarity.
 */
public interface RetrievalService {

    /**
     * Retrieve top-k most similar chunks ordered by highest similarity first.
     *
     * @param queryEmbedding embedding vector for the query (same dimensionality as stored embeddings)
     * @param topK optional override for number of results; if null or <= 0, use configured default
     * @return ordered list of similar chunks
     */
    List<DocumentChunk> retrieveSimilar(float[] queryEmbedding, Integer topK);
}
