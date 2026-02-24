package com.walshe.aimarket.repository;

import com.walshe.aimarket.domain.DocumentChunk;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the DocumentChunk entity.
 */
@Repository
public interface DocumentChunkRepository extends JpaRepository<DocumentChunk, Long> {

    /**
     * Native pgvector cosine similarity search against document_chunk.embedding.
     * Orders by ascending cosine distance (closest first).
     *
     * @param queryVector pgvector text literal (e.g., "[0.1, 0.2, ...]")
     * @param pageable page request to control LIMIT (use PageRequest.of(0, topK))
     */
    @Query(value = "SELECT * FROM document_chunk dc ORDER BY dc.embedding <=> CAST(:query AS vector(1536)) ASC", nativeQuery = true)
    List<DocumentChunk> findSimilarByCosine(@Param("query") String queryVector, Pageable pageable);
}
