package com.walshe.aimarket.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.walshe.aimarket.domain.DocumentChunk;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
class PromptBuilderServiceTest {

    private PromptBuilderService newService() {
        try {
            Class<?> impl = Class.forName("com.walshe.aimarket.service.impl.PromptBuilderServiceImpl");
            var ctor = impl.getDeclaredConstructor();
            ctor.setAccessible(true);
            return (PromptBuilderService) ctor.newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Failed to instantiate PromptBuilderServiceImpl reflectively", e);
        }
    }

    @Test
    void buildsDeterministicPrompt_givenSameInputs_returnsSameOutput() {
        String query = "What are the key risks for ACME in Q4?";
        List<DocumentChunk> chunksA = List.of(
            new DocumentChunk().chunkIndex(0).chunkText("ACME faces FX exposure in EMEA.").createdAt(Instant.now()),
            new DocumentChunk().chunkIndex(1).chunkText("Supply chain delays may impact margins.").createdAt(Instant.now())
        );
        List<DocumentChunk> chunksB = List.of(
            new DocumentChunk().chunkIndex(0).chunkText("ACME faces FX exposure in EMEA.").createdAt(Instant.now()),
            new DocumentChunk().chunkIndex(1).chunkText("Supply chain delays may impact margins.").createdAt(Instant.now())
        );

        PromptBuilderService svc = newService();
        String prompt1 = svc.buildPrompt(query, chunksA);
        String prompt2 = svc.buildPrompt(query, chunksB);

        assertThat(prompt1).isEqualTo(prompt2);
    }

    @Test
    void preservesContextOrdering_andNumbersChunks() {
        String query = "Summarize risks";
        List<DocumentChunk> chunks = List.of(
            new DocumentChunk().chunkIndex(10).chunkText("First text"),
            new DocumentChunk().chunkIndex(2).chunkText("Second text"),
            new DocumentChunk().chunkIndex(7).chunkText("Third text")
        );

        String prompt = newService().buildPrompt(query, chunks);

        // Expect numbered lines in provided order
        assertThat(prompt).containsSubsequence(
            "[CONTEXT]",
            "1. First text",
            "2. Second text",
            "3. Third text"
        );
    }

    @Test
    void containsUserQuery_andJsonInstruction() {
        String query = "Provide a brief summary";
        List<DocumentChunk> chunks = List.of();

        String prompt = newService().buildPrompt(query, chunks);

        assertThat(prompt).contains("[USER QUERY]");
        assertThat(prompt).contains(query);
        assertThat(prompt).contains("Return a strictly valid JSON object");
        assertThat(prompt).contains("summary");
        assertThat(prompt).contains("riskFactors");
        assertThat(prompt).contains("confidenceScore");
        assertThat(prompt).contains("modelUsed");
        assertThat(prompt).contains("tokensUsed");
    }
}
