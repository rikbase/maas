package com.maas.dify.dto.oauth2;

public record TokenResponse(
        String accessToken,
        String tokenType,
        long expiresIn,
        String scope
) {}
