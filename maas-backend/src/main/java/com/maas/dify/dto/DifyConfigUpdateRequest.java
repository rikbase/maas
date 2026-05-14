package com.maas.dify.dto;

import jakarta.validation.constraints.NotBlank;

public record DifyConfigUpdateRequest(
        @NotBlank String name,
        @NotBlank String baseUrl,
        @NotBlank String adminEmail,
        String apiKey,
        String adminPassword
) {}
