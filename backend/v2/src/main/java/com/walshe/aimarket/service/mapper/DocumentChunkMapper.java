package com.walshe.aimarket.service.mapper;

import com.walshe.aimarket.domain.Document;
import com.walshe.aimarket.domain.DocumentChunk;
import com.walshe.aimarket.service.dto.DocumentChunkDTO;
import com.walshe.aimarket.service.dto.DocumentDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link DocumentChunk} and its DTO {@link DocumentChunkDTO}.
 */
@Mapper(componentModel = "spring")
public interface DocumentChunkMapper extends EntityMapper<DocumentChunkDTO, DocumentChunk> {
    @Mapping(target = "document", source = "document", qualifiedByName = "documentId")
    DocumentChunkDTO toDto(DocumentChunk s);

    @Named("documentId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    DocumentDTO toDtoDocumentId(Document document);
}
