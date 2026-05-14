package com.maas.security.dto;

import com.maas.security.entity.DetectorType;
import com.maas.security.entity.RuleAction;
import com.maas.security.entity.RuleSeverity;
import com.maas.security.entity.SecurityRule;
import java.time.Instant;
import java.util.UUID;

public record SecurityRuleVO(
    UUID id, String name, String description,
    DetectorType detectorType, String configJson, String scopeJson,
    RuleSeverity severity, RuleAction action, double threshold,
    boolean enabled, Instant createdAt, Instant updatedAt
) {
    public static SecurityRuleVO from(SecurityRule r) {
        return new SecurityRuleVO(
            r.getId(), r.getName(), r.getDescription(),
            r.getDetectorType(), r.getConfigJson(), r.getScopeJson(),
            r.getSeverity(), r.getAction(), r.getThreshold(),
            r.isEnabled(), r.getCreatedAt(), r.getUpdatedAt()
        );
    }
}
