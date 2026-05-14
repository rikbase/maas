package com.maas.monitor.dto;

import java.math.BigDecimal;

public record ProviderHealthVO(
    String providerName,
    long requestCount,
    long errorCount,
    double avgLatencyMs,
    BigDecimal totalCost
) {}
