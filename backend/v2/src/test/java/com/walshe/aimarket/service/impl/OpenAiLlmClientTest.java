package com.walshe.aimarket.service.impl;

import com.walshe.aimarket.config.LlmProperties;
import com.walshe.aimarket.service.LlmClient;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import org.springframework.http.HttpMethod;

class OpenAiLlmClientTest {

    @Test
    void happyPath_parsesContentAndUsage() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();

        LlmProperties props = new LlmProperties("test-key", "gpt-4o-mini", "https://api.openai.com");
        OpenAiLlmClient client = new OpenAiLlmClient(builder, props);

        String responseJson = """
            {"id":"chatcmpl-123","object":"chat.completion","created":1694268190,
             "model":"gpt-4o-mini",
             "choices":[{"index":0,"message":{"role":"assistant","content":"Hello world"},"finish_reason":"stop"}],
             "usage":{"prompt_tokens":12,"completion_tokens":7,"total_tokens":19}}
            """;

        server.expect(ExpectedCount.once(), requestTo("https://api.openai.com/v1/chat/completions"))
            .andExpect(method(HttpMethod.POST))
            .andRespond(withSuccess(responseJson, MediaType.APPLICATION_JSON));

        LlmClient.LlmResult result = client.generate("prompt");

        assertThat(result.content()).isEqualTo("Hello world");
        assertThat(result.modelUsed()).isEqualTo("gpt-4o-mini");
        assertThat(result.tokensUsed()).isEqualTo(19);

        server.verify();
    }

    @Test
    void emptyResponse_throwsControlledError() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();

        LlmProperties props = new LlmProperties("test-key", "gpt-4o-mini", "https://api.openai.com");
        OpenAiLlmClient client = new OpenAiLlmClient(builder, props);

        String responseJson = "{}";

        server.expect(ExpectedCount.once(), requestTo("https://api.openai.com/v1/chat/completions"))
            .andExpect(method(HttpMethod.POST))
            .andRespond(withSuccess(responseJson, MediaType.APPLICATION_JSON));

        assertThrows(RuntimeException.class, () -> client.generate("prompt"));

        server.verify();
    }
}
