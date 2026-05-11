package com.maas.apikey.service;

import com.maas.apikey.dto.CreateKeyRequest;
import com.maas.apikey.dto.KeyVO;
import com.maas.apikey.entity.ApiKey;
import com.maas.apikey.entity.KeyStatus;
import com.maas.apikey.repository.ApiKeyRepository;
import com.maas.common.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Service
public class ApiKeyService {
    private final ApiKeyRepository apiKeyRepository;

    public ApiKeyService(ApiKeyRepository apiKeyRepository) {
        this.apiKeyRepository = apiKeyRepository;
    }

    public List<KeyVO> listAll() {
        return apiKeyRepository.findAll().stream()
            .map(KeyVO::from)
            .toList();
    }

    public KeyVO getById(UUID id) {
        return apiKeyRepository.findById(id)
            .map(KeyVO::from)
            .orElseThrow(() -> new BusinessException(404, "Key not found"));
    }

    @Transactional
    public KeyVO create(CreateKeyRequest request) {
        if (apiKeyRepository.existsByName(request.name())) {
            throw new BusinessException(409, "Key name already exists");
        }
        String rawKey = generateRawKey();
        String keyHash = hashKey(rawKey);
        ApiKey key = new ApiKey(keyHash, request.name(), request.keyType());
        if (request.policyJson() != null) key.setPolicyJson(request.policyJson());
        key = apiKeyRepository.save(key);
        return KeyVO.from(key, rawKey);
    }

    @Transactional
    public void delete(UUID id) {
        if (!apiKeyRepository.existsById(id)) {
            throw new BusinessException(404, "Key not found");
        }
        apiKeyRepository.deleteById(id);
    }

    @Transactional
    public void revoke(UUID id) {
        ApiKey key = apiKeyRepository.findById(id)
            .orElseThrow(() -> new BusinessException(404, "Key not found"));
        key.setStatus(KeyStatus.revoked);
        apiKeyRepository.save(key);
    }

    public KeyVO authenticate(String bearerToken) {
        String keyHash = hashKey(bearerToken);
        ApiKey key = apiKeyRepository.findByKeyHash(keyHash)
            .orElseThrow(() -> new BusinessException(401, "Invalid API key"));
        if (key.getStatus() != KeyStatus.active) {
            throw new BusinessException(401, "API key is not active");
        }
        if (key.getExpiresAt() != null && key.getExpiresAt().isBefore(java.time.Instant.now())) {
            throw new BusinessException(401, "API key has expired");
        }
        return KeyVO.from(key);
    }

    private String generateRawKey() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return "maas-" + Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hashKey(String rawKey) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(rawKey.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Key hashing failed", e);
        }
    }
}
