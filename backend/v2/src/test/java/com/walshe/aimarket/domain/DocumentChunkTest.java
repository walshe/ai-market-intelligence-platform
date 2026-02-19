package com.walshe.aimarket.domain;

import static com.walshe.aimarket.domain.DocumentChunkTestSamples.*;
import static com.walshe.aimarket.domain.DocumentTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.walshe.aimarket.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class DocumentChunkTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(DocumentChunk.class);
        DocumentChunk documentChunk1 = getDocumentChunkSample1();
        DocumentChunk documentChunk2 = new DocumentChunk();
        assertThat(documentChunk1).isNotEqualTo(documentChunk2);

        documentChunk2.setId(documentChunk1.getId());
        assertThat(documentChunk1).isEqualTo(documentChunk2);

        documentChunk2 = getDocumentChunkSample2();
        assertThat(documentChunk1).isNotEqualTo(documentChunk2);
    }

    @Test
    void documentTest() {
        DocumentChunk documentChunk = getDocumentChunkRandomSampleGenerator();
        Document documentBack = getDocumentRandomSampleGenerator();

        documentChunk.setDocument(documentBack);
        assertThat(documentChunk.getDocument()).isEqualTo(documentBack);

        documentChunk.document(null);
        assertThat(documentChunk.getDocument()).isNull();
    }
}
