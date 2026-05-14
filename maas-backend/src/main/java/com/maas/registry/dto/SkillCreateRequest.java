package com.maas.registry.dto;

import jakarta.validation.constraints.NotBlank;

public record SkillCreateRequest(
    @NotBlank String name,
    String description,
    String type,
    String configJson
) {}
