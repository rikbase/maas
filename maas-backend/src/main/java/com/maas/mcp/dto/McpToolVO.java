package com.maas.mcp.dto;

import com.maas.mcp.entity.McpTool;
import java.time.Instant;
import java.util.UUID;

public record McpToolVO(
    UUID id, UUID serverId, String name, String description,
    String inputSchema, boolean enabled, boolean autoRegister,
    Instant createdAt, Instant updatedAt
) {
    public static McpToolVO from(McpTool t) {
        return new McpToolVO(
            t.getId(), t.getServerId(), t.getName(), t.getDescription(),
            t.getInputSchema(), t.isEnabled(), t.isAutoRegister(),
            t.getCreatedAt(), t.getUpdatedAt()
        );
    }
}
