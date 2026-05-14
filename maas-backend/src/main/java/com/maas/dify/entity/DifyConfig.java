package com.maas.dify.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "dify_config")
public class DifyConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(name = "base_url", nullable = false, length = 512)
    private String baseUrl;

    @Column(name = "api_key_encrypted", nullable = false, length = 1024)
    private String apiKeyEncrypted;

    @Column(name = "admin_email")
    private String adminEmail;

    @Column(name = "admin_password_encrypted", length = 1024)
    private String adminPasswordEncrypted;

    @Column(nullable = false, length = 16)
    private String status = "disconnected";

    @Column(name = "last_test_at")
    private Instant lastTestAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public DifyConfig() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }

    public String getApiKeyEncrypted() { return apiKeyEncrypted; }
    public void setApiKeyEncrypted(String apiKeyEncrypted) { this.apiKeyEncrypted = apiKeyEncrypted; }

    public String getAdminEmail() { return adminEmail; }
    public void setAdminEmail(String adminEmail) { this.adminEmail = adminEmail; }

    public String getAdminPasswordEncrypted() { return adminPasswordEncrypted; }
    public void setAdminPasswordEncrypted(String adminPasswordEncrypted) { this.adminPasswordEncrypted = adminPasswordEncrypted; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Instant getLastTestAt() { return lastTestAt; }
    public void setLastTestAt(Instant lastTestAt) { this.lastTestAt = lastTestAt; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
