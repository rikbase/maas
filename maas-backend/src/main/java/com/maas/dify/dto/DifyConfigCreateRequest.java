package com.maas.dify.dto;

import jakarta.validation.constraints.NotBlank;

public record DifyConfigCreateRequest(
        @NotBlank String name,
        @NotBlank String baseUrl,
        @NotBlank String authCode,
        String adminEmail,
        String adminPassword
) {}
