package com.walshe.aimarket.service.mapper;

import com.walshe.aimarket.domain.Document;
import com.walshe.aimarket.service.dto.DocumentDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Document} and its DTO {@link DocumentDTO}.
 */
@Mapper(componentModel = "spring")
public interface DocumentMapper extends EntityMapper<DocumentDTO, Document> {}
