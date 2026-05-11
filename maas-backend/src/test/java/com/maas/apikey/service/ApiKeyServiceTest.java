package com.maas.apikey.service;

import com.maas.apikey.dto.CreateKeyRequest;
import com.maas.apikey.dto.KeyVO;
import com.maas.apikey.entity.ApiKey;
import com.maas.apikey.entity.KeyStatus;
import com.maas.apikey.entity.KeyType;
import com.maas.apikey.repository.ApiKeyRepository;
import com.maas.common.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApiKeyServiceTest {

    @Mock ApiKeyRepository apiKeyRepository;
    @InjectMocks ApiKeyService apiKeyService;

    @Test
    void create_shouldReturnKeyVO() {
        when(apiKeyRepository.existsByName("test-key")).thenReturn(false);
        when(apiKeyRepository.save(any())).thenAnswer(inv -> {
            ApiKey k = inv.getArgument(0);
            k.setId(UUID.randomUUID());
            return k;
        });

        KeyVO vo = apiKeyService.create(new CreateKeyRequest("test-key", KeyType.application, null));
        assertNotNull(vo.id());
        assertEquals("test-key", vo.name());
    }

    @Test
    void create_shouldThrowOnDuplicateName() {
        when(apiKeyRepository.existsByName("dup")).thenReturn(true);
        assertThrows(BusinessException.class, () ->
            apiKeyService.create(new CreateKeyRequest("dup", KeyType.application, null)));
    }

    @Test
    void authenticate_shouldThrowOnInvalidKey() {
        when(apiKeyRepository.findByKeyHash(any())).thenReturn(Optional.empty());
        assertThrows(BusinessException.class, () -> apiKeyService.authenticate("invalid-key"));
    }

    @Test
    void authenticate_shouldSucceedWithValidKey() {
        ApiKey key = new ApiKey("hash", "k", KeyType.application);
        key.setId(UUID.randomUUID());
        key.setStatus(KeyStatus.active);
        when(apiKeyRepository.findByKeyHash(any())).thenReturn(Optional.of(key));

        assertDoesNotThrow(() -> apiKeyService.authenticate("valid-key"));
    }

    @Test
    void revoke_shouldSetStatus() {
        ApiKey key = new ApiKey("hash", "k", KeyType.application);
        key.setId(UUID.randomUUID());
        when(apiKeyRepository.findById(key.getId())).thenReturn(Optional.of(key));

        apiKeyService.revoke(key.getId());
        assertEquals(KeyStatus.revoked, key.getStatus());
    }

    @Test
    void delete_shouldDeleteExistingKey() {
        UUID id = UUID.randomUUID();
        when(apiKeyRepository.existsById(id)).thenReturn(true);

        apiKeyService.delete(id);

        verify(apiKeyRepository).deleteById(id);
    }

    @Test
    void delete_shouldThrowWhenNotFound() {
        UUID id = UUID.randomUUID();
        when(apiKeyRepository.existsById(id)).thenReturn(false);

        assertThrows(BusinessException.class, () -> apiKeyService.delete(id));
    }

    @Test
    void getById_shouldReturnKeyVO() {
        ApiKey key = new ApiKey("hash", "k", KeyType.application);
        key.setId(UUID.randomUUID());
        when(apiKeyRepository.findById(key.getId())).thenReturn(Optional.of(key));

        KeyVO vo = apiKeyService.getById(key.getId());

        assertNotNull(vo);
        assertEquals(key.getId(), vo.id());
        assertEquals(key.getName(), vo.name());
    }
}
