package com.walshe.aimarket.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.walshe.aimarket.IntegrationTest;
import com.walshe.aimarket.domain.Document;
import com.walshe.aimarket.domain.DocumentChunk;
import com.walshe.aimarket.domain.User;
import com.walshe.aimarket.repository.DocumentChunkRepository;
import com.walshe.aimarket.repository.DocumentRepository;
import com.walshe.aimarket.repository.UserRepository;
import com.walshe.aimarket.service.EmbeddingService;
import com.walshe.aimarket.service.LlmClient;
import com.walshe.aimarket.service.dto.AnalysisRequestDTO;
import com.walshe.aimarket.web.rest.vm.LoginVM;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link AnalysisResource} REST controller.
 */
@AutoConfigureMockMvc
@IntegrationTest
@Import(AnalysisResourceIT.StubConfig.class)
@TestPropertySource(properties = {
    "application.embedding.openai.api-key=dummy",
    "application.embedding.openai.model-name=text-embedding-3-small",
    "application.embedding.openai.base-url=http://localhost",
    "application.llm.openai.api-key=dummy",
    "application.llm.openai.model-name=gpt-4o",
    "application.llm.openai.base-url=http://localhost"
})
class AnalysisResourceIT {

    @TestConfiguration
    static class StubConfig {
        @Bean
        @Primary
        EmbeddingService embeddingService() {
            return new EmbeddingService() {
                @Override
                public float[] embed(String text) {
                    float[] vec = new float[1536];
                    if ("How was Q4?".equals(text)) {
                        vec[0] = 1.0f;
                    }
                    return vec;
                }
                @Override
                public String getModelName() { return "text-embedding-3-small"; }
            };
        }

        @Bean
        @Primary
        LlmClient llmClient() {
            return prompt -> new LlmClient.LlmResult(
                "{\"summary\":\"Growth of 20% detected.\", \"riskFactors\":[\"Economic slowdown\"], \"confidenceScore\":0.95}",
                "gpt-4o",
                150
            );
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper om;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private DocumentChunkRepository documentChunkRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String jwtToken;

    @BeforeEach
    @Transactional
    void setup() throws Exception {
        // Create user
        User user = new User();
        user.setLogin("analysis-test-user");
        user.setEmail("analysis-test@example.com");
        user.setPassword(passwordEncoder.encode("password"));
        user.setActivated(true);
        userRepository.saveAndFlush(user);

        // Login to get JWT
        LoginVM login = new LoginVM();
        login.setUsername("analysis-test-user");
        login.setPassword("password");
        MvcResult loginResult = mockMvc
            .perform(post("/api/authenticate").contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(login)))
            .andExpect(status().isOk())
            .andReturn();

        String responseBody = loginResult.getResponse().getContentAsString();
        jwtToken = om.readTree(responseBody).get("id_token").asText();

        // Seed data
        Document doc = new Document().title("Q4 Report").content("Full content").createdAt(Instant.now());
        documentRepository.saveAndFlush(doc);

        DocumentChunk chunk = new DocumentChunk()
            .document(doc)
            .chunkIndex(0)
            .chunkText("The company grew by 20% in Q4.")
            .embeddingModel("text-embedding-3-small")
            .createdAt(Instant.now())
            .embedding(unitAlongX()); // Must have 1536 dimensions
        documentChunkRepository.saveAndFlush(chunk);
    }

    private String unitAlongX() {
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        sb.append("1.0");
        for (int i = 0; i < 1535; i++) sb.append(", 0.0");
        sb.append(']');
        return sb.toString();
    }

    @Test
    @Transactional
    void analyze_shouldReturnRagResponse() throws Exception {
        AnalysisRequestDTO request = new AnalysisRequestDTO("How was Q4?", 5);

        mockMvc
            .perform(
                post("/api/v1/analysis")
                    .header("Authorization", "Bearer " + jwtToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(request))
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.summary").value("Growth of 20% detected."))
            .andExpect(jsonPath("$.riskFactors[0]").value("Economic slowdown"))
            .andExpect(jsonPath("$.confidenceScore").value(0.95))
            .andExpect(jsonPath("$.modelUsed").value("gpt-4o"))
            .andExpect(jsonPath("$.tokensUsed").value(150));
    }

    @Test
    void analyze_shouldReturnUnauthorizedWithoutToken() throws Exception {
        AnalysisRequestDTO request = new AnalysisRequestDTO("Query", 5);
        mockMvc
            .perform(post("/api/v1/analysis").contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(request)))
            .andExpect(status().isUnauthorized());
    }
}
