package com.walshe.aimarket.web.rest;

import static com.walshe.aimarket.domain.DocumentChunkAsserts.*;
import static com.walshe.aimarket.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.walshe.aimarket.IntegrationTest;
import com.walshe.aimarket.domain.Document;
import com.walshe.aimarket.domain.DocumentChunk;
import com.walshe.aimarket.repository.DocumentChunkRepository;
import com.walshe.aimarket.service.dto.DocumentChunkDTO;
import com.walshe.aimarket.service.mapper.DocumentChunkMapper;
import jakarta.persistence.EntityManager;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link DocumentChunkResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class DocumentChunkResourceIT {

    private static final Integer DEFAULT_CHUNK_INDEX = 1;
    private static final Integer UPDATED_CHUNK_INDEX = 2;

    private static final String DEFAULT_CHUNK_TEXT = "AAAAAAAAAA";
    private static final String UPDATED_CHUNK_TEXT = "BBBBBBBBBB";

    private static final String DEFAULT_EMBEDDING_MODEL = "AAAAAAAAAA";
    private static final String UPDATED_EMBEDDING_MODEL = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/document-chunks";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private DocumentChunkRepository documentChunkRepository;

    @Autowired
    private DocumentChunkMapper documentChunkMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restDocumentChunkMockMvc;

    private DocumentChunk documentChunk;

    private DocumentChunk insertedDocumentChunk;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static DocumentChunk createEntity(EntityManager em) {
        DocumentChunk documentChunk = new DocumentChunk()
            .chunkIndex(DEFAULT_CHUNK_INDEX)
            .chunkText(DEFAULT_CHUNK_TEXT)
            .embeddingModel(DEFAULT_EMBEDDING_MODEL);
        // Add required entity
        Document document;
        if (TestUtil.findAll(em, Document.class).isEmpty()) {
            document = DocumentResourceIT.createEntity();
            em.persist(document);
            em.flush();
        } else {
            document = TestUtil.findAll(em, Document.class).get(0);
        }
        documentChunk.setDocument(document);
        return documentChunk;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static DocumentChunk createUpdatedEntity(EntityManager em) {
        DocumentChunk updatedDocumentChunk = new DocumentChunk()
            .chunkIndex(UPDATED_CHUNK_INDEX)
            .chunkText(UPDATED_CHUNK_TEXT)
            .embeddingModel(UPDATED_EMBEDDING_MODEL);
        // Add required entity
        Document document;
        if (TestUtil.findAll(em, Document.class).isEmpty()) {
            document = DocumentResourceIT.createUpdatedEntity();
            em.persist(document);
            em.flush();
        } else {
            document = TestUtil.findAll(em, Document.class).get(0);
        }
        updatedDocumentChunk.setDocument(document);
        return updatedDocumentChunk;
    }

    @BeforeEach
    void initTest() {
        documentChunk = createEntity(em);
    }

    @AfterEach
    void cleanup() {
        if (insertedDocumentChunk != null) {
            documentChunkRepository.delete(insertedDocumentChunk);
            insertedDocumentChunk = null;
        }
    }

    @Test
    @Transactional
    void createDocumentChunk() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the DocumentChunk
        DocumentChunkDTO documentChunkDTO = documentChunkMapper.toDto(documentChunk);
        var returnedDocumentChunkDTO = om.readValue(
            restDocumentChunkMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(documentChunkDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            DocumentChunkDTO.class
        );

        // Validate the DocumentChunk in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedDocumentChunk = documentChunkMapper.toEntity(returnedDocumentChunkDTO);
        assertDocumentChunkUpdatableFieldsEquals(returnedDocumentChunk, getPersistedDocumentChunk(returnedDocumentChunk));

        insertedDocumentChunk = returnedDocumentChunk;
    }

    @Test
    @Transactional
    void createDocumentChunkWithExistingId() throws Exception {
        // Create the DocumentChunk with an existing ID
        documentChunk.setId(1L);
        DocumentChunkDTO documentChunkDTO = documentChunkMapper.toDto(documentChunk);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restDocumentChunkMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(documentChunkDTO)))
            .andExpect(status().isBadRequest());

        // Validate the DocumentChunk in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkChunkIndexIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        documentChunk.setChunkIndex(null);

        // Create the DocumentChunk, which fails.
        DocumentChunkDTO documentChunkDTO = documentChunkMapper.toDto(documentChunk);

        restDocumentChunkMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(documentChunkDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllDocumentChunks() throws Exception {
        // Initialize the database
        insertedDocumentChunk = documentChunkRepository.saveAndFlush(documentChunk);

        // Get all the documentChunkList
        restDocumentChunkMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(documentChunk.getId().intValue())))
            .andExpect(jsonPath("$.[*].chunkIndex").value(hasItem(DEFAULT_CHUNK_INDEX)))
            .andExpect(jsonPath("$.[*].chunkText").value(hasItem(DEFAULT_CHUNK_TEXT)))
            .andExpect(jsonPath("$.[*].embeddingModel").value(hasItem(DEFAULT_EMBEDDING_MODEL)));
    }

    @Test
    @Transactional
    void getDocumentChunk() throws Exception {
        // Initialize the database
        insertedDocumentChunk = documentChunkRepository.saveAndFlush(documentChunk);

        // Get the documentChunk
        restDocumentChunkMockMvc
            .perform(get(ENTITY_API_URL_ID, documentChunk.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(documentChunk.getId().intValue()))
            .andExpect(jsonPath("$.chunkIndex").value(DEFAULT_CHUNK_INDEX))
            .andExpect(jsonPath("$.chunkText").value(DEFAULT_CHUNK_TEXT))
            .andExpect(jsonPath("$.embeddingModel").value(DEFAULT_EMBEDDING_MODEL));
    }

    @Test
    @Transactional
    void getNonExistingDocumentChunk() throws Exception {
        // Get the documentChunk
        restDocumentChunkMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingDocumentChunk() throws Exception {
        // Initialize the database
        insertedDocumentChunk = documentChunkRepository.saveAndFlush(documentChunk);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the documentChunk
        DocumentChunk updatedDocumentChunk = documentChunkRepository.findById(documentChunk.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedDocumentChunk are not directly saved in db
        em.detach(updatedDocumentChunk);
        updatedDocumentChunk.chunkIndex(UPDATED_CHUNK_INDEX).chunkText(UPDATED_CHUNK_TEXT).embeddingModel(UPDATED_EMBEDDING_MODEL);
        DocumentChunkDTO documentChunkDTO = documentChunkMapper.toDto(updatedDocumentChunk);

        restDocumentChunkMockMvc
            .perform(
                put(ENTITY_API_URL_ID, documentChunkDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(documentChunkDTO))
            )
            .andExpect(status().isOk());

        // Validate the DocumentChunk in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedDocumentChunkToMatchAllProperties(updatedDocumentChunk);
    }

    @Test
    @Transactional
    void putNonExistingDocumentChunk() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        documentChunk.setId(longCount.incrementAndGet());

        // Create the DocumentChunk
        DocumentChunkDTO documentChunkDTO = documentChunkMapper.toDto(documentChunk);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restDocumentChunkMockMvc
            .perform(
                put(ENTITY_API_URL_ID, documentChunkDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(documentChunkDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the DocumentChunk in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchDocumentChunk() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        documentChunk.setId(longCount.incrementAndGet());

        // Create the DocumentChunk
        DocumentChunkDTO documentChunkDTO = documentChunkMapper.toDto(documentChunk);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restDocumentChunkMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(documentChunkDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the DocumentChunk in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamDocumentChunk() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        documentChunk.setId(longCount.incrementAndGet());

        // Create the DocumentChunk
        DocumentChunkDTO documentChunkDTO = documentChunkMapper.toDto(documentChunk);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restDocumentChunkMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(documentChunkDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the DocumentChunk in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateDocumentChunkWithPatch() throws Exception {
        // Initialize the database
        insertedDocumentChunk = documentChunkRepository.saveAndFlush(documentChunk);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the documentChunk using partial update
        DocumentChunk partialUpdatedDocumentChunk = new DocumentChunk();
        partialUpdatedDocumentChunk.setId(documentChunk.getId());

        partialUpdatedDocumentChunk.chunkIndex(UPDATED_CHUNK_INDEX).embeddingModel(UPDATED_EMBEDDING_MODEL);

        restDocumentChunkMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedDocumentChunk.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedDocumentChunk))
            )
            .andExpect(status().isOk());

        // Validate the DocumentChunk in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertDocumentChunkUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedDocumentChunk, documentChunk),
            getPersistedDocumentChunk(documentChunk)
        );
    }

    @Test
    @Transactional
    void fullUpdateDocumentChunkWithPatch() throws Exception {
        // Initialize the database
        insertedDocumentChunk = documentChunkRepository.saveAndFlush(documentChunk);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the documentChunk using partial update
        DocumentChunk partialUpdatedDocumentChunk = new DocumentChunk();
        partialUpdatedDocumentChunk.setId(documentChunk.getId());

        partialUpdatedDocumentChunk.chunkIndex(UPDATED_CHUNK_INDEX).chunkText(UPDATED_CHUNK_TEXT).embeddingModel(UPDATED_EMBEDDING_MODEL);

        restDocumentChunkMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedDocumentChunk.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedDocumentChunk))
            )
            .andExpect(status().isOk());

        // Validate the DocumentChunk in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertDocumentChunkUpdatableFieldsEquals(partialUpdatedDocumentChunk, getPersistedDocumentChunk(partialUpdatedDocumentChunk));
    }

    @Test
    @Transactional
    void patchNonExistingDocumentChunk() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        documentChunk.setId(longCount.incrementAndGet());

        // Create the DocumentChunk
        DocumentChunkDTO documentChunkDTO = documentChunkMapper.toDto(documentChunk);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restDocumentChunkMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, documentChunkDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(documentChunkDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the DocumentChunk in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchDocumentChunk() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        documentChunk.setId(longCount.incrementAndGet());

        // Create the DocumentChunk
        DocumentChunkDTO documentChunkDTO = documentChunkMapper.toDto(documentChunk);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restDocumentChunkMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(documentChunkDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the DocumentChunk in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamDocumentChunk() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        documentChunk.setId(longCount.incrementAndGet());

        // Create the DocumentChunk
        DocumentChunkDTO documentChunkDTO = documentChunkMapper.toDto(documentChunk);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restDocumentChunkMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(documentChunkDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the DocumentChunk in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteDocumentChunk() throws Exception {
        // Initialize the database
        insertedDocumentChunk = documentChunkRepository.saveAndFlush(documentChunk);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the documentChunk
        restDocumentChunkMockMvc
            .perform(delete(ENTITY_API_URL_ID, documentChunk.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return documentChunkRepository.count();
    }

    protected void assertIncrementedRepositoryCount(long countBefore) {
        assertThat(countBefore + 1).isEqualTo(getRepositoryCount());
    }

    protected void assertDecrementedRepositoryCount(long countBefore) {
        assertThat(countBefore - 1).isEqualTo(getRepositoryCount());
    }

    protected void assertSameRepositoryCount(long countBefore) {
        assertThat(countBefore).isEqualTo(getRepositoryCount());
    }

    protected DocumentChunk getPersistedDocumentChunk(DocumentChunk documentChunk) {
        return documentChunkRepository.findById(documentChunk.getId()).orElseThrow();
    }

    protected void assertPersistedDocumentChunkToMatchAllProperties(DocumentChunk expectedDocumentChunk) {
        assertDocumentChunkAllPropertiesEquals(expectedDocumentChunk, getPersistedDocumentChunk(expectedDocumentChunk));
    }

    protected void assertPersistedDocumentChunkToMatchUpdatableProperties(DocumentChunk expectedDocumentChunk) {
        assertDocumentChunkAllUpdatablePropertiesEquals(expectedDocumentChunk, getPersistedDocumentChunk(expectedDocumentChunk));
    }
}
