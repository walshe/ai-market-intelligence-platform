package com.walshe.aimarket.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.walshe.aimarket.ai.embedding.EmbeddingClient;
import com.walshe.aimarket.ai.llm.CompletionResponse;
import com.walshe.aimarket.ai.llm.LLMCompletionClient;
import com.walshe.aimarket.domain.DocumentChunk;
import com.walshe.aimarket.service.AnalysisService;
import com.walshe.aimarket.service.EmbeddingService;
import com.walshe.aimarket.service.PromptBuilderService;
import com.walshe.aimarket.service.RetrievalService;
import com.walshe.aimarket.service.dto.AnalysisResponseDTO;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;

/**
 * Implementation of {@link AnalysisService} for RAG flow orchestration.
 */
@Service
@Transactional(readOnly = true)
class AnalysisServiceImpl implements AnalysisService {

    private static final Logger LOG = LoggerFactory.getLogger(AnalysisServiceImpl.class);

    private final EmbeddingClient embeddingClient;
    private final EmbeddingService embeddingService;
    private final RetrievalService retrievalService;
    private final PromptBuilderService promptBuilderService;
    private final LLMCompletionClient llmClient;
    private final ObjectMapper objectMapper;

    AnalysisServiceImpl(
        EmbeddingClient embeddingClient,
        EmbeddingService embeddingService,
        RetrievalService retrievalService,
        PromptBuilderService promptBuilderService,
        LLMCompletionClient llmClient,
        ObjectMapper objectMapper
    ) {
        this.embeddingClient = embeddingClient;
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

        // 1 & 2) Step 1: Convert query to vector using the EMBEDDING model
        // and retrieve topK chunks from the vector database.
        // This is the first AI model call in the RAG flow.
        List<DocumentChunk> similarChunks = retrieveContext(query, topK, correlationId);

        // 3) Build prompt with retrieved context
        String prompt = promptBuilderService.buildPrompt(query, similarChunks);

        // 4) Step 2: Pass the context + query to the COMPLETION model (LLM)
        // to generate the final human-readable answer.
        // This is the second AI model call in the RAG flow, intended for reasoning.
        LOG.debug("completion generation started correlationId={}", correlationId);
        CompletionResponse llmResult = llmClient.complete(prompt, correlationId);

        // 5) Parse JSON response (enforce required fields)
        ParsedModelResponse parsed = parseModelJson(llmResult.generatedText());

        LOG.info("analysis request completed correlationId={}", correlationId);

        // 6) Map to response DTO including modelUsed + tokensUsed
        return new AnalysisResponseDTO(
            parsed.summary,
            parsed.riskFactors,
            parsed.confidenceScore,
            llmResult.modelName(),
            llmResult.inputTokens() + llmResult.outputTokens()
        );
    }

    @Override
    public Flux<ServerSentEvent<String>> streamAnalysis(String query, Integer topK, String correlationId) {
        LOG.info("stream analysis request started correlationId={}", correlationId);

        // 1 & 2) Step 1: Convert query to vector using the EMBEDDING model
        // and retrieve topK chunks from the vector database.
        List<DocumentChunk> similarChunks = retrieveContext(query, topK, correlationId);

        // 3) Build prompt with retrieved context
        String prompt = promptBuilderService.buildStreamingPrompt(query, similarChunks);

        // 4) Step 2: Stream tokens from the COMPLETION model (LLM)
        return llmClient.streamCompletion(prompt, correlationId)
            .map(token -> ServerSentEvent.<String>builder()
                .event("token")
                .data(token)
                .build())
            .concatWith(Flux.just(ServerSentEvent.<String>builder()
                .event("done")
                .data("")
                .build()))
            .onErrorResume(e -> {
                LOG.error("stream analysis error correlationId={}: {}", correlationId, e.getMessage());
                return Flux.just(ServerSentEvent.<String>builder()
                    .event("error")
                    .data(e.getMessage())
                    .build());
            })
            .doOnComplete(() -> LOG.info("stream analysis request completed correlationId={}", correlationId));
    }

    private List<DocumentChunk> retrieveContext(String query, Integer topK, String correlationId) {
        LOG.debug("query embedding started correlationId={}", correlationId);
        float[] queryEmbedding = embeddingClient.generateEmbedding(query);
        return retrievalService.retrieveSimilar(queryEmbedding, topK);
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
