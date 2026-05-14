package com.maas.mcp.dto;

import com.maas.mcp.entity.McpTransport;
import jakarta.validation.constraints.NotBlank;

public record McpServerCreateRequest(
    @NotBlank String name,
    String description,
    McpTransport transport,
    String command,
    String args,
    String envJson,
    String url,
    String apiKey,
    String configJson
) {}
