package com.maas.log.dto;

public record LoginStatsVO(
    int todaySuccess,
    int todayFailed,
    int last24hAttempts
) {}
