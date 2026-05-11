package com.maas.provider.repository;

import com.maas.provider.entity.Provider;
import com.maas.provider.entity.ProviderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface ProviderRepository extends JpaRepository<Provider, UUID> {
    List<Provider> findByStatus(ProviderStatus status);
    boolean existsByName(String name);
}
