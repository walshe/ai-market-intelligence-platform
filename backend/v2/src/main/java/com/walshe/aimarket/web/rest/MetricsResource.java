package com.walshe.aimarket.web.rest;

import com.walshe.aimarket.domain.CostLog;
import com.walshe.aimarket.repository.CostLogRepository;
import com.walshe.aimarket.service.dto.CostMetricsDTO;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for retrieving metrics.
 */
@RestController
@RequestMapping("/api/v1/metrics")
class MetricsResource {

    private static final Logger LOG = LoggerFactory.getLogger(MetricsResource.class);

    private final CostLogRepository costLogRepository;

    MetricsResource(CostLogRepository costLogRepository) {
        this.costLogRepository = costLogRepository;
    }

    /**
     * GET /cost : get cost metrics.
     *
     * @param correlationId optional correlationId to filter by.
     * @return the ResponseEntity with status 200 (OK) and the cost metrics in body.
     */
    @GetMapping("/cost")
    ResponseEntity<?> getCostMetrics(@RequestParam(required = false) String correlationId) {
        if (correlationId != null) {
            LOG.debug("REST request to get cost records for correlationId: {}", correlationId);
            List<CostLog> logs = costLogRepository.findByCorrelationId(correlationId);
            return ResponseEntity.ok(logs);
        }

        LOG.debug("REST request to get cost metrics");

        BigDecimal totalUsd = costLogRepository.getTotalCost();
        if (totalUsd == null) {
            totalUsd = BigDecimal.ZERO;
        }

        List<Object[]> costByModelRaw = costLogRepository.getCostByModel();
        Map<String, BigDecimal> byModel = costByModelRaw.stream()
            .collect(Collectors.toMap(
                row -> (String) row[0],
                row -> (BigDecimal) row[1],
                BigDecimal::add
            ));

        List<Object[]> costByCallTypeRaw = costLogRepository.getCostByCallType();
        Map<CostLog.CallType, BigDecimal> byCallType = costByCallTypeRaw.stream()
            .collect(Collectors.toMap(
                row -> (CostLog.CallType) row[0],
                row -> (BigDecimal) row[1],
                BigDecimal::add
            ));

        return ResponseEntity.ok(new CostMetricsDTO(totalUsd, byModel, byCallType));
    }
}
