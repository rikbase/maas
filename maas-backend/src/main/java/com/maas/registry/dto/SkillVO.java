package com.maas.registry.dto;

import com.maas.registry.entity.Skill;
import com.maas.registry.entity.SkillStatus;
import java.time.Instant;
import java.util.UUID;

public record SkillVO(
    UUID id, String name, String description, int version,
    SkillStatus status, String type, String configJson,
    String publishNote, UUID createdBy,
    Instant createdAt, Instant updatedAt
) {
    public static SkillVO from(Skill s) {
        return new SkillVO(
            s.getId(), s.getName(), s.getDescription(), s.getVersion(),
            s.getStatus(), s.getType(), s.getConfigJson(),
            s.getPublishNote(), s.getCreatedBy(),
            s.getCreatedAt(), s.getUpdatedAt()
        );
    }
}
