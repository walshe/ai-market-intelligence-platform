package com.walshe.aimarket.service.dto;

import java.util.List;

/**
 * Response DTO for analysis endpoint.
 */
public record AnalysisResponseDTO(
    String summary,
    List<String> riskFactors,
    double confidenceScore,
    String modelUsed,
    int tokensUsed
) {}
