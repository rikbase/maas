package com.maas.monitor.service;

import com.maas.monitor.dto.ProviderHealthVO;
import com.maas.monitor.dto.UsageRankVO;
import com.maas.monitor.dto.UsageTrendVO;
import com.maas.monitor.entity.UsageRecord;
import com.maas.monitor.repository.UsageRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
public class UsageService {
    private final UsageRecordRepository usageRecordRepository;

    public UsageService(UsageRecordRepository usageRecordRepository) {
        this.usageRecordRepository = usageRecordRepository;
    }

    @Transactional
    public void record(UUID apiKeyId, UUID providerId, String model, int promptTokens, int completionTokens, int latencyMs, BigDecimal cost) {
        UsageRecord record = new UsageRecord(apiKeyId, providerId, model, promptTokens, completionTokens, latencyMs, cost);
        usageRecordRepository.save(record);
    }

    public long getRequestCountToday(UUID apiKeyId) {
        return usageRecordRepository.countByApiKeyIdSince(apiKeyId, Instant.now().truncatedTo(ChronoUnit.DAYS));
    }

    public BigDecimal getTotalCostToday(UUID apiKeyId) {
        return usageRecordRepository.totalCostByApiKeyIdSince(apiKeyId, Instant.now().truncatedTo(ChronoUnit.DAYS));
    }

    public long getTotalRequestCountToday() {
        return usageRecordRepository.countAllSince(Instant.now().truncatedTo(ChronoUnit.DAYS));
    }

    public BigDecimal getTotalCostToday() {
        return usageRecordRepository.totalCostAllSince(Instant.now().truncatedTo(ChronoUnit.DAYS));
    }

    public long getTotalTokensToday() {
        return usageRecordRepository.totalTokensSince(Instant.now().truncatedTo(ChronoUnit.DAYS));
    }

    // Extended stats for dashboard
    public long getTotalPromptTokensToday() {
        return usageRecordRepository.totalPromptTokensSince(Instant.now().truncatedTo(ChronoUnit.DAYS));
    }

    public long getTotalCompletionTokensToday() {
        return usageRecordRepository.totalCompletionTokensSince(Instant.now().truncatedTo(ChronoUnit.DAYS));
    }

    public double getAvgLatencyToday() {
        return usageRecordRepository.avgLatencySince(Instant.now().truncatedTo(ChronoUnit.DAYS));
    }

    public long getErrorCountToday() {
        return usageRecordRepository.countErrorsSince(Instant.now().truncatedTo(ChronoUnit.DAYS));
    }

    // Trends
    public List<UsageTrendVO> getTrends(String range) {
        Instant since = switch (range) {
            case "week" -> Instant.now().minus(7, ChronoUnit.DAYS);
            case "month" -> Instant.now().minus(30, ChronoUnit.DAYS);
            default -> Instant.now().minus(1, ChronoUnit.DAYS); // "today"
        };
        return usageRecordRepository.findDailyTrends(since).stream()
            .map(row -> new UsageTrendVO(
                ((java.sql.Date) row[0]).toLocalDate(),
                ((Number) row[1]).longValue(),
                ((Number) row[2]).longValue(),
                (BigDecimal) row[3]
            ))
            .toList();
    }

    // By model
    public List<UsageRankVO> getUsageByModel(String range) {
        Instant since = parseRange(range);
        return usageRecordRepository.findUsageByModel(since).stream()
            .map(row -> new UsageRankVO(
                null,
                (String) row[0],
                ((Number) row[1]).longValue(),
                ((Number) row[2]).longValue(),
                (BigDecimal) row[3]
            ))
            .toList();
    }

    // By provider
    public List<ProviderHealthVO> getProviderHealth(String range) {
        Instant since = parseRange(range);
        return usageRecordRepository.findProviderHealth(since).stream()
            .map(row -> new ProviderHealthVO(
                String.valueOf(row[0]),
                ((Number) row[1]).longValue(),
                ((Number) row[2]).longValue(),
                ((Number) row[3]).doubleValue(),
                (BigDecimal) row[4]
            ))
            .toList();
    }

    private Instant parseRange(String range) {
        if (range == null) range = "today";
        return switch (range) {
            case "week" -> Instant.now().minus(7, ChronoUnit.DAYS);
            case "month" -> Instant.now().minus(30, ChronoUnit.DAYS);
            default -> Instant.now().truncatedTo(ChronoUnit.DAYS);
        };
    }
}
