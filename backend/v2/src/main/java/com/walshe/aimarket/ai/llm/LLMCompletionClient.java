package com.walshe.aimarket.ai.llm;

import reactor.core.publisher.Flux;

/**
 * Client interface for Large Language Model text generation.
 */
public interface LLMCompletionClient {

    /**
     * Generate model output for the given prompt.
     *
     * @param prompt the full prompt content
     * @param correlationId (optional) the correlation ID for grouping calls
     * @return result containing generated content, usage metadata, and performance metrics
     */
    CompletionResponse complete(String prompt, String correlationId);

    /**
     * Stream model output for the given prompt.
     *
     * @param prompt the full prompt content
     * @param correlationId (optional) the correlation ID for grouping calls
     * @return a Flux of generated text chunks
     */
    Flux<String> streamCompletion(String prompt, String correlationId);
}
