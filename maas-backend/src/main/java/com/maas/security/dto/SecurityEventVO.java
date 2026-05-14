package com.maas.security.dto;

import com.maas.security.entity.SecurityEvent;
import java.time.Instant;
import java.util.UUID;

public record SecurityEventVO(
    UUID id, UUID ruleId, UUID apiKeyId,
    String provider, String model, String direction,
    String detectorType, String severity, String actionTaken,
    String requestSummary, String matchedContent,
    Instant createdAt
) {
    public static SecurityEventVO from(SecurityEvent e) {
        return new SecurityEventVO(
            e.getId(), e.getRuleId(), e.getApiKeyId(),
            e.getProvider(), e.getModel(), e.getDirection(),
            e.getDetectorType(), e.getSeverity(), e.getActionTaken(),
            e.getRequestSummary(), e.getMatchedContent(),
            e.getCreatedAt()
        );
    }
}
