package com.maas.gateway.service;

import com.maas.common.exception.BusinessException;
import com.maas.provider.entity.Provider;
import com.maas.provider.entity.ProviderStatus;
import com.maas.provider.entity.ProviderType;
import com.maas.provider.repository.ProviderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ModelRouterTest {

    @Mock ProviderRepository providerRepository;
    @InjectMocks ModelRouter modelRouter;

    @Test
    void route_shouldReturnHealthyProvider() {
        Provider p = new Provider("openai", ProviderType.openai_compatible, "{}");
        p.setHealthStatus("healthy");
        when(providerRepository.findByStatus(ProviderStatus.enabled)).thenReturn(List.of(p));

        Provider result = modelRouter.route("gpt-4", null);
        assertEquals("openai", result.getName());
    }

    @Test
    void route_shouldThrowWhenNoProvider() {
        when(providerRepository.findByStatus(ProviderStatus.enabled)).thenReturn(List.of());
        assertThrows(BusinessException.class, () -> modelRouter.route("gpt-4", null));
    }
}
