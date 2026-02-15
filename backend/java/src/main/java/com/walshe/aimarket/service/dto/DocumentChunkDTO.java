package com.walshe.aimarket.service.dto;

import jakarta.persistence.Lob;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * A DTO for the {@link com.walshe.aimarket.domain.DocumentChunk} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class DocumentChunkDTO implements Serializable {

    private Long id;

    @NotNull
    private Integer chunkIndex;

    @Lob
    private String chunkText;

    private String embeddingModel;

    @NotNull
    private DocumentDTO document;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getChunkIndex() {
        return chunkIndex;
    }

    public void setChunkIndex(Integer chunkIndex) {
        this.chunkIndex = chunkIndex;
    }

    public String getChunkText() {
        return chunkText;
    }

    public void setChunkText(String chunkText) {
        this.chunkText = chunkText;
    }

    public String getEmbeddingModel() {
        return embeddingModel;
    }

    public void setEmbeddingModel(String embeddingModel) {
        this.embeddingModel = embeddingModel;
    }

    public DocumentDTO getDocument() {
        return document;
    }

    public void setDocument(DocumentDTO document) {
        this.document = document;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DocumentChunkDTO)) {
            return false;
        }

        DocumentChunkDTO documentChunkDTO = (DocumentChunkDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, documentChunkDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "DocumentChunkDTO{" +
            "id=" + getId() +
            ", chunkIndex=" + getChunkIndex() +
            ", chunkText='" + getChunkText() + "'" +
            ", embeddingModel='" + getEmbeddingModel() + "'" +
            ", document=" + getDocument() +
            "}";
    }
}
