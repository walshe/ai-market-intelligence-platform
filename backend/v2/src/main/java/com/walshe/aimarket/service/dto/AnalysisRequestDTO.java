package com.walshe.aimarket.service.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for analysis endpoint.
 */
public record AnalysisRequestDTO(
    @NotBlank String query,
    Integer topK
) {}
