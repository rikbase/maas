package com.maas.security.dto;

public record SecurityStatsVO(
    long totalEvents,
    long blockedCount,
    long flaggedCount,
    long promptInjectionCount,
    long secretLeakCount,
    long last24hEvents
) {}
