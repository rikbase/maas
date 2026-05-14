package com.maas.user.dto;

public record UpdateUserRequest(
    String displayName,
    String password,
    String role,
    String status
) {}
