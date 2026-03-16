package com.walshe.aimarket.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Properties for OpenAI chat completion API.
 */
@ConfigurationProperties(prefix = "application.llm")
@Validated
public record LlmProperties(
    @NotBlank String provider,
    OpenAIProperties openai,
    BedrockProperties bedrock
) {
    public record OpenAIProperties(
        @NotBlank String apiKey,
        @NotBlank String modelName,
        @NotBlank String baseUrl
    ) {}

    public record BedrockProperties(
        @NotBlank String region,
        @NotBlank String modelName,
        String accessKey,
        String secretKey
    ) {}
}
