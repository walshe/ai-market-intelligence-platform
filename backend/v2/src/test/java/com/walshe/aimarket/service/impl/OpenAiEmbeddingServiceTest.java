package com.walshe.aimarket.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.walshe.aimarket.config.EmbeddingProperties;
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
    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        properties = new EmbeddingProperties("test-key", "text-embedding-3-small", "https://api.openai.com");
        RestClient.Builder builder = RestClient.builder();
        mockServer = MockRestServiceServer.bindTo(builder).build();
        embeddingService = new OpenAiEmbeddingService(builder, properties);
    }

    @Test
    void shouldReturnEmbedding() throws JsonProcessingException {
        String text = "hello world";
        List<Double> expectedEmbedding = List.of(0.1, 0.2, 0.3);

        OpenAiEmbeddingService.EmbeddingData data = new OpenAiEmbeddingService.EmbeddingData(expectedEmbedding, 0, "embedding");
        OpenAiEmbeddingService.EmbeddingResponse response = new OpenAiEmbeddingService.EmbeddingResponse(List.of(data), null);
        String responseJson = objectMapper.writeValueAsString(response);

        mockServer
            .expect(requestTo("https://api.openai.com/v1/embeddings"))
            .andExpect(method(HttpMethod.POST))
            .andExpect(header("Authorization", "Bearer test-key"))
            .andExpect(jsonPath("$.model").value("text-embedding-3-small"))
            .andExpect(jsonPath("$.input").value(text))
            .andRespond(withSuccess(responseJson, MediaType.APPLICATION_JSON));

        List<Double> result = embeddingService.embed(text);

        assertThat(result).isEqualTo(expectedEmbedding);
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
