package com.maas.user.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateUserRequest(
    @NotBlank String username,
    @NotBlank String password,
    String displayName,
    String role
) {}
