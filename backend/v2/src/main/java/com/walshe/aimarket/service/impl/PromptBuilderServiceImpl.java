package com.walshe.aimarket.service.impl;

import com.walshe.aimarket.domain.DocumentChunk;
import com.walshe.aimarket.service.PromptBuilderService;
import com.walshe.aimarket.service.PromptService;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

/**
 * Deterministic implementation of {@link PromptBuilderService}.
 */
@Service
class PromptBuilderServiceImpl implements PromptBuilderService {

    private final PromptService promptService;

    PromptBuilderServiceImpl(PromptService promptService) {
        this.promptService = promptService;
    }

    @Override
    public String buildPrompt(String query, List<DocumentChunk> contextChunks) {
        Objects.requireNonNull(query, "query must not be null");
        Objects.requireNonNull(contextChunks, "contextChunks must not be null");

        final String contextSection = buildContextSection(contextChunks);

        return promptService.renderPrompt("analysis.system.prompt", Map.of(
            "context", contextSection,
            "query", query
        ));
    }

    @Override
    public String buildStreamingPrompt(String query, List<DocumentChunk> contextChunks) {
        Objects.requireNonNull(query, "query must not be null");
        Objects.requireNonNull(contextChunks, "contextChunks must not be null");

        final String contextSection = buildContextSection(contextChunks);

        return promptService.renderPrompt("analysis.streaming.prompt", Map.of(
            "context", contextSection,
            "query", query
        ));
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
