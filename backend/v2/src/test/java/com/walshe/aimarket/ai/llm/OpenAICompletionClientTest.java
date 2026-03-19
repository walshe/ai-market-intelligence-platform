package com.walshe.aimarket.ai.llm;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.walshe.aimarket.config.LlmProperties;
import com.walshe.aimarket.service.CostTrackingService;
import com.walshe.aimarket.ai.llm.OpenAICompletionClient.ChatStreamResponse;
import com.walshe.aimarket.ai.llm.OpenAICompletionClient.Delta;
import com.walshe.aimarket.ai.llm.OpenAICompletionClient.StreamChoice;
import com.walshe.aimarket.ai.llm.OpenAICompletionClient.Usage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OpenAICompletionClientTest {

    @Mock
    private WebClient.Builder webClientBuilder;

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private WebClient.RequestBodySpec requestBodySpec;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    private OpenAICompletionClient client;

    @Mock
    private CostTrackingService costTrackingService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        LlmProperties.OpenAIProperties openAIProperties = new LlmProperties.OpenAIProperties(
            "test-key",
            "gpt-4o-mini",
            "https://api.openai.com"
        );
        LlmProperties properties = new LlmProperties("openai", openAIProperties, null);

        when(webClientBuilder.baseUrl(any())).thenReturn(webClientBuilder);
        when(webClientBuilder.defaultHeader(any(), any())).thenReturn(webClientBuilder);
        when(webClientBuilder.build()).thenReturn(webClient);

        // We don't need to mock RestClient for streaming tests
        client = new OpenAICompletionClient(
            org.springframework.web.client.RestClient.builder(),
            webClientBuilder,
            properties,
            costTrackingService,
            objectMapper
        );
    }

    @Test
    void streamCompletion_shouldParseSSEStreamCorrectly() throws Exception {
        String prompt = "Hello";
        String correlationId = "test-id";

        ChatStreamResponse chunk1 = new ChatStreamResponse(null, null, 0, null, new StreamChoice[]{new StreamChoice(0, new Delta("Hello"), null)}, null);
        ChatStreamResponse chunk2 = new ChatStreamResponse(null, null, 0, null, new StreamChoice[]{new StreamChoice(0, new Delta(" world"), null)}, null);

        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri("/v1/chat/completions")).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.accept(MediaType.TEXT_EVENT_STREAM)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToFlux(ChatStreamResponse.class)).thenReturn(Flux.just(chunk1, chunk2));

        Flux<String> result = client.streamCompletion(prompt, correlationId);

        List<String> tokens = result.collectList().block();
        assertThat(tokens).containsExactly("Hello", " world");
    }

    @Test
    void streamCompletion_shouldHandleUsageInSeparateChunk() throws Exception {
        String prompt = "Hello";
        String correlationId = "test-id";

        ChatStreamResponse chunk1 = new ChatStreamResponse(null, null, 0, null, new StreamChoice[]{new StreamChoice(0, new Delta("Hello"), null)}, null);
        ChatStreamResponse chunk2 = new ChatStreamResponse(null, null, 0, "gpt-4o-mini-2024-07-18", new StreamChoice[]{}, new Usage(10, 5, 15));

        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri("/v1/chat/completions")).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.accept(MediaType.TEXT_EVENT_STREAM)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToFlux(ChatStreamResponse.class)).thenReturn(Flux.just(chunk1, chunk2));

        Flux<String> result = client.streamCompletion(prompt, correlationId);

        List<String> tokens = result.collectList().block();
        assertThat(tokens).containsExactly("Hello");

        // verify(costTrackingService).logCompletionUsage(...) could be added here
    }
}
