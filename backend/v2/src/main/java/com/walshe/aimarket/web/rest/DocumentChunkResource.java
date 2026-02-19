package com.walshe.aimarket.web.rest;

import com.walshe.aimarket.repository.DocumentChunkRepository;
import com.walshe.aimarket.service.DocumentChunkService;
import com.walshe.aimarket.service.dto.DocumentChunkDTO;
import com.walshe.aimarket.web.rest.errors.BadRequestAlertException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link com.walshe.aimarket.domain.DocumentChunk}.
 */
@RestController
@RequestMapping("/api/document-chunks")
public class DocumentChunkResource {

    private static final Logger LOG = LoggerFactory.getLogger(DocumentChunkResource.class);

    private static final String ENTITY_NAME = "documentChunk";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final DocumentChunkService documentChunkService;

    private final DocumentChunkRepository documentChunkRepository;

    public DocumentChunkResource(DocumentChunkService documentChunkService, DocumentChunkRepository documentChunkRepository) {
        this.documentChunkService = documentChunkService;
        this.documentChunkRepository = documentChunkRepository;
    }

    /**
     * {@code POST  /document-chunks} : Create a new documentChunk.
     *
     * @param documentChunkDTO the documentChunkDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new documentChunkDTO, or with status {@code 400 (Bad Request)} if the documentChunk has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<DocumentChunkDTO> createDocumentChunk(@Valid @RequestBody DocumentChunkDTO documentChunkDTO)
        throws URISyntaxException {
        LOG.debug("REST request to save DocumentChunk : {}", documentChunkDTO);
        if (documentChunkDTO.getId() != null) {
            throw new BadRequestAlertException("A new documentChunk cannot already have an ID", ENTITY_NAME, "idexists");
        }
        documentChunkDTO = documentChunkService.save(documentChunkDTO);
        return ResponseEntity.created(new URI("/api/document-chunks/" + documentChunkDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, documentChunkDTO.getId().toString()))
            .body(documentChunkDTO);
    }

    /**
     * {@code PUT  /document-chunks/:id} : Updates an existing documentChunk.
     *
     * @param id the id of the documentChunkDTO to save.
     * @param documentChunkDTO the documentChunkDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated documentChunkDTO,
     * or with status {@code 400 (Bad Request)} if the documentChunkDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the documentChunkDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<DocumentChunkDTO> updateDocumentChunk(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody DocumentChunkDTO documentChunkDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update DocumentChunk : {}, {}", id, documentChunkDTO);
        if (documentChunkDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, documentChunkDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!documentChunkRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        documentChunkDTO = documentChunkService.update(documentChunkDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, documentChunkDTO.getId().toString()))
            .body(documentChunkDTO);
    }

    /**
     * {@code PATCH  /document-chunks/:id} : Partial updates given fields of an existing documentChunk, field will ignore if it is null
     *
     * @param id the id of the documentChunkDTO to save.
     * @param documentChunkDTO the documentChunkDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated documentChunkDTO,
     * or with status {@code 400 (Bad Request)} if the documentChunkDTO is not valid,
     * or with status {@code 404 (Not Found)} if the documentChunkDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the documentChunkDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<DocumentChunkDTO> partialUpdateDocumentChunk(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody DocumentChunkDTO documentChunkDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update DocumentChunk partially : {}, {}", id, documentChunkDTO);
        if (documentChunkDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, documentChunkDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!documentChunkRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<DocumentChunkDTO> result = documentChunkService.partialUpdate(documentChunkDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, documentChunkDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /document-chunks} : get all the documentChunks.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of documentChunks in body.
     */
    @GetMapping("")
    public List<DocumentChunkDTO> getAllDocumentChunks() {
        LOG.debug("REST request to get all DocumentChunks");
        return documentChunkService.findAll();
    }

    /**
     * {@code GET  /document-chunks/:id} : get the "id" documentChunk.
     *
     * @param id the id of the documentChunkDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the documentChunkDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<DocumentChunkDTO> getDocumentChunk(@PathVariable("id") Long id) {
        LOG.debug("REST request to get DocumentChunk : {}", id);
        Optional<DocumentChunkDTO> documentChunkDTO = documentChunkService.findOne(id);
        return ResponseUtil.wrapOrNotFound(documentChunkDTO);
    }

    /**
     * {@code DELETE  /document-chunks/:id} : delete the "id" documentChunk.
     *
     * @param id the id of the documentChunkDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDocumentChunk(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete DocumentChunk : {}", id);
        documentChunkService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }
}
