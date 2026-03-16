package com.walshe.aimarket.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.walshe.aimarket.ai.llm.BedrockClient;
import com.walshe.aimarket.ai.llm.LLMCompletionClient;
import com.walshe.aimarket.ai.llm.OpenAICompletionClient;
import com.walshe.aimarket.service.CostTrackingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
class LLMProviderConfiguration {

    private static final Logger LOG = LoggerFactory.getLogger(LLMProviderConfiguration.class);

    private final LlmProperties properties;

    LLMProviderConfiguration(LlmProperties properties) {
        this.properties = properties;
    }

    @Bean
    @ConditionalOnProperty(name = "application.llm.provider", havingValue = "openai")
    LLMCompletionClient openAICompletionClient(
        RestClient.Builder restClientBuilder,
        WebClient.Builder webClientBuilder,
        CostTrackingService costTrackingService
    ) {
        LOG.info("Configuring OpenAI completion client for model: {}", properties.openai().modelName());
        return new OpenAICompletionClient(restClientBuilder, webClientBuilder, properties, costTrackingService);
    }

    @Bean
    @ConditionalOnProperty(name = "application.llm.provider", havingValue = "bedrock")
    LLMCompletionClient bedrockCompletionClient(
        CostTrackingService costTrackingService,
        ObjectMapper objectMapper
    ) {
        LOG.info("Configuring Amazon Bedrock completion client for model: {}", properties.bedrock().modelName());
        return new BedrockClient(properties, costTrackingService, objectMapper);
    }
}
