package com.maas.registry.dto;

import com.maas.registry.entity.ToolDefinition;
import com.maas.registry.entity.ToolSource;
import java.time.Instant;
import java.util.UUID;

public record ToolVO(
    UUID id, UUID skillId, String name, String description,
    ToolSource source, String sourceRef, String inputSchema,
    boolean enabled, Instant createdAt, Instant updatedAt
) {
    public static ToolVO from(ToolDefinition t) {
        return new ToolVO(
            t.getId(), t.getSkillId(), t.getName(), t.getDescription(),
            t.getSource(), t.getSourceRef(), t.getInputSchema(),
            t.isEnabled(), t.getCreatedAt(), t.getUpdatedAt()
        );
    }
}
