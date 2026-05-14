package com.maas.provider.service;

import com.maas.provider.adapter.ProviderAdapter;
import com.maas.provider.entity.Provider;
import com.maas.provider.entity.ProviderStatus;
import com.maas.provider.repository.ProviderRepository;
import com.maas.provider.repository.ProviderModelRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProviderHealthChecker {
    private final ProviderRepository providerRepository;
    private final ProviderModelRepository providerModelRepository;
    private final List<ProviderAdapter> adapters;

    public ProviderHealthChecker(ProviderRepository providerRepository,
                                  ProviderModelRepository providerModelRepository,
                                  List<ProviderAdapter> adapters) {
        this.providerRepository = providerRepository;
        this.providerModelRepository = providerModelRepository;
        this.adapters = adapters;
    }

    @Scheduled(fixedRate = 60_000)
    public void checkAll() {
        List<Provider> providers = providerRepository.findByStatus(ProviderStatus.enabled);
        for (Provider provider : providers) {
            checkProvider(provider);
        }
    }

    public void checkProvider(Provider provider) {
        ProviderAdapter adapter = findAdapter(provider);
        if (adapter == null) return;

        boolean healthy = adapter.checkHealth(provider);
        provider.setHealthStatus(healthy ? "healthy" : "unhealthy");
        if (healthy) {
            List<String> models = adapter.fetchModels(provider);
            syncModels(provider, models);
        }
        providerRepository.save(provider);
    }

    private void syncModels(Provider provider, List<String> models) {
        providerModelRepository.deleteByProviderId(provider.getId());
        for (String modelName : models) {
            providerModelRepository.save(
                new com.maas.provider.entity.ProviderModel(provider, modelName));
        }
    }

    private ProviderAdapter findAdapter(Provider provider) {
        for (ProviderAdapter adapter : adapters) {
            if (adapter.supports(provider)) return adapter;
        }
        return null;
    }
}
