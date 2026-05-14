package com.maas.security.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "security_rule")
public class SecurityRule {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 256)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "detector_type", nullable = false, length = 64)
    private DetectorType detectorType;

    @Column(name = "config_json", columnDefinition = "jsonb", nullable = false)
    @JdbcTypeCode(SqlTypes.JSON)
    private String configJson = "{}";

    @Column(name = "scope_json", columnDefinition = "jsonb", nullable = false)
    @JdbcTypeCode(SqlTypes.JSON)
    private String scopeJson = "{}";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private RuleSeverity severity = RuleSeverity.medium;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private RuleAction action = RuleAction.block;

    @Column(nullable = false)
    private double threshold = 0.85;

    @Column(nullable = false)
    private boolean enabled = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public SecurityRule() {}

    public SecurityRule(String name, DetectorType detectorType, String configJson, RuleSeverity severity, RuleAction action) {
        this.name = name;
        this.detectorType = detectorType;
        this.configJson = configJson;
        this.severity = severity;
        this.action = action;
    }

    @PreUpdate
    public void preUpdate() { this.updatedAt = Instant.now(); }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public DetectorType getDetectorType() { return detectorType; }
    public void setDetectorType(DetectorType detectorType) { this.detectorType = detectorType; }
    public String getConfigJson() { return configJson; }
    public void setConfigJson(String configJson) { this.configJson = configJson; }
    public String getScopeJson() { return scopeJson; }
    public void setScopeJson(String scopeJson) { this.scopeJson = scopeJson; }
    public RuleSeverity getSeverity() { return severity; }
    public void setSeverity(RuleSeverity severity) { this.severity = severity; }
    public RuleAction getAction() { return action; }
    public void setAction(RuleAction action) { this.action = action; }
    public double getThreshold() { return threshold; }
    public void setThreshold(double threshold) { this.threshold = threshold; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
