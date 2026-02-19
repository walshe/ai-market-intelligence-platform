package com.walshe.aimarket.service;

import java.util.List;

/**
 * Service interface for chunking document content.
 */
public interface ChunkingService {
    /**
     * Splits the given content into a list of chunks.
     *
     * @param content the content to split.
     * @return a list of chunks.
     */
    List<String> chunk(String content);
}
