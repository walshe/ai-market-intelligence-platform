package com.walshe.aimarket.web.rest;

import com.walshe.aimarket.service.AnalysisService;
import com.walshe.aimarket.service.dto.AnalysisRequestDTO;
import com.walshe.aimarket.service.dto.AnalysisResponseDTO;
import jakarta.validation.Valid;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

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
        String correlationId = UUID.randomUUID().toString();
        LOG.info("analysis request started correlationId={}", correlationId);
        LOG.debug("REST request to analyze query: {}, correlationId: {}", request.query(), correlationId);

        AnalysisResponseDTO response = analysisService.analyze(request.query(), request.topK(), correlationId);
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    Flux<String> streamAnalysis(@Valid @RequestBody AnalysisRequestDTO request) {
        String correlationId = UUID.randomUUID().toString();
        LOG.info("stream analysis request started correlationId={}", correlationId);
        return analysisService.streamAnalysis(request.query(), request.topK(), correlationId);
    }
}
