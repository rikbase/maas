package com.maas.monitor.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record UsageRankVO(
    UUID id,
    String name,
    long requestCount,
    long tokenCount,
    BigDecimal cost
) {}
