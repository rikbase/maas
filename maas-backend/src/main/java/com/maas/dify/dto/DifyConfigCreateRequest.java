package com.maas.dify.dto;

import jakarta.validation.constraints.NotBlank;

public record DifyConfigCreateRequest(
        @NotBlank String name,
        @NotBlank String baseUrl,
        @NotBlank String apiKey,
        @NotBlank String adminEmail,
        @NotBlank String adminPassword
) {}
