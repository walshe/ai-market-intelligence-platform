package com.walshe.aimarket.service.dto;

import java.io.Serializable;
import java.util.UUID;

/**
 * A DTO for the {@link com.walshe.aimarket.domain.CostLog} entity,
 * sent as a Kafka event.
 */
public record CostLogEvent(
    String callType,
    String modelName,
    Integer inputTokens,
    Integer outputTokens,
    Long documentId,
    UUID correlationId
) implements Serializable {
}
