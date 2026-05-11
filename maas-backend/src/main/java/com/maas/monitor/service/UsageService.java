package com.maas.monitor.service;

import com.maas.monitor.entity.UsageRecord;
import com.maas.monitor.repository.UsageRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
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

    public long getTotalTokensToday() {
        return usageRecordRepository.totalTokensSince(Instant.now().truncatedTo(ChronoUnit.DAYS));
    }
}
