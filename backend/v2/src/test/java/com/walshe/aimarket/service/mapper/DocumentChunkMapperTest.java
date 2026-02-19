package com.walshe.aimarket.service.mapper;

import static com.walshe.aimarket.domain.DocumentChunkAsserts.*;
import static com.walshe.aimarket.domain.DocumentChunkTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DocumentChunkMapperTest {

    private DocumentChunkMapper documentChunkMapper;

    @BeforeEach
    void setUp() {
        documentChunkMapper = new DocumentChunkMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getDocumentChunkSample1();
        var actual = documentChunkMapper.toEntity(documentChunkMapper.toDto(expected));
        assertDocumentChunkAllPropertiesEquals(expected, actual);
    }
}
