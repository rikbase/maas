package com.maas.dify.dto.oauth2;

import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record AuthorizeRequest(
        @NotBlank String clientId,
        @NotBlank String redirectUri,
        @NotBlank String state,
        UUID difyConfigId
) {}
