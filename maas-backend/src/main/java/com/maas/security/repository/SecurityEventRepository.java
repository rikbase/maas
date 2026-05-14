package com.maas.security.repository;

import com.maas.security.entity.SecurityEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.UUID;

public interface SecurityEventRepository extends JpaRepository<SecurityEvent, UUID> {

    Page<SecurityEvent> findByOrderByCreatedAtDesc(Pageable pageable);

    Page<SecurityEvent> findBySeverity(String severity, Pageable pageable);

    Page<SecurityEvent> findByDetectorType(String detectorType, Pageable pageable);

    Page<SecurityEvent> findByCreatedAtBetween(Instant start, Instant end, Pageable pageable);

    Page<SecurityEvent> findBySeverityAndDetectorType(String severity, String detectorType, Pageable pageable);

    long countByCreatedAtAfter(Instant since);

    long countByActionTaken(String actionTaken);

    long countByDetectorType(String detectorType);

    @Query("SELECT COUNT(e) FROM SecurityEvent e WHERE e.createdAt >= :since AND e.actionTaken = :action")
    long countByActionSince(@Param("since") Instant since, @Param("action") String action);
}
