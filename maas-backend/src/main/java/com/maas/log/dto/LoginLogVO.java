package com.maas.log.dto;

import com.maas.log.entity.LoginLog;
import java.time.Instant;
import java.util.UUID;

public record LoginLogVO(
    UUID id,
    String username,
    String ip,
    boolean success,
    String failReason,
    Instant createdAt
) {
    public static LoginLogVO from(LoginLog log) {
        return new LoginLogVO(
            log.getId(),
            log.getUsername(),
            log.getIp(),
            log.getSuccess(),
            log.getFailReason(),
            log.getCreatedAt()
        );
    }
}
