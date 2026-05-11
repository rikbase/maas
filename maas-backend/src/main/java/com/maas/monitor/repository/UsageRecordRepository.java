package com.maas.monitor.repository;

import com.maas.monitor.entity.UsageRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.Instant;
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
}
