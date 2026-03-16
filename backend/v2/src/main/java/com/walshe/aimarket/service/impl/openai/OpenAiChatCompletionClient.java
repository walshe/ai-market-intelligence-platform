package com.walshe.aimarket.service.impl.openai;

import com.walshe.aimarket.config.LlmProperties;
import com.walshe.aimarket.service.CostTrackingService;
import com.walshe.aimarket.service.ChatCompletionClient;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

/**
 * OpenAI implementation of {@link ChatCompletionClient} using chat completions API.
 */
@Service
public class OpenAiChatCompletionClient implements ChatCompletionClient {

    private final RestClient restClient;
    private final LlmProperties properties;
    private final CostTrackingService costTrackingService;

    OpenAiChatCompletionClient(RestClient.Builder restClientBuilder, LlmProperties properties, CostTrackingService costTrackingService) {
        this.properties = properties;
        this.costTrackingService = costTrackingService;
        this.restClient = restClientBuilder
            .baseUrl(properties.baseUrl())
            .defaultHeader("Authorization", "Bearer " + properties.apiKey())
            .build();
    }

    @Override
    public ChatCompletionResult generate(String prompt) {
        return generate(prompt, null);
    }

    @Override
    public ChatCompletionResult generate(String prompt, String correlationId) {
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
        int promptTokens = 0;
        int completionTokens = 0;
        int totalTokens = 0;

        if (response.usage() != null) {
            promptTokens = response.usage().prompt_tokens();
            completionTokens = response.usage().completion_tokens();
            totalTokens = response.usage().total_tokens();

            costTrackingService.logCompletionUsage(
                modelUsed,
                promptTokens,
                completionTokens,
                correlationId
            );
        }

        return new ChatCompletionResult(content, modelUsed, promptTokens, completionTokens, totalTokens);
    }

    private record ChatCompletionRequest(String model, Message[] messages) {}

    private record Message(String role, String content) {}

    private record ChatCompletionResponse(String id, String object, long created, String model,
                                          Choice[] choices, Usage usage) {}

    private record Choice(int index, Message message, Object logprobs, String finish_reason) {}

    private record Usage(int prompt_tokens, int completion_tokens, int total_tokens) {}
}
