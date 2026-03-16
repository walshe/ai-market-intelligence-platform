package com.walshe.aimarket.service;

/**
 * Client interface for Large Language Model generation.
 */
public interface ChatCompletionClient {

    /**
     * Generate model output for the given prompt.
     *
     * @param prompt the full prompt content
     * @return result containing generated content, model used, and tokens used
     */
    ChatCompletionResult generate(String prompt);

    /**
     * Generate model output for the given prompt with correlation tracking.
     *
     * @param prompt the full prompt content
     * @param correlationId (optional) the correlation ID for grouping calls
     * @return result containing generated content, model used, and tokens used
     */
    default ChatCompletionResult generate(String prompt, String correlationId) {
        return generate(prompt);
    }

    /**
     * Result of an LLM generation call.
     * @param content generated text content
     * @param modelUsed model identifier actually used
     * @param promptTokens tokens used in the prompt
     * @param completionTokens tokens used in the generated completion
     * @param totalTokens total tokens used as reported by provider
     */
    record ChatCompletionResult(String content, String modelUsed, int promptTokens, int completionTokens, int totalTokens) {}
}
