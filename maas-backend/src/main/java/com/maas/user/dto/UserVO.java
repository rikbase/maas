package com.maas.user.dto;

import com.maas.user.entity.User;
import com.maas.user.entity.UserRole;
import com.maas.user.entity.UserStatus;

import java.time.Instant;
import java.util.UUID;

public record UserVO(
    UUID id,
    String username,
    String displayName,
    UserRole role,
    UserStatus status,
    Instant createdAt,
    Instant updatedAt
) {
    public static UserVO from(User user) {
        return new UserVO(
            user.getId(),
            user.getUsername(),
            user.getDisplayName(),
            user.getRole(),
            user.getStatus(),
            user.getCreatedAt(),
            user.getUpdatedAt()
        );
    }
}
