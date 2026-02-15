package com.walshe.aimarket.service;

import com.walshe.aimarket.domain.DocumentChunk;
import com.walshe.aimarket.repository.DocumentChunkRepository;
import com.walshe.aimarket.service.dto.DocumentChunkDTO;
import com.walshe.aimarket.service.mapper.DocumentChunkMapper;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.walshe.aimarket.domain.DocumentChunk}.
 */
@Service
@Transactional
public class DocumentChunkService {

    private static final Logger LOG = LoggerFactory.getLogger(DocumentChunkService.class);

    private final DocumentChunkRepository documentChunkRepository;

    private final DocumentChunkMapper documentChunkMapper;

    public DocumentChunkService(DocumentChunkRepository documentChunkRepository, DocumentChunkMapper documentChunkMapper) {
        this.documentChunkRepository = documentChunkRepository;
        this.documentChunkMapper = documentChunkMapper;
    }

    /**
     * Save a documentChunk.
     *
     * @param documentChunkDTO the entity to save.
     * @return the persisted entity.
     */
    public DocumentChunkDTO save(DocumentChunkDTO documentChunkDTO) {
        LOG.debug("Request to save DocumentChunk : {}", documentChunkDTO);
        DocumentChunk documentChunk = documentChunkMapper.toEntity(documentChunkDTO);
        documentChunk = documentChunkRepository.save(documentChunk);
        return documentChunkMapper.toDto(documentChunk);
    }

    /**
     * Update a documentChunk.
     *
     * @param documentChunkDTO the entity to save.
     * @return the persisted entity.
     */
    public DocumentChunkDTO update(DocumentChunkDTO documentChunkDTO) {
        LOG.debug("Request to update DocumentChunk : {}", documentChunkDTO);
        DocumentChunk documentChunk = documentChunkMapper.toEntity(documentChunkDTO);
        documentChunk = documentChunkRepository.save(documentChunk);
        return documentChunkMapper.toDto(documentChunk);
    }

    /**
     * Partially update a documentChunk.
     *
     * @param documentChunkDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<DocumentChunkDTO> partialUpdate(DocumentChunkDTO documentChunkDTO) {
        LOG.debug("Request to partially update DocumentChunk : {}", documentChunkDTO);

        return documentChunkRepository
            .findById(documentChunkDTO.getId())
            .map(existingDocumentChunk -> {
                documentChunkMapper.partialUpdate(existingDocumentChunk, documentChunkDTO);

                return existingDocumentChunk;
            })
            .map(documentChunkRepository::save)
            .map(documentChunkMapper::toDto);
    }

    /**
     * Get all the documentChunks.
     *
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public List<DocumentChunkDTO> findAll() {
        LOG.debug("Request to get all DocumentChunks");
        return documentChunkRepository.findAll().stream().map(documentChunkMapper::toDto).collect(Collectors.toCollection(LinkedList::new));
    }

    /**
     * Get one documentChunk by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<DocumentChunkDTO> findOne(Long id) {
        LOG.debug("Request to get DocumentChunk : {}", id);
        return documentChunkRepository.findById(id).map(documentChunkMapper::toDto);
    }

    /**
     * Delete the documentChunk by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        LOG.debug("Request to delete DocumentChunk : {}", id);
        documentChunkRepository.deleteById(id);
    }
}
