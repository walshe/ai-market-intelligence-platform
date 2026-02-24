package com.walshe.aimarket.service;

import com.walshe.aimarket.domain.DocumentChunk;
import java.util.List;

/**
 * Builds a deterministic prompt for the financial analysis assistant.
 * The prompt must include:
 * - A fixed system instruction (assistant role)
 * - The ordered context chunks (numbered, in the same order as provided)
 * - The user query
 * - An explicit instruction to return JSON matching the response contract
 */
public interface PromptBuilderService {

    /**
     * Build a deterministic prompt string from the provided inputs.
     *
     * @param query the user's query
     * @param contextChunks ordered list of context chunks; order must be preserved in the prompt
     * @return the fully constructed prompt
     */
    String buildPrompt(String query, List<DocumentChunk> contextChunks);
}
