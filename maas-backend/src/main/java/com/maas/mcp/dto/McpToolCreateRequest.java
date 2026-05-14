package com.maas.mcp.dto;

import jakarta.validation.constraints.NotBlank;

public record McpToolCreateRequest(
    @NotBlank String name,
    String description,
    String inputSchema,
    Boolean autoRegister
) {}
