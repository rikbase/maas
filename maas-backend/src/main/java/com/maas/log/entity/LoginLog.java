package com.maas.log.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "maas_login_log")
public class LoginLog {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 64)
    private String username;

    @Column(length = 45)
    private String ip;

    @Column(name = "user_agent", length = 512)
    private String userAgent;

    @Column(nullable = false)
    private Boolean success = true;

    @Column(name = "fail_reason", length = 256)
    private String failReason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public LoginLog() {}

    public LoginLog(String username, String ip, String userAgent, boolean success, String failReason) {
        this.username = username;
        this.ip = ip;
        this.userAgent = userAgent;
        this.success = success;
        this.failReason = failReason;
    }

    public UUID getId() { return id; }
    public String getUsername() { return username; }
    public String getIp() { return ip; }
    public String getUserAgent() { return userAgent; }
    public Boolean getSuccess() { return success; }
    public String getFailReason() { return failReason; }
    public Instant getCreatedAt() { return createdAt; }
}
