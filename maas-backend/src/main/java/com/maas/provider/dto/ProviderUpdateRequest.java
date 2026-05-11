package com.maas.provider.dto;

public record ProviderUpdateRequest(
    String name,
    String configJson
) {}
