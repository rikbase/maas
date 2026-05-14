package com.maas.provider.dto;

public record ModelUpdateRequest(
    String modelName,
    String capabilities,
    String status
) {}
