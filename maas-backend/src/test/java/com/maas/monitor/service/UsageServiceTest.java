package com.maas.monitor.service;

import com.maas.monitor.entity.UsageRecord;
import com.maas.monitor.repository.UsageRecordRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsageServiceTest {

    @Mock UsageRecordRepository usageRecordRepository;
    @InjectMocks UsageService usageService;

    @Test
    void getTotalTokensToday_shouldReturnSum() {
        when(usageRecordRepository.totalTokensSince(any(Instant.class))).thenReturn(500L);
        assertEquals(500L, usageService.getTotalTokensToday());
        verify(usageRecordRepository).totalTokensSince(any(Instant.class));
    }

    @Test
    void getRequestCountToday_shouldReturnCount() {
        UUID keyId = UUID.randomUUID();
        when(usageRecordRepository.countByApiKeyIdSince(any(), any())).thenReturn(10L);
        assertEquals(10L, usageService.getRequestCountToday(keyId));
        verify(usageRecordRepository).countByApiKeyIdSince(eq(keyId), any(Instant.class));
    }

    @Test
    void getTotalCostToday_shouldReturnCost() {
        UUID keyId = UUID.randomUUID();
        when(usageRecordRepository.totalCostByApiKeyIdSince(any(), any())).thenReturn(new BigDecimal("1.50"));
        assertEquals(new BigDecimal("1.50"), usageService.getTotalCostToday(keyId));
        verify(usageRecordRepository).totalCostByApiKeyIdSince(eq(keyId), any(Instant.class));
    }

    @Test
    void record_shouldSaveUsageRecord() {
        UUID keyId = UUID.randomUUID();
        UUID providerId = UUID.randomUUID();
        usageService.record(keyId, providerId, "gpt-4", 10, 20, 100, new BigDecimal("0.02"));
        verify(usageRecordRepository).save(any(UsageRecord.class));
    }
}
