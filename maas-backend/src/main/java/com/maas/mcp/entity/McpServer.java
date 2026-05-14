package com.maas.mcp.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "mcp_server")
public class McpServer {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 256)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private McpTransport transport = McpTransport.stdio;

    @Column(length = 512)
    private String command;

    @Column(columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String args = "[]";

    @Column(name = "env_json", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String envJson = "{}";

    @Column(length = 512)
    private String url;

    @Column(name = "api_key", length = 256)
    private String apiKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private McpServerStatus status = McpServerStatus.online;

    @Column(name = "config_json", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String configJson = "{}";

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public McpServer() {}

    @PreUpdate
    public void preUpdate() { this.updatedAt = Instant.now(); }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public McpTransport getTransport() { return transport; }
    public void setTransport(McpTransport transport) { this.transport = transport; }
    public String getCommand() { return command; }
    public void setCommand(String command) { this.command = command; }
    public String getArgs() { return args; }
    public void setArgs(String args) { this.args = args; }
    public String getEnvJson() { return envJson; }
    public void setEnvJson(String envJson) { this.envJson = envJson; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }
    public McpServerStatus getStatus() { return status; }
    public void setStatus(McpServerStatus status) { this.status = status; }
    public String getConfigJson() { return configJson; }
    public void setConfigJson(String configJson) { this.configJson = configJson; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
