package com.maas.dify.dto;

import com.maas.dify.entity.DifyConfig;

import java.time.Instant;
import java.util.UUID;

public record DifyConfigVO(
        UUID id,
        String name,
        String baseUrl,
        String status,
        String adminEmail,
        Instant lastTestAt,
        Instant createdAt,
        Instant updatedAt
) {
    public static DifyConfigVO from(DifyConfig c) {
        return new DifyConfigVO(
                c.getId(),
                c.getName(),
                c.getBaseUrl(),
                c.getStatus(),
                c.getAdminEmail(),
                c.getLastTestAt(),
                c.getCreatedAt(),
                c.getUpdatedAt()
        );
    }
}
