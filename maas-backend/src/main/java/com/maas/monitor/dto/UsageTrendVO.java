package com.maas.monitor.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record UsageTrendVO(
    LocalDate date,
    long requestCount,
    long tokenCount,
    BigDecimal cost
) {}
