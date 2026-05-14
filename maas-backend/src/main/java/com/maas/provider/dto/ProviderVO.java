package com.maas.provider.dto;

import com.maas.provider.entity.Provider;
import com.maas.provider.entity.ProviderType;
import com.maas.provider.entity.ProviderStatus;
import java.time.Instant;
import java.util.UUID;

public record ProviderVO(
    UUID id, String name, ProviderType type,
    ProviderStatus status, String healthStatus,
    String configJson,
    Instant createdAt, Instant updatedAt
) {
    public static ProviderVO from(Provider p) {
        return new ProviderVO(
            p.getId(), p.getName(), p.getType(),
            p.getStatus(), p.getHealthStatus(),
            p.getConfigJson(),
            p.getCreatedAt(), p.getUpdatedAt()
        );
    }
}
