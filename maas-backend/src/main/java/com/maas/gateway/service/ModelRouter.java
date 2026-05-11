package com.maas.gateway.service;

import com.maas.common.exception.BusinessException;
import com.maas.provider.entity.Provider;
import com.maas.provider.entity.ProviderStatus;
import com.maas.provider.repository.ProviderRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ModelRouter {
    private final ProviderRepository providerRepository;

    public ModelRouter(ProviderRepository providerRepository) {
        this.providerRepository = providerRepository;
    }

    public Provider route(String model, String preferredProvider) {
        List<Provider> providers = providerRepository.findByStatus(ProviderStatus.enabled);

        if (preferredProvider != null) {
            return providers.stream()
                .filter(p -> p.getName().equals(preferredProvider) && "healthy".equals(p.getHealthStatus()))
                .findFirst()
                .orElseThrow(() -> new BusinessException(503, "Preferred provider unavailable"));
        }

        return providers.stream()
            .filter(p -> "healthy".equals(p.getHealthStatus()))
            .findFirst()
            .orElseThrow(() -> new BusinessException(404, "No available provider for model: " + model));
    }
}
