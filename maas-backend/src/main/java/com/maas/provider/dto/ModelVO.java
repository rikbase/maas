package com.maas.provider.dto;

import com.maas.provider.entity.ProviderModel;
import java.util.UUID;

public record ModelVO(
    UUID id,
    String modelName,
    UUID providerId,
    String providerName,
    String capabilities,
    String status
) {
    public static ModelVO from(ProviderModel m, String providerName) {
        return new ModelVO(
            m.getId(),
            m.getModelName(),
            m.getProvider().getId(),
            providerName,
            m.getCapabilities(),
            m.getStatus()
        );
    }
}
