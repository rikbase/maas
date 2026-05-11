package com.maas.apikey.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "api_key")
public class ApiKey {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "key_hash", nullable = false, unique = true, length = 256)
    private String keyHash;

    @Column(nullable = false, length = 128)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "key_type", nullable = false, length = 32)
    private KeyType keyType = KeyType.application;

    @Column(name = "policy_json", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String policyJson = "{}";

    @Column(name = "created_by", length = 128)
    private String createdBy;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Enumerated(EnumType.STRING)
    @Column(length = 32)
    private KeyStatus status = KeyStatus.active;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public ApiKey() {}

    public ApiKey(String keyHash, String name, KeyType keyType) {
        this.keyHash = keyHash;
        this.name = name;
        this.keyType = keyType;
    }

    @PreUpdate
    public void preUpdate() { this.updatedAt = Instant.now(); }

    // getters / setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getKeyHash() { return keyHash; }
    public void setKeyHash(String keyHash) { this.keyHash = keyHash; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public KeyType getKeyType() { return keyType; }
    public void setKeyType(KeyType keyType) { this.keyType = keyType; }
    public String getPolicyJson() { return policyJson; }
    public void setPolicyJson(String policyJson) { this.policyJson = policyJson; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }
    public KeyStatus getStatus() { return status; }
    public void setStatus(KeyStatus status) { this.status = status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
