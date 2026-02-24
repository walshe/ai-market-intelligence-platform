package com.walshe.aimarket.service.impl;

import com.walshe.aimarket.config.RetrievalProperties;
import com.walshe.aimarket.domain.DocumentChunk;
import com.walshe.aimarket.repository.DocumentChunkRepository;
import com.walshe.aimarket.service.RetrievalService;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.springframework.stereotype.Service;

@Service
class RetrievalServiceImpl implements RetrievalService {

    private final DocumentChunkRepository repository;
    private final RetrievalProperties properties;

    RetrievalServiceImpl(DocumentChunkRepository repository, RetrievalProperties properties) {
        this.repository = repository;
        this.properties = properties;
    }

    @Override
    public List<DocumentChunk> retrieveSimilar(float[] queryEmbedding, Integer topK) {
        int effectiveTopK = (topK == null || topK <= 0) ? properties.getDefaultTopK() : topK;
        String vectorLiteral = toVectorLiteral(queryEmbedding);
        List<DocumentChunk> results = repository.findSimilarByCosine(vectorLiteral, effectiveTopK);
        System.out.println("[DEBUG_LOG] RetrievalServiceImpl effectiveTopK: " + effectiveTopK);
        System.out.println("[DEBUG_LOG] RetrievalServiceImpl query (first 20 chars): " + vectorLiteral.substring(0, Math.min(20, vectorLiteral.length())));
        System.out.println("[DEBUG_LOG] RetrievalServiceImpl results size: " + results.size());
        return results;
    }

    private String toVectorLiteral(float[] embedding) {
        if (embedding == null || embedding.length == 0) {
            return "[]";
        }
        // Ensure dot decimal separator regardless of locale
        return IntStream.range(0, embedding.length)
            .mapToObj(i -> String.format(Locale.ROOT, "%s", embedding[i]))
            .collect(Collectors.joining(", ", "[", "]"));
    }
}
