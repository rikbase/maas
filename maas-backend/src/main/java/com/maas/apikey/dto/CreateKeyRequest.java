package com.maas.apikey.dto;

import com.maas.apikey.entity.KeyType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateKeyRequest(
    @NotBlank String name,
    @NotNull KeyType keyType,
    String policyJson
) {}
