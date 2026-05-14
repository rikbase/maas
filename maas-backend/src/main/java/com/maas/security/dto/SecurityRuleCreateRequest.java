package com.maas.security.dto;

import com.maas.security.entity.DetectorType;
import com.maas.security.entity.RuleAction;
import com.maas.security.entity.RuleSeverity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SecurityRuleCreateRequest(
    @NotBlank String name,
    String description,
    @NotNull DetectorType detectorType,
    String configJson,
    String scopeJson,
    RuleSeverity severity,
    RuleAction action,
    Double threshold
) {}
