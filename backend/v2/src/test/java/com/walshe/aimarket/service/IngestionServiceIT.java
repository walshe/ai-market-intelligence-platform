package com.walshe.aimarket.service;

import com.walshe.aimarket.domain.Document;
import com.walshe.aimarket.domain.DocumentChunk;
import com.walshe.aimarket.repository.DocumentChunkRepository;
import com.walshe.aimarket.repository.DocumentRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import com.walshe.aimarket.IntegrationTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@IntegrationTest
@com.walshe.aimarket.config.EmbeddedSQL
@DirtiesContext
@Import(IngestionServiceIT.StubConfig.class)
@org.springframework.test.context.TestPropertySource(properties = {
    "application.embedding.openai.api-key=dummy",
    "application.embedding.openai.model-name=text-embedding-3-small",
    "application.embedding.openai.base-url=http://localhost"
})
class IngestionServiceIT {

    @TestConfiguration
    static class StubConfig {
        @Bean
        @org.springframework.context.annotation.Primary
        EmbeddingService embeddingService() {
            return new EmbeddingService() {
                @Override
                public float[] embed(String text) {
                    float[] vec = new float[1536];
                    vec[0] = (float) Math.min(1_000d, text.length());
                    return vec;
                }
                @Override
                public String getModelName() { return "text-embedding-3-small"; }
            };
        }
    }

    @Autowired
    private IngestionService ingestionService;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private DocumentChunkRepository documentChunkRepository;

    @Test
    @Transactional
    void ingestDocument_persistsChunksWithEmbeddingsAndOrdering() {
        // Persist a document with multiple sentences to produce multiple chunks
        // Build content long enough to trigger multiple chunks with DEFAULT_MAX_CHUNK_LEN=800
        String longSentence = "A".repeat(300) + "."; // ~301 chars with punctuation
        String content = String.join(" ", longSentence, longSentence, longSentence, longSentence, longSentence, longSentence);
        Document toPersist = new Document()
            .title("Test Doc")
            .content(content)
            .createdAt(Instant.now());
        Document persisted = documentRepository.saveAndFlush(toPersist);
        final Long documentId = persisted.getId();

        // Act
        ingestionService.ingestDocument(persisted);

        // Assert
        List<DocumentChunk> chunks = documentChunkRepository.findAll().stream()
            .filter(c -> c.getDocument().getId().equals(documentId))
            .sorted(Comparator.comparingInt(DocumentChunk::getChunkIndex))
            .toList();

        assertThat(chunks.size()).isGreaterThan(1);
        int expectedIndex = 0;
        for (DocumentChunk c : chunks) {
            assertThat(c.getChunkIndex()).isEqualTo(expectedIndex++);
            assertThat(c.getEmbeddingModel()).isEqualTo("text-embedding-3-small");
            assertThat(c.getEmbedding()).isNotNull();
            assertThat(c.getEmbedding()).startsWith("[");
            assertThat(c.getChunkText()).isNotBlank();
        }
    }

    @Test
    @Transactional
    void ingestDocument_isIdempotent_byDeletingExistingChunks() {
        // Arrange: persist a document with content that will produce multiple chunks
        String longSentence = "A".repeat(300) + ".";
        String content = String.join(" ", longSentence, longSentence, longSentence, longSentence);
        Document doc = new Document()
            .title("Idempotency Doc")
            .content(content)
            .createdAt(Instant.now());
        doc = documentRepository.saveAndFlush(doc);
        final Long documentId = doc.getId();

        // Act 1: first ingestion
        ingestionService.ingestDocument(doc);
        List<DocumentChunk> first = documentChunkRepository.findAll().stream()
            .filter(c -> c.getDocument().getId().equals(documentId))
            .toList();
        int firstCount = first.size();
        assertThat(firstCount).isGreaterThan(0);

        // Act 2: second ingestion (should NOT duplicate, we delete before re-ingesting)
        ingestionService.ingestDocument(doc);
        List<DocumentChunk> second = documentChunkRepository.findAll().stream()
            .filter(c -> c.getDocument().getId().equals(documentId))
            .toList();

        // Assert: same count, indices re-created deterministically starting from 0
        assertThat(second.size()).isEqualTo(firstCount);
        assertThat(second.stream().map(DocumentChunk::getChunkIndex).distinct().count()).isEqualTo((long) firstCount);
        assertThat(second.stream().map(DocumentChunk::getEmbeddingModel).distinct().toList()).containsExactly("text-embedding-3-small");
    }
}
