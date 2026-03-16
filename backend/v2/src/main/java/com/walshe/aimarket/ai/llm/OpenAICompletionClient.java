package com.walshe.aimarket.ai.llm;

import com.walshe.aimarket.config.LlmProperties;
import com.walshe.aimarket.service.CostTrackingService;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

/**
 * OpenAI implementation of {@link LLMCompletionClient}.
 */
public class OpenAICompletionClient implements LLMCompletionClient {

    private final RestClient restClient;
    private final WebClient webClient;
    private final LlmProperties properties;
    private final CostTrackingService costTrackingService;

    public OpenAICompletionClient(
        RestClient.Builder restClientBuilder,
        WebClient.Builder webClientBuilder,
        LlmProperties properties,
        CostTrackingService costTrackingService
    ) {
        this.properties = properties;
        this.costTrackingService = costTrackingService;
        this.restClient = restClientBuilder
            .baseUrl(properties.openai().baseUrl())
            .defaultHeader("Authorization", "Bearer " + properties.openai().apiKey())
            .build();
        this.webClient = webClientBuilder
            .baseUrl(properties.openai().baseUrl())
            .defaultHeader("Authorization", "Bearer " + properties.openai().apiKey())
            .build();
    }

    @Override
    public CompletionResponse complete(String prompt, String correlationId) {
        long startTime = System.currentTimeMillis();

        ChatCompletionRequest request = new ChatCompletionRequest(
            properties.openai().modelName(),
            new Message[] { new Message("user", prompt) }
        );

        ChatCompletionResponse response = restClient
            .post()
            .uri("/v1/chat/completions")
            .contentType(MediaType.APPLICATION_JSON)
            .body(request)
            .retrieve()
            .body(ChatCompletionResponse.class);

        long latencyMs = System.currentTimeMillis() - startTime;

        if (response == null || response.choices() == null || response.choices().length == 0
            || response.choices()[0] == null || response.choices()[0].message() == null) {
            throw new RuntimeException("Empty or invalid response from OpenAI chat completions API");
        }

        String content = response.choices()[0].message().content();
        String modelUsed = response.model() != null ? response.model() : properties.openai().modelName();
        int promptTokens = 0;
        int completionTokens = 0;

        if (response.usage() != null) {
            promptTokens = response.usage().prompt_tokens();
            completionTokens = response.usage().completion_tokens();

            costTrackingService.logCompletionUsage(
                modelUsed,
                promptTokens,
                completionTokens,
                correlationId,
                "openai",
                latencyMs
            );
        }

        return new CompletionResponse(
            content,
            promptTokens,
            completionTokens,
            modelUsed,
            "openai",
            latencyMs
        );
    }

    @Override
    public Flux<String> streamCompletion(String prompt, String correlationId) {
        long startTime = System.currentTimeMillis();
        ChatCompletionRequest request = new ChatCompletionRequest(
            properties.openai().modelName(),
            new Message[] { new Message("user", prompt) },
            true
        );

        return webClient
            .post()
            .uri("/v1/chat/completions")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .retrieve()
            .bodyToFlux(ChatStreamResponse.class)
            .doOnNext(response -> {
                if (response.usage() != null) {
                    long latencyMs = System.currentTimeMillis() - startTime;
                    costTrackingService.logCompletionUsage(
                        response.model() != null ? response.model() : properties.openai().modelName(),
                        response.usage().prompt_tokens(),
                        response.usage().completion_tokens(),
                        correlationId,
                        "openai",
                        latencyMs
                    );
                }
            })
            .filter(response -> response.choices() != null && response.choices().length > 0)
            .map(response -> response.choices()[0].delta().content())
            .filter(content -> content != null && !content.isEmpty());
    }

    private record ChatCompletionRequest(String model, Message[] messages, boolean stream, StreamOptions stream_options) {
        ChatCompletionRequest(String model, Message[] messages, boolean stream) {
            this(model, messages, stream, stream ? new StreamOptions(true) : null);
        }
        ChatCompletionRequest(String model, Message[] messages) {
            this(model, messages, false, null);
        }
    }

    private record StreamOptions(boolean include_usage) {}

    private record Message(String role, String content) {}

    private record ChatCompletionResponse(String id, String object, long created, String model,
                                          Choice[] choices, Usage usage) {}

    private record Choice(int index, Message message, Object logprobs, String finish_reason) {}

    private record Usage(int prompt_tokens, int completion_tokens, int total_tokens) {}

    private record ChatStreamResponse(String id, String object, long created, String model,
                                      StreamChoice[] choices, Usage usage) {}

    private record StreamChoice(int index, Delta delta, String finish_reason) {}

    private record Delta(String content) {}
}
