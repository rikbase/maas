package com.maas.provider.dto;

import com.maas.provider.entity.ProviderType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ProviderCreateRequest(
    @NotBlank String name,
    @NotNull ProviderType type,
    @NotBlank String configJson
) {}
