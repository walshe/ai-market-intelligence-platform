package com.walshe.aimarket.service.impl;

import com.walshe.aimarket.config.LlmProperties;
import com.walshe.aimarket.service.LlmClient;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

/**
 * OpenAI implementation of {@link LlmClient} using chat completions API.
 */
@Service
class OpenAiLlmClient implements LlmClient {

    private final RestClient restClient;
    private final LlmProperties properties;

    OpenAiLlmClient(RestClient.Builder restClientBuilder, LlmProperties properties) {
        this.properties = properties;
        this.restClient = restClientBuilder
            .baseUrl(properties.baseUrl())
            .defaultHeader("Authorization", "Bearer " + properties.apiKey())
            .build();
    }

    @Override
    public LlmResult generate(String prompt) {
        ChatCompletionRequest request = new ChatCompletionRequest(
            properties.modelName(),
            new Message[] { new Message("user", prompt) }
        );

        ChatCompletionResponse response = restClient
            .post()
            .uri("/v1/chat/completions")
            .contentType(MediaType.APPLICATION_JSON)
            .body(request)
            .retrieve()
            .body(ChatCompletionResponse.class);

        if (response == null || response.choices() == null || response.choices().length == 0
            || response.choices()[0] == null || response.choices()[0].message() == null) {
            throw new RuntimeException("Empty or invalid response from OpenAI chat completions API");
        }

        String content = response.choices()[0].message().content();
        String modelUsed = response.model() != null ? response.model() : properties.modelName();
        int tokensUsed = response.usage() != null ? response.usage().total_tokens() : 0;

        return new LlmResult(content, modelUsed, tokensUsed);
    }

    private record ChatCompletionRequest(String model, Message[] messages) {}

    private record Message(String role, String content) {}

    private record ChatCompletionResponse(String id, String object, long created, String model,
                                          Choice[] choices, Usage usage) {}

    private record Choice(int index, Message message, Object logprobs, String finish_reason) {}

    private record Usage(int prompt_tokens, int completion_tokens, int total_tokens) {}
}
