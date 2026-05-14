package com.maas.log.repository;

import com.maas.log.entity.LoginLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface LoginLogRepository extends JpaRepository<LoginLog, UUID> {
    List<LoginLog> findTop10ByOrderByCreatedAtDesc();

    int countByCreatedAtAfterAndSuccessTrue(Instant since);

    int countByCreatedAtAfterAndSuccessFalse(Instant since);

    int countByCreatedAtAfter(Instant since);
}
