package com.maas.apikey.dto;

import com.maas.apikey.entity.ApiKey;
import com.maas.apikey.entity.KeyStatus;
import com.maas.apikey.entity.KeyType;
import java.time.Instant;
import java.util.UUID;

public record KeyVO(
    UUID id, String name, KeyType keyType,
    String keyPrefix, KeyStatus status,
    Instant createdAt, Instant expiresAt,
    String rawKey
) {
    public static KeyVO from(ApiKey key) {
        String prefix = key.getKeyHash().length() > 8
            ? "..." + key.getKeyHash().substring(key.getKeyHash().length() - 8)
            : "****";
        return new KeyVO(key.getId(), key.getName(), key.getKeyType(),
            prefix, key.getStatus(), key.getCreatedAt(), key.getExpiresAt(), null);
    }

    public static KeyVO from(ApiKey key, String rawKey) {
        String prefix = rawKey.length() > 8 ? rawKey.substring(0, 8) : "****";
        return new KeyVO(key.getId(), key.getName(), key.getKeyType(),
            prefix, key.getStatus(), key.getCreatedAt(), key.getExpiresAt(), rawKey);
    }
}
