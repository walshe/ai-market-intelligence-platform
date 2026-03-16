package com.walshe.aimarket.ai.llm;

/**
 * Result of an LLM generation call.
 *
 * @param generatedText   generated text content
 * @param inputTokens     tokens used in the prompt
 * @param outputTokens    tokens used in the generated completion
 * @param modelName       model identifier actually used
 * @param provider        provider name (e.g., "openai", "bedrock")
 * @param latencyMs       latency of the request in milliseconds
 */
public record CompletionResponse(
    String generatedText,
    int inputTokens,
    int outputTokens,
    String modelName,
    String provider,
    long latencyMs
) {}
