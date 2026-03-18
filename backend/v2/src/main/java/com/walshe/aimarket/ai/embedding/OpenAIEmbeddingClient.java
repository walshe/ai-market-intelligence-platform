package com.walshe.aimarket.ai.embedding;

import com.walshe.aimarket.config.EmbeddingProperties;
import com.walshe.aimarket.service.CostTrackingService;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * OpenAI implementation of {@link EmbeddingClient}.
 */
@Component
public class OpenAIEmbeddingClient implements EmbeddingClient {

    private final RestClient restClient;
    private final EmbeddingProperties properties;
    private final CostTrackingService costTrackingService;

    public OpenAIEmbeddingClient(RestClient.Builder restClientBuilder, EmbeddingProperties properties, CostTrackingService costTrackingService) {
        this.properties = properties;
        this.costTrackingService = costTrackingService;
        this.restClient = restClientBuilder
            .baseUrl(properties.baseUrl())
            .defaultHeader("Authorization", "Bearer " + properties.apiKey())
            .build();
    }

    @Override
    public float[] generateEmbedding(String text) {
        // Use the configured EMBEDDING model (e.g. text-embedding-3-small)
        // This model converts text to a mathematical vector for semantic search.
        EmbeddingRequest request = new EmbeddingRequest(properties.modelName(), text);

        long start = System.currentTimeMillis();
        EmbeddingResponse response = restClient
            .post()
            .uri("/v1/embeddings")
            .contentType(MediaType.APPLICATION_JSON)
            .body(request)
            .retrieve()
            .body(EmbeddingResponse.class);
        long latency = System.currentTimeMillis() - start;

        if (response == null || response.data() == null || response.data().isEmpty()) {
            throw new RuntimeException("Empty response from OpenAI embedding API");
        }

        if (response.usage() != null) {
            costTrackingService.logEmbeddingUsage(
                properties.modelName(),
                response.usage().prompt_tokens(),
                null,
                null,
                "openai",
                latency
            );
        }

        return response.data().get(0).embedding();
    }

    private record EmbeddingRequest(String model, String input) {}

    private record EmbeddingResponse(List<EmbeddingData> data, Usage usage) {}

    private record EmbeddingData(float[] embedding, int index, String object) {}

    private record Usage(int prompt_tokens, int total_tokens) {}
}
