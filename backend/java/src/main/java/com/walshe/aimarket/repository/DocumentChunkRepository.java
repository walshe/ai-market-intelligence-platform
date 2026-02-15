package com.walshe.aimarket.repository;

import com.walshe.aimarket.domain.DocumentChunk;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the DocumentChunk entity.
 */
@SuppressWarnings("unused")
@Repository
public interface DocumentChunkRepository extends JpaRepository<DocumentChunk, Long> {}
