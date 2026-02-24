package com.walshe.aimarket.service;

import com.walshe.aimarket.IntegrationTest;
import com.walshe.aimarket.domain.Document;
import com.walshe.aimarket.domain.DocumentChunk;
import com.walshe.aimarket.repository.DocumentChunkRepository;
import com.walshe.aimarket.repository.DocumentRepository;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@IntegrationTest
@DirtiesContext
@TestPropertySource(properties = {
    "application.retrieval.default-top-k=2"
})
class RetrievalServiceIT {

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private DocumentChunkRepository chunkRepository;

    @Autowired
    private RetrievalService retrievalService;

    private Document doc;

    @BeforeEach
    void setUp() {
        doc = new Document()
            .title("Vec Test Doc")
            .content("irrelevant")
            .createdAt(Instant.now());
        doc = documentRepository.saveAndFlush(doc);

        // Three chunks with distinct cosine distances to query [1,0,0,...]
        // c1 direction (1,0) -> closest
        // c3 direction (1,1) -> second
        // c2 direction (0,1) -> farthest
        saveChunk(0, unitAlongX());
        saveChunk(1, unitAlongY());
        saveChunk(2, unitXY());
    }

    @Test
    @Transactional
    void retrieveSimilar_ordersByCosineDistance_andRespectsDefaultTopK() {
        System.out.println("[DEBUG_LOG] Chunk count in DB: " + chunkRepository.count());
        chunkRepository.findAll().forEach(c -> System.out.println("[DEBUG_LOG] Chunk index: " + c.getChunkIndex() + ", embedding: " + c.getEmbedding()));

        float[] query = unitAlongXArray();
        List<DocumentChunk> results = retrievalService.retrieveSimilar(query, null);

        System.out.println("[DEBUG_LOG] Results size: " + results.size());
        results.forEach(r -> System.out.println("[DEBUG_LOG] Result index: " + r.getChunkIndex()));

        // default-top-k=2 should limit to 2 results
        assertThat(results).hasSize(2);
        // Expect chunkIndex 0 (unit X) then chunkIndex 2 (unit XY)
        assertThat(results.get(0).getChunkIndex()).isEqualTo(0);
        assertThat(results.get(1).getChunkIndex()).isEqualTo(2);
    }

    private void saveChunk(int idx, String vectorLiteral1536) {
        DocumentChunk c = new DocumentChunk()
            .document(doc)
            .chunkIndex(idx)
            .chunkText("chunk-" + idx)
            .embeddingModel("manual-test")
            .createdAt(Instant.now())
            .embedding(vectorLiteral1536);
        chunkRepository.saveAndFlush(c);
    }

    private String unitAlongX() {
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        sb.append("1.0, 0.0");
        for (int i = 0; i < 1534; i++) sb.append(", 0.0");
        sb.append(']');
        return sb.toString();
    }

    private String unitAlongY() {
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        sb.append("0.0, 1.0");
        for (int i = 0; i < 1534; i++) sb.append(", 0.0");
        sb.append(']');
        return sb.toString();
    }

    private String unitXY() {
        // normalized (1,1)/sqrt(2) has same direction as (1,1) for cosine
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        sb.append("0.707, 0.707");
        for (int i = 0; i < 1534; i++) sb.append(", 0.0");
        sb.append(']');
        return sb.toString();
    }

    private float[] unitAlongXArray() {
        float[] v = new float[1536];
        v[0] = 1.0f;
        // others are 0.0f
        return v;
    }
}
