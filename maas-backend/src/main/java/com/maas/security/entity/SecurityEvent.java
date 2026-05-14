package com.maas.security.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "security_event")
public class SecurityEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "rule_id")
    private UUID ruleId;

    @Column(name = "api_key_id")
    private UUID apiKeyId;

    @Column(length = 128)
    private String provider;

    @Column(length = 256)
    private String model;

    @Column(nullable = false, length = 16)
    private String direction;

    @Column(name = "detector_type", nullable = false, length = 64)
    private String detectorType;

    @Column(nullable = false, length = 16)
    private String severity;

    @Column(name = "action_taken", nullable = false, length = 16)
    private String actionTaken;

    @Column(columnDefinition = "TEXT")
    private String requestSummary;

    @Column(name = "matched_content", columnDefinition = "TEXT")
    private String matchedContent;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    public void prePersist() { this.createdAt = Instant.now(); }

    public SecurityEvent() {}

    public SecurityEvent(UUID ruleId, UUID apiKeyId, String provider, String model,
                         String direction, String detectorType, String severity,
                         String actionTaken, String requestSummary, String matchedContent) {
        this.ruleId = ruleId;
        this.apiKeyId = apiKeyId;
        this.provider = provider;
        this.model = model;
        this.direction = direction;
        this.detectorType = detectorType;
        this.severity = severity;
        this.actionTaken = actionTaken;
        this.requestSummary = requestSummary;
        this.matchedContent = matchedContent;
    }

    public UUID getId() { return id; }
    public UUID getRuleId() { return ruleId; }
    public UUID getApiKeyId() { return apiKeyId; }
    public String getProvider() { return provider; }
    public String getModel() { return model; }
    public String getDirection() { return direction; }
    public String getDetectorType() { return detectorType; }
    public String getSeverity() { return severity; }
    public String getActionTaken() { return actionTaken; }
    public String getRequestSummary() { return requestSummary; }
    public String getMatchedContent() { return matchedContent; }
    public Instant getCreatedAt() { return createdAt; }
}
