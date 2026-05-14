package com.maas.dify.dto.oauth2;

import jakarta.validation.constraints.NotBlank;

public record TokenRequest(
        @NotBlank String grantType,
        @NotBlank String code,
        @NotBlank String clientId,
        @NotBlank String clientSecret,
        @NotBlank String redirectUri
) {}
