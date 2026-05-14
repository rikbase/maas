package com.maas.registry.dto;

import com.maas.registry.entity.ToolSource;
import jakarta.validation.constraints.NotBlank;

public record ToolCreateRequest(
    @NotBlank String name,
    String description,
    ToolSource source,
    String sourceRef,
    String inputSchema
) {}
