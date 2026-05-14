package com.maas.workflow.dto;

import java.time.LocalDate;

public record ExecutionTrendVO(
    LocalDate date,
    long totalCount,
    long completedCount,
    long failedCount,
    long runningCount,
    long pendingCount,
    long cancelledCount
) {}
