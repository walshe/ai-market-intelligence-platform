package com.walshe.aimarket.service.dto;

import com.walshe.aimarket.domain.CostLog;
import java.math.BigDecimal;
import java.util.Map;

/**
 * DTO for cost metrics.
 */
public record CostMetricsDTO(
    BigDecimal totalUsd,
    Map<String, BigDecimal> byModel,
    Map<CostLog.CallType, BigDecimal> byCallType
) {
}
