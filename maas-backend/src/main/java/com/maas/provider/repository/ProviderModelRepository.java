package com.maas.provider.repository;

import com.maas.provider.entity.ProviderModel;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface ProviderModelRepository extends JpaRepository<ProviderModel, UUID> {
    List<ProviderModel> findByProviderId(UUID providerId);
    void deleteByProviderId(UUID providerId);
}
