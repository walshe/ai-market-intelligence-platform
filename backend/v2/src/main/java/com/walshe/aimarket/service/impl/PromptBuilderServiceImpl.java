package com.walshe.aimarket.service.impl;

import com.walshe.aimarket.domain.DocumentChunk;
import com.walshe.aimarket.service.PromptBuilderService;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

/**
 * Deterministic implementation of {@link PromptBuilderService}.
 */
@Service
class PromptBuilderServiceImpl implements PromptBuilderService {

    // Fixed, deterministic system instruction per project guidelines
    private static final String SYSTEM_INSTRUCTION = "You are a financial analysis assistant. "
        + "Use only the provided context to answer. If the context is insufficient, say you don't know. "
        + "Be concise and objective.";

    // Explicit JSON contract instruction (Phase 5 fields referenced for consistency)
    private static final String JSON_CONTRACT_INSTRUCTION = "Return a strictly valid JSON object with the following fields: "
        + "summary (string), riskFactors (array of strings), confidenceScore (number between 0 and 1), "
        + "modelUsed (string), tokensUsed (integer). Do not include markdown fences or extra commentary.";

    @Override
    public String buildPrompt(String query, List<DocumentChunk> contextChunks) {
        Objects.requireNonNull(query, "query must not be null");
        Objects.requireNonNull(contextChunks, "contextChunks must not be null");

        final String contextSection = buildContextSection(contextChunks);

        StringBuilder sb = new StringBuilder(512);
        sb.append("[SYSTEM]\n").append(SYSTEM_INSTRUCTION).append("\n\n");
        sb.append("[CONTEXT]\n").append(contextSection).append("\n\n");
        sb.append("[USER QUERY]\n").append(query).append("\n\n");
        sb.append("[OUTPUT FORMAT]\n").append(JSON_CONTRACT_INSTRUCTION);
        return sb.toString();
    }

    private String buildContextSection(List<DocumentChunk> chunks) {
        if (chunks.isEmpty()) {
            return "(no context)"; // deterministic placeholder for empty context
        }
        // Preserve the order as given; number starting from 1 for readability
        return rangeMapToLines(chunks);
    }

    private String rangeMapToLines(List<DocumentChunk> chunks) {
        StringBuilder sb = new StringBuilder(Math.max(64, chunks.size() * 64));
        for (int i = 0; i < chunks.size(); i++) {
            DocumentChunk c = chunks.get(i);
            String text = c != null ? c.getChunkText() : "";
            // Normalize nulls to empty to keep determinism
            if (text == null) {
                text = "";
            }
            sb.append(i + 1).append('.').append(' ').append(text).append('\n');
        }
        // remove trailing newline for deterministic output
        if (sb.length() > 0) {
            sb.setLength(sb.length() - 1);
        }
        return sb.toString();
    }
}
