package com.maas.registry.dto;

import com.maas.registry.entity.ToolSource;

public record ToolUpdateRequest(
    String name,
    String description,
    ToolSource source,
    String sourceRef,
    String inputSchema,
    Boolean enabled
) {}
