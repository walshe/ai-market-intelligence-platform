package com.walshe.aimarket.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.walshe.aimarket.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class DocumentChunkDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(DocumentChunkDTO.class);
        DocumentChunkDTO documentChunkDTO1 = new DocumentChunkDTO();
        documentChunkDTO1.setId(1L);
        DocumentChunkDTO documentChunkDTO2 = new DocumentChunkDTO();
        assertThat(documentChunkDTO1).isNotEqualTo(documentChunkDTO2);
        documentChunkDTO2.setId(documentChunkDTO1.getId());
        assertThat(documentChunkDTO1).isEqualTo(documentChunkDTO2);
        documentChunkDTO2.setId(2L);
        assertThat(documentChunkDTO1).isNotEqualTo(documentChunkDTO2);
        documentChunkDTO1.setId(null);
        assertThat(documentChunkDTO1).isNotEqualTo(documentChunkDTO2);
    }
}
