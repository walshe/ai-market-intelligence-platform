package com.walshe.aimarket.service.impl.openai;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.walshe.aimarket.config.EmbeddingProperties;
import com.walshe.aimarket.service.CostTrackingService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class OpenAiEmbeddingServiceTest {

    private EmbeddingProperties properties;
    private OpenAiEmbeddingService embeddingService;
    private MockRestServiceServer mockServer;
    private CostTrackingService costTrackingService;
    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        properties = new EmbeddingProperties("test-key", "text-embedding-3-small", "https://api.openai.com");
        costTrackingService = mock(CostTrackingService.class);
        RestClient.Builder builder = RestClient.builder();
        mockServer = MockRestServiceServer.bindTo(builder).build();
        embeddingService = new OpenAiEmbeddingService(builder, properties, costTrackingService);
    }

    @Test
    void shouldReturnEmbedding() throws JsonProcessingException {
        String text = "hello world";
        float[] expectedEmbedding = new float[] { 0.1f, 0.2f, 0.3f };

        OpenAiEmbeddingService.EmbeddingData data = new OpenAiEmbeddingService.EmbeddingData(expectedEmbedding, 0, "embedding");
        OpenAiEmbeddingService.Usage usage = new OpenAiEmbeddingService.Usage(10, 10);
        OpenAiEmbeddingService.EmbeddingResponse response = new OpenAiEmbeddingService.EmbeddingResponse(List.of(data), usage);
        String responseJson = objectMapper.writeValueAsString(response);

        mockServer
            .expect(requestTo("https://api.openai.com/v1/embeddings"))
            .andExpect(method(HttpMethod.POST))
            .andExpect(header("Authorization", "Bearer test-key"))
            .andExpect(jsonPath("$.model").value("text-embedding-3-small"))
            .andExpect(jsonPath("$.input").value(text))
            .andRespond(withSuccess(responseJson, MediaType.APPLICATION_JSON));

        float[] result = embeddingService.embed(text, 1L, "corr-1");

        assertThat(result).containsExactly(expectedEmbedding);
        verify(costTrackingService).logEmbeddingUsage("text-embedding-3-small", 10, 1L, "corr-1");
        mockServer.verify();
    }

    @Test
    void shouldThrowExceptionWhenResponseIsEmpty() {
        String text = "hello world";

        mockServer
            .expect(requestTo("https://api.openai.com/v1/embeddings"))
            .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> embeddingService.embed(text))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Empty response from OpenAI embedding API");

        mockServer.verify();
    }

    @Test
    void shouldReturnModelName() {
        assertThat(embeddingService.getModelName()).isEqualTo("text-embedding-3-small");
    }
}
