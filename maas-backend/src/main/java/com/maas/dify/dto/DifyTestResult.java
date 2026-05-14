package com.maas.dify.dto;

public record DifyTestResult(
        boolean connected,
        String message,
        String appName
) {}
