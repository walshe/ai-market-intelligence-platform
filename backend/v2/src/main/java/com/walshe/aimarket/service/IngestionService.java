package com.walshe.aimarket.service;

import com.walshe.aimarket.domain.Document;
import com.walshe.aimarket.domain.DocumentChunk;
import com.walshe.aimarket.repository.DocumentChunkRepository;
import com.walshe.aimarket.repository.DocumentRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

/**
 * Orchestrates document ingestion: chunking, embedding generation, and persistence.
 */
@Service
public class IngestionService {

    private static final Logger log = LoggerFactory.getLogger(IngestionService.class);

    private final DocumentRepository documentRepository;
    private final DocumentChunkRepository documentChunkRepository;
    private final ChunkingService chunkingService;
    private final EmbeddingService embeddingService;

    IngestionService(
        DocumentRepository documentRepository,
        DocumentChunkRepository documentChunkRepository,
        ChunkingService chunkingService,
        EmbeddingService embeddingService
    ) {
        this.documentRepository = documentRepository;
        this.documentChunkRepository = documentChunkRepository;
        this.chunkingService = chunkingService;
        this.embeddingService = embeddingService;
    }

    @Transactional
    public void ingestDocument(Long documentId) {
        Document document = documentRepository.findById(documentId)
            .orElseThrow(() -> new IllegalArgumentException("Document not found: " + documentId));
        ingestDocument(document);
    }

    @Transactional
    public void ingestDocument(Document document) {
        if (document.getId() == null) {
            // Persist document if not yet persisted (should normally be persisted beforehand)
            document = documentRepository.save(document);
        }
        String content = document.getContent();
        if (content == null || content.isBlank()) {
            log.debug("Document {} has empty content; skipping ingestion", document.getId());
            return;
        }

        List<String> chunks = chunkingService.chunk(content);
        String model = embeddingService.getModelName();

        int index = 0;
        for (String chunkText : chunks) {
            float[] embedding = embeddingService.embed(chunkText);
            // Validate embedding length (vector(1536))
            if (embedding == null || embedding.length != 1536) {
                throw new IllegalStateException("Embedding size mismatch: expected 1536 but was " + (embedding == null ? 0 : embedding.length));
            }

            DocumentChunk chunk = new DocumentChunk()
                .chunkIndex(index)
                .chunkText(chunkText)
                .embeddingModel(model)
                .createdAt(Instant.now())
                .document(document)
                .embedding(toVectorLiteral(embedding));

            documentChunkRepository.save(chunk);
            index++;
        }
    }

    private String toVectorLiteral(float[] embedding) {
        StringBuilder sb = new StringBuilder(10 * embedding.length);
        sb.append('[');
        for (int i = 0; i < embedding.length; i++) {
            if (i > 0) sb.append(", ");
            float f = embedding[i];
            sb.append(Float.toString(f));
        }
        sb.append(']');
        return sb.toString();
    }
}
