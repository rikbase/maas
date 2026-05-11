package com.maas.user.dto;

import com.maas.user.entity.UserRole;

public record UserVO(
    java.util.UUID id,
    String username,
    String displayName,
    UserRole role
) {}
