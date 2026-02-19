package com.walshe.aimarket.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Properties for OpenAI embedding API.
 */
@ConfigurationProperties(prefix = "application.embedding.openai")
@Validated
public record EmbeddingProperties(
    @NotBlank String apiKey,
    @NotBlank String modelName,
    @NotBlank String baseUrl
) {}
