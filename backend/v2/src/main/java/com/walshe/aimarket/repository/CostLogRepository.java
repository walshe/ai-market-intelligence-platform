package com.walshe.aimarket.repository;

import com.walshe.aimarket.domain.CostLog;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the CostLog entity.
 */
@Repository
public interface CostLogRepository extends JpaRepository<CostLog, Long> {
    List<CostLog> findByCorrelationId(String correlationId);

    @Query("SELECT SUM(c.estimatedUsdCost) FROM CostLog c")
    java.math.BigDecimal getTotalCost();

    @Query("SELECT c.modelName, SUM(c.estimatedUsdCost) FROM CostLog c GROUP BY c.modelName")
    List<Object[]> getCostByModel();

    @Query("SELECT c.callType, SUM(c.estimatedUsdCost) FROM CostLog c GROUP BY c.callType")
    List<Object[]> getCostByCallType();
}
