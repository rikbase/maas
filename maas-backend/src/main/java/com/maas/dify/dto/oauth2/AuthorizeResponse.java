package com.maas.dify.dto.oauth2;

public record AuthorizeResponse(
        String code,
        String state,
        String redirectUri
) {}
