package com.maas.provider.service;

import com.maas.common.exception.BusinessException;
import com.maas.provider.dto.ProviderCreateRequest;
import com.maas.provider.dto.ProviderVO;
import com.maas.provider.entity.Provider;
import com.maas.provider.entity.ProviderType;
import com.maas.provider.repository.ProviderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProviderServiceTest {

    @Mock ProviderRepository providerRepository;
    @InjectMocks ProviderService providerService;

    @Test
    void create_shouldSaveProvider() {
        when(providerRepository.existsByName("test")).thenReturn(false);
        when(providerRepository.save(any())).thenAnswer(invocation -> {
            Provider p = invocation.getArgument(0);
            p.setId(UUID.randomUUID());
            return p;
        });

        ProviderVO vo = providerService.create(
            new ProviderCreateRequest("test", ProviderType.openai_compatible, "{}"));

        assertNotNull(vo.id());
        assertEquals("test", vo.name());
        assertEquals(ProviderType.openai_compatible, vo.type());
    }

    @Test
    void create_shouldThrowWhenNameExists() {
        when(providerRepository.existsByName("test")).thenReturn(true);
        assertThrows(BusinessException.class, () ->
            providerService.create(new ProviderCreateRequest("test", ProviderType.openai_compatible, "{}")));
    }

    @Test
    void getById_shouldThrowWhenNotFound() {
        when(providerRepository.findById(any())).thenReturn(Optional.empty());
        assertThrows(BusinessException.class, () -> providerService.getById(UUID.randomUUID()));
    }

    @Test
    void listAll_shouldReturnAll() {
        when(providerRepository.findAll()).thenReturn(List.of(new Provider("a", ProviderType.openai_compatible, "{}")));
        assertEquals(1, providerService.listAll().size());
    }
}
