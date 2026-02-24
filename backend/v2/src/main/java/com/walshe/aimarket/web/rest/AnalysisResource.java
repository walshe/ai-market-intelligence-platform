package com.walshe.aimarket.web.rest;

import com.walshe.aimarket.service.AnalysisService;
import com.walshe.aimarket.service.dto.AnalysisRequestDTO;
import com.walshe.aimarket.service.dto.AnalysisResponseDTO;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for RAG analysis endpoint.
 */
@RestController
@RequestMapping("/api/v1/analysis")
class AnalysisResource {

    private static final Logger LOG = LoggerFactory.getLogger(AnalysisResource.class);

    private final AnalysisService analysisService;

    AnalysisResource(AnalysisService analysisService) {
        this.analysisService = analysisService;
    }

    @PostMapping
    ResponseEntity<AnalysisResponseDTO> analyze(@Valid @RequestBody AnalysisRequestDTO request) {
        LOG.debug("REST request to analyze query");
        AnalysisResponseDTO response = analysisService.analyze(request.query(), request.topK());
        return ResponseEntity.ok(response);
    }
}
