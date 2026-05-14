package com.maas.security.dto;

import com.maas.security.entity.DetectorType;
import com.maas.security.entity.RuleAction;
import com.maas.security.entity.RuleSeverity;

public record SecurityRuleUpdateRequest(
    String name,
    String description,
    DetectorType detectorType,
    String configJson,
    String scopeJson,
    RuleSeverity severity,
    RuleAction action,
    Double threshold,
    Boolean enabled
) {}
