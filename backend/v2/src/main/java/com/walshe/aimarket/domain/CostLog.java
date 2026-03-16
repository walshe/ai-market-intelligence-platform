package com.walshe.aimarket.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * A CostLog entry to track LLM usage costs.
 */
@Entity
@Table(name = "cost_log")
public class CostLog implements Serializable {

    private static final long serialVersionUID = 1L;

    public enum CallType {
        EMBEDDING,
        COMPLETION,
    }

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "call_type", nullable = false)
    private CallType callType;

    @NotNull
    @Column(name = "model_name", nullable = false)
    private String modelName;

    @NotNull
    @Column(name = "input_tokens", nullable = false)
    private Integer inputTokens;

    @Column(name = "output_tokens")
    private Integer outputTokens;

    @NotNull
    @Column(name = "total_tokens", nullable = false)
    private Integer totalTokens;

    @NotNull
    @Column(name = "estimated_usd_cost", precision = 21, scale = 10, nullable = false)
    private BigDecimal estimatedUsdCost;

    @Column(name = "document_id")
    private Long documentId;

    @Column(name = "correlation_id")
    private String correlationId;

    @Column(name = "provider")
    private String provider;

    @Column(name = "latency_ms")
    private Long latencyMs;

    @Column(name = "created_at", nullable = false, updatable = false, insertable = false)
    private Instant createdAt;

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public CallType getCallType() {
        return callType;
    }

    public void setCallType(CallType callType) {
        this.callType = callType;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public Integer getInputTokens() {
        return inputTokens;
    }

    public void setInputTokens(Integer inputTokens) {
        this.inputTokens = inputTokens;
    }

    public Integer getOutputTokens() {
        return outputTokens;
    }

    public void setOutputTokens(Integer outputTokens) {
        this.outputTokens = outputTokens;
    }

    public Integer getTotalTokens() {
        return totalTokens;
    }

    public void setTotalTokens(Integer totalTokens) {
        this.totalTokens = totalTokens;
    }

    public BigDecimal getEstimatedUsdCost() {
        return estimatedUsdCost;
    }

    public void setEstimatedUsdCost(BigDecimal estimatedUsdCost) {
        this.estimatedUsdCost = estimatedUsdCost;
    }

    public Long getDocumentId() {
        return documentId;
    }

    public void setDocumentId(Long documentId) {
        this.documentId = documentId;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public Long getLatencyMs() {
        return latencyMs;
    }

    public void setLatencyMs(Long latencyMs) {
        this.latencyMs = latencyMs;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CostLog)) {
            return false;
        }
        return id != null && id.equals(((CostLog) o).id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "CostLog{" +
            "id=" + getId() +
            ", callType='" + getCallType() + "'" +
            ", modelName='" + getModelName() + "'" +
            ", inputTokens=" + getInputTokens() +
            ", outputTokens=" + getOutputTokens() +
            ", totalTokens=" + getTotalTokens() +
            ", estimatedUsdCost=" + getEstimatedUsdCost() +
            ", documentId=" + getDocumentId() +
            ", correlationId='" + getCorrelationId() + "'" +
            ", provider='" + getProvider() + "'" +
            ", latencyMs=" + getLatencyMs() +
            ", createdAt='" + getCreatedAt() + "'" +
            "}";
    }
}
