package com.walshe.aimarket.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Properties for OpenAI chat completion API.
 */
@ConfigurationProperties(prefix = "application.llm.openai")
@Validated
public record LlmProperties(
    @NotBlank String apiKey,
    @NotBlank String modelName,
    @NotBlank String baseUrl
) {}
