package com.maas.registry.dto;

import com.maas.registry.entity.SkillStatus;

public record SkillUpdateRequest(
    String name,
    String description,
    String type,
    String configJson,
    SkillStatus status,
    String publishNote
) {}
