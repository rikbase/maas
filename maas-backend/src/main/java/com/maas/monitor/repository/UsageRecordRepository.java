package com.maas.monitor.repository;

import com.maas.monitor.entity.UsageRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface UsageRecordRepository extends JpaRepository<UsageRecord, UUID> {

    @Query("SELECT COUNT(u) FROM UsageRecord u WHERE u.apiKeyId = :keyId AND u.createdAt >= :since")
    long countByApiKeyIdSince(@Param("keyId") UUID keyId, @Param("since") Instant since);

    @Query("SELECT COALESCE(SUM(u.cost), 0) FROM UsageRecord u WHERE u.apiKeyId = :keyId AND u.createdAt >= :since")
    BigDecimal totalCostByApiKeyIdSince(@Param("keyId") UUID keyId, @Param("since") Instant since);

    @Query("SELECT COALESCE(SUM(u.promptTokens + u.completionTokens), 0) FROM UsageRecord u WHERE u.createdAt >= :since")
    long totalTokensSince(@Param("since") Instant since);

    @Query("SELECT COUNT(u) FROM UsageRecord u WHERE u.createdAt >= :since")
    long countAllSince(@Param("since") Instant since);

    @Query("SELECT COALESCE(SUM(u.cost), 0) FROM UsageRecord u WHERE u.createdAt >= :since")
    BigDecimal totalCostAllSince(@Param("since") Instant since);

    @Query("SELECT COALESCE(SUM(u.promptTokens), 0) FROM UsageRecord u WHERE u.createdAt >= :since")
    long totalPromptTokensSince(@Param("since") Instant since);

    @Query("SELECT COALESCE(SUM(u.completionTokens), 0) FROM UsageRecord u WHERE u.createdAt >= :since")
    long totalCompletionTokensSince(@Param("since") Instant since);

    @Query("SELECT COALESCE(AVG(u.latencyMs), 0) FROM UsageRecord u WHERE u.createdAt >= :since")
    double avgLatencySince(@Param("since") Instant since);

    @Query("SELECT COUNT(u) FROM UsageRecord u WHERE u.createdAt >= :since AND u.status = 'error'")
    long countErrorsSince(@Param("since") Instant since);

    @Query("SELECT COUNT(u) FROM UsageRecord u WHERE u.providerId = :providerId AND u.createdAt >= :since")
    long countByProviderSince(@Param("providerId") UUID providerId, @Param("since") Instant since);

    @Query("SELECT COALESCE(SUM(u.promptTokens + u.completionTokens), 0) FROM UsageRecord u WHERE u.providerId = :providerId AND u.createdAt >= :since")
    long totalTokensByProviderSince(@Param("providerId") UUID providerId, @Param("since") Instant since);

    // Trends — daily aggregation
    @Query(value = """
        SELECT DATE(u.created_at) AS date,
               COUNT(*) AS request_count,
               COALESCE(SUM(u.prompt_tokens + u.completion_tokens), 0) AS token_count,
               COALESCE(SUM(u.cost), 0) AS cost
        FROM usage_record u
        WHERE u.created_at >= :since
        GROUP BY DATE(u.created_at)
        ORDER BY DATE(u.created_at)
        """, nativeQuery = true)
    List<Object[]> findDailyTrends(@Param("since") Instant since);

    // By model ranking
    @Query(value = """
        SELECT u.model,
               COUNT(*) AS request_count,
               COALESCE(SUM(u.prompt_tokens + u.completion_tokens), 0) AS token_count,
               COALESCE(SUM(u.cost), 0) AS cost
        FROM usage_record u
        WHERE u.created_at >= :since AND u.model IS NOT NULL
        GROUP BY u.model
        ORDER BY request_count DESC
        LIMIT 20
        """, nativeQuery = true)
    List<Object[]> findUsageByModel(@Param("since") Instant since);

    // By provider aggregation
    @Query(value = """
        SELECT COALESCE(CAST(u.provider_id AS text), 'unknown') AS provider_id,
               COUNT(*) AS request_count,
               COALESCE(SUM(CASE WHEN u.status = 'error' THEN 1 ELSE 0 END), 0) AS error_count,
               COALESCE(AVG(u.latency_ms), 0) AS avg_latency,
               COALESCE(SUM(u.cost), 0) AS total_cost
        FROM usage_record u
        WHERE u.created_at >= :since
        GROUP BY u.provider_id
        ORDER BY request_count DESC
        """, nativeQuery = true)
    List<Object[]> findProviderHealth(@Param("since") Instant since);
}
