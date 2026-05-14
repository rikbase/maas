package com.maas.provider.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record ModelCreateRequest(
    @NotNull UUID providerId,
    @NotBlank String modelName,
    String capabilities,
    String status
) {}
