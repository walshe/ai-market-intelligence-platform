package com.walshe.aimarket.service;

/**
 * Client interface for Large Language Model generation.
 */
public interface LlmClient {

    /**
     * Generate model output for the given prompt.
     *
     * @param prompt the full prompt content
     * @return result containing generated content, model used, and tokens used
     */
    LlmResult generate(String prompt);

    /**
     * Result of an LLM generation call.
     * @param content generated text content
     * @param modelUsed model identifier actually used
     * @param tokensUsed total tokens used as reported by provider
     */
    record LlmResult(String content, String modelUsed, int tokensUsed) {}
}
