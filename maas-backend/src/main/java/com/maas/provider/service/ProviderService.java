package com.maas.provider.service;

import com.maas.common.exception.BusinessException;
import com.maas.provider.dto.ProviderCreateRequest;
import com.maas.provider.dto.ProviderUpdateRequest;
import com.maas.provider.dto.ProviderVO;
import com.maas.provider.entity.Provider;
import com.maas.provider.entity.ProviderStatus;
import com.maas.provider.repository.ProviderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ProviderService {
    private final ProviderRepository providerRepository;

    public ProviderService(ProviderRepository providerRepository) {
        this.providerRepository = providerRepository;
    }

    public List<ProviderVO> listAll() {
        return providerRepository.findAll().stream()
            .map(ProviderVO::from)
            .toList();
    }

    public ProviderVO getById(UUID id) {
        return providerRepository.findById(id)
            .map(ProviderVO::from)
            .orElseThrow(() -> new BusinessException(404, "Provider not found"));
    }

    @Transactional
    public ProviderVO create(ProviderCreateRequest request) {
        if (providerRepository.existsByName(request.name())) {
            throw new BusinessException(409, "Provider name already exists");
        }
        Provider provider = new Provider(request.name(), request.type(), request.configJson());
        provider = providerRepository.save(provider);
        return ProviderVO.from(provider);
    }

    @Transactional
    public ProviderVO update(UUID id, ProviderUpdateRequest request) {
        Provider provider = providerRepository.findById(id)
            .orElseThrow(() -> new BusinessException(404, "Provider not found"));
        if (request.name() != null) provider.setName(request.name());
        if (request.configJson() != null) provider.setConfigJson(request.configJson());
        provider = providerRepository.save(provider);
        return ProviderVO.from(provider);
    }

    @Transactional
    public void delete(UUID id) {
        if (!providerRepository.existsById(id)) {
            throw new BusinessException(404, "Provider not found");
        }
        providerRepository.deleteById(id);
    }

    @Transactional
    public void setStatus(UUID id, ProviderStatus status) {
        Provider provider = providerRepository.findById(id)
            .orElseThrow(() -> new BusinessException(404, "Provider not found"));
        provider.setStatus(status);
        providerRepository.save(provider);
    }

    public List<Provider> getEnabledProviders() {
        return providerRepository.findByStatus(ProviderStatus.enabled);
    }
}
