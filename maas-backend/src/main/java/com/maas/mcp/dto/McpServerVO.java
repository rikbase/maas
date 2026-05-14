package com.maas.mcp.dto;

import com.maas.mcp.entity.McpServer;
import com.maas.mcp.entity.McpServerStatus;
import com.maas.mcp.entity.McpTransport;

import java.time.Instant;
import java.util.UUID;

public record McpServerVO(
    UUID id, String name, String description,
    McpTransport transport, String command, String args, String envJson,
    String url, String apiKey, McpServerStatus status, String configJson,
    Instant createdAt, Instant updatedAt,
    long toolCount
) {
    public static McpServerVO from(McpServer s, long toolCount) {
        return new McpServerVO(
            s.getId(), s.getName(), s.getDescription(),
            s.getTransport(), s.getCommand(), s.getArgs(), s.getEnvJson(),
            s.getUrl(), s.getApiKey(), s.getStatus(), s.getConfigJson(),
            s.getCreatedAt(), s.getUpdatedAt(), toolCount
        );
    }
}
