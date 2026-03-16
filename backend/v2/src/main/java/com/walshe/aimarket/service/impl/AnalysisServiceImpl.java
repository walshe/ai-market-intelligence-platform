package com.walshe.aimarket.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.walshe.aimarket.domain.DocumentChunk;
import com.walshe.aimarket.service.AnalysisService;
import com.walshe.aimarket.service.EmbeddingService;
import com.walshe.aimarket.service.ChatCompletionClient;
import com.walshe.aimarket.service.PromptBuilderService;
import com.walshe.aimarket.service.RetrievalService;
import com.walshe.aimarket.service.dto.AnalysisResponseDTO;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of {@link AnalysisService} for RAG flow orchestration.
 */
@Service
@Transactional(readOnly = true)
class AnalysisServiceImpl implements AnalysisService {

    private static final Logger LOG = LoggerFactory.getLogger(AnalysisServiceImpl.class);

    private final EmbeddingService embeddingService;
    private final RetrievalService retrievalService;
    private final PromptBuilderService promptBuilderService;
    private final ChatCompletionClient llmClient;
    private final ObjectMapper objectMapper;

    AnalysisServiceImpl(
        EmbeddingService embeddingService,
        RetrievalService retrievalService,
        PromptBuilderService promptBuilderService,
        ChatCompletionClient llmClient,
        ObjectMapper objectMapper
    ) {
        this.embeddingService = embeddingService;
        this.retrievalService = retrievalService;
        this.promptBuilderService = promptBuilderService;
        this.llmClient = llmClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public AnalysisResponseDTO analyze(String query, Integer topK) {
        return analyze(query, topK, null);
    }

    @Override
    public AnalysisResponseDTO analyze(String query, Integer topK, String correlationId) {
        LOG.info("analysis request started correlationId={}", correlationId);

        // 1) Embed query
        LOG.debug("query embedding started correlationId={}", correlationId);
        float[] queryEmbedding = embeddingService.embed(query, null, correlationId);

        // 2) Retrieve topK chunks
        List<DocumentChunk> similarChunks = retrievalService.retrieveSimilar(queryEmbedding, topK);

        // 3) Build prompt
        String prompt = promptBuilderService.buildPrompt(query, similarChunks);

        // 4) Call LLM
        LOG.debug("completion generation started correlationId={}", correlationId);
        ChatCompletionClient.ChatCompletionResult llmResult = llmClient.generate(prompt, correlationId);

        // 5) Parse JSON response (enforce required fields)
        ParsedModelResponse parsed = parseModelJson(llmResult.content());

        LOG.info("analysis request completed correlationId={}", correlationId);

        // 6) Map to response DTO including modelUsed + tokensUsed
        return new AnalysisResponseDTO(
            parsed.summary,
            parsed.riskFactors,
            parsed.confidenceScore,
            llmResult.modelUsed(),
            llmResult.totalTokens()
        );
    }

    private ParsedModelResponse parseModelJson(String content) {
        try {
            JsonNode root = objectMapper.readTree(content);
            if (root == null || !root.isObject()) {
                throw new IllegalArgumentException("Invalid model response: root is not an object");
            }
            // summary
            JsonNode summaryNode = root.get("summary");
            if (summaryNode == null || !summaryNode.isTextual()) {
                throw new IllegalArgumentException("Missing or invalid 'summary'");
            }
            String summary = summaryNode.asText("");

            // riskFactors
            JsonNode riskNode = root.get("riskFactors");
            List<String> riskFactors = new ArrayList<>();
            if (riskNode != null && riskNode.isArray()) {
                Iterator<JsonNode> it = riskNode.elements();
                while (it.hasNext()) {
                    JsonNode n = it.next();
                    if (n.isTextual()) {
                        riskFactors.add(n.asText());
                    }
                }
            } else {
                // enforce required field
                throw new IllegalArgumentException("Missing or invalid 'riskFactors'");
            }

            // confidenceScore
            JsonNode confNode = root.get("confidenceScore");
            if (confNode == null || !confNode.isNumber()) {
                throw new IllegalArgumentException("Missing or invalid 'confidenceScore'");
            }
            double confidenceScore = confNode.asDouble();

            return new ParsedModelResponse(summary, riskFactors, confidenceScore);
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse model JSON response", e);
        }
    }

    private record ParsedModelResponse(String summary, List<String> riskFactors, double confidenceScore) {}
}
