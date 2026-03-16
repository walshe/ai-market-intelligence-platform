package com.walshe.aimarket.service.impl.openai;

import com.walshe.aimarket.config.EmbeddingProperties;
import com.walshe.aimarket.service.CostTrackingService;
import com.walshe.aimarket.service.EmbeddingService;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

/**
 * Concrete implementation of {@link EmbeddingService} for OpenAI.
 */
@Service
public class OpenAiEmbeddingService implements EmbeddingService {

    private final RestClient restClient;
    private final EmbeddingProperties properties;
    private final CostTrackingService costTrackingService;

    OpenAiEmbeddingService(RestClient.Builder restClientBuilder, EmbeddingProperties properties, CostTrackingService costTrackingService) {
        this.properties = properties;
        this.costTrackingService = costTrackingService;
        this.restClient = restClientBuilder
            .baseUrl(properties.baseUrl())
            .defaultHeader("Authorization", "Bearer " + properties.apiKey())
            .build();
    }

    @Override
    public float[] embed(String text) {
        return embed(text, null, null);
    }

    @Override
    public float[] embed(String text, Long documentId, String correlationId) {
        EmbeddingRequest request = new EmbeddingRequest(properties.modelName(), text);

        EmbeddingResponse response = restClient
            .post()
            .uri("/v1/embeddings")
            .contentType(MediaType.APPLICATION_JSON)
            .body(request)
            .retrieve()
            .body(EmbeddingResponse.class);

        if (response == null || response.data() == null || response.data().isEmpty()) {
            throw new RuntimeException("Empty response from OpenAI embedding API");
        }

        if (response.usage() != null) {
            costTrackingService.logEmbeddingUsage(
                properties.modelName(),
                response.usage().prompt_tokens(),
                documentId,
                correlationId
            );
        }

        return response.data().get(0).embedding();
    }

    @Override
    public String getModelName() {
        return properties.modelName();
    }

    private record EmbeddingRequest(String model, String input) {}

    record EmbeddingResponse(List<EmbeddingData> data, Usage usage) {}

    record EmbeddingData(float[] embedding, int index, String object) {}

    record Usage(int prompt_tokens, int total_tokens) {}
}
