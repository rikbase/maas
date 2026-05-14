package com.maas.dify.dto;

import jakarta.validation.constraints.NotBlank;

public record DifyConfigUpdateRequest(
        @NotBlank String name,
        @NotBlank String baseUrl,
        String authCode,
        String adminEmail,
        String adminPassword
) {}
