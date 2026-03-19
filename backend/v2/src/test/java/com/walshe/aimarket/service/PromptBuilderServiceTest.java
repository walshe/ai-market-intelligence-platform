package com.walshe.aimarket.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.walshe.aimarket.config.PromptProperties;
import com.walshe.aimarket.domain.DocumentChunk;
import com.walshe.aimarket.service.dto.PromptDefinition;
import com.walshe.aimarket.service.impl.PromptServiceImpl;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PromptBuilderServiceTest {

    private PromptBuilderService promptBuilderService;

    @BeforeEach
    void setUp() {
        PromptProperties properties = new PromptProperties();
        Map<String, PromptDefinition> definitions = new HashMap<>();
        definitions.put("analysis.system.prompt", new PromptDefinition(
            "You are a financial analysis assistant. Use only the provided context to answer. If the context is insufficient, say you don't know. Be concise and objective.",
            "[SYSTEM]\n{systemPrompt}\n\n[CONTEXT]\n{context}\n\n[USER QUERY]\n{query}\n\n[OUTPUT FORMAT]\nReturn a strictly valid JSON object with the following fields: summary (string), riskFactors (array of strings), confidenceScore (number between 0 and 1), modelUsed (string), tokensUsed (integer). Do not include markdown fences or extra commentary."
        ));
        definitions.put("analysis.streaming.prompt", new PromptDefinition(
            "You are a financial analysis assistant. Use only the provided context to answer. If the context is insufficient, say you don't know. Be concise and objective.",
            "[SYSTEM]\n{systemPrompt}\n\n[CONTEXT]\n{context}\n\n[USER QUERY]\n{query}\n\n[INSTRUCTION]\nProvide a concise financial analysis answering the query using the context. Return plain text only. Do not return JSON."
        ));
        properties.setDefinitions(definitions);

        PromptService promptService = new PromptServiceImpl(properties);

        try {
            Class<?> impl = Class.forName("com.walshe.aimarket.service.impl.PromptBuilderServiceImpl");
            var ctor = impl.getDeclaredConstructor(PromptService.class);
            ctor.setAccessible(true);
            promptBuilderService = (PromptBuilderService) ctor.newInstance(promptService);
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

        String prompt1 = promptBuilderService.buildPrompt(query, chunksA);
        String prompt2 = promptBuilderService.buildPrompt(query, chunksB);

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

        String prompt = promptBuilderService.buildPrompt(query, chunks);

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

        String prompt = promptBuilderService.buildPrompt(query, chunks);

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
