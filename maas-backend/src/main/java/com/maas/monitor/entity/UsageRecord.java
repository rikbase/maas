package com.maas.monitor.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "usage_record")
public class UsageRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "api_key_id")
    private UUID apiKeyId;

    @Column(name = "provider_id")
    private UUID providerId;

    @Column(length = 256)
    private String model;

    @Column(name = "prompt_tokens")
    private int promptTokens;

    @Column(name = "completion_tokens")
    private int completionTokens;

    @Column(name = "latency_ms")
    private int latencyMs;

    @Column(precision = 12, scale = 6)
    private BigDecimal cost = BigDecimal.ZERO;

    @Column(length = 32)
    private String status = "success";

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = Instant.now();
    }

    public UsageRecord() {}

    public UsageRecord(UUID apiKeyId, UUID providerId, String model, int promptTokens, int completionTokens, int latencyMs, BigDecimal cost) {
        this.apiKeyId = apiKeyId;
        this.providerId = providerId;
        this.model = model;
        this.promptTokens = promptTokens;
        this.completionTokens = completionTokens;
        this.latencyMs = latencyMs;
        this.cost = cost;
    }

    // getters
    public UUID getId() { return id; }
    public UUID getApiKeyId() { return apiKeyId; }
    public UUID getProviderId() { return providerId; }
    public String getModel() { return model; }
    public int getPromptTokens() { return promptTokens; }
    public int getCompletionTokens() { return completionTokens; }
    public int getLatencyMs() { return latencyMs; }
    public BigDecimal getCost() { return cost; }
    public String getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
}
