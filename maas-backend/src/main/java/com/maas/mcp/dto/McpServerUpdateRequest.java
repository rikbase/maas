package com.maas.mcp.dto;

import com.maas.mcp.entity.McpTransport;
import com.maas.mcp.entity.McpServerStatus;

public record McpServerUpdateRequest(
    String name,
    String description,
    McpTransport transport,
    String command,
    String args,
    String envJson,
    String url,
    String apiKey,
    McpServerStatus status,
    String configJson
) {}
