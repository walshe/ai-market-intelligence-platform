package com.walshe.aimarket.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.Instant;

/**
 * A DocumentChunk.
 */
@Entity
@Table(name = "document_chunk")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class DocumentChunk implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @NotNull
    @Column(name = "chunk_index", nullable = false)
    private Integer chunkIndex;

    @Lob
    @Column(name = "chunk_text", nullable = false, columnDefinition = "text")
    private String chunkText;

    @Column(name = "embedding_model")
    private String embeddingModel;

    @NotNull
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @ManyToOne(optional = false)
    @NotNull
    private Document document;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public DocumentChunk id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getChunkIndex() {
        return this.chunkIndex;
    }

    public DocumentChunk chunkIndex(Integer chunkIndex) {
        this.setChunkIndex(chunkIndex);
        return this;
    }

    public void setChunkIndex(Integer chunkIndex) {
        this.chunkIndex = chunkIndex;
    }

    public String getChunkText() {
        return this.chunkText;
    }

    public DocumentChunk chunkText(String chunkText) {
        this.setChunkText(chunkText);
        return this;
    }

    public void setChunkText(String chunkText) {
        this.chunkText = chunkText;
    }

    public String getEmbeddingModel() {
        return this.embeddingModel;
    }

    public DocumentChunk embeddingModel(String embeddingModel) {
        this.setEmbeddingModel(embeddingModel);
        return this;
    }

    public void setEmbeddingModel(String embeddingModel) {
        this.embeddingModel = embeddingModel;
    }

    public Instant getCreatedAt() {
        return this.createdAt;
    }

    public DocumentChunk createdAt(Instant createdAt) {
        this.setCreatedAt(createdAt);
        return this;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Document getDocument() {
        return this.document;
    }

    public void setDocument(Document document) {
        this.document = document;
    }

    public DocumentChunk document(Document document) {
        this.setDocument(document);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DocumentChunk)) {
            return false;
        }
        return getId() != null && getId().equals(((DocumentChunk) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "DocumentChunk{" +
            "id=" + getId() +
            ", chunkIndex=" + getChunkIndex() +
            ", chunkText='" + getChunkText() + "'" +
            ", embeddingModel='" + getEmbeddingModel() + "'" +
            ", createdAt='" + getCreatedAt() + "'" +
            "}";
    }
}
