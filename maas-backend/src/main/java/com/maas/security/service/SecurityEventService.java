package com.maas.security.service;

import com.maas.security.dto.SecurityEventVO;
import com.maas.security.dto.SecurityStatsVO;
import com.maas.security.repository.SecurityEventRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class SecurityEventService {

    private final SecurityEventRepository repository;

    public SecurityEventService(SecurityEventRepository repository) {
        this.repository = repository;
    }

    public Page<SecurityEventVO> findAll(Pageable pageable) {
        return repository.findByOrderByCreatedAtDesc(pageable)
            .map(SecurityEventVO::from);
    }

    public Page<SecurityEventVO> findByFilters(String severity, String detectorType,
                                                Instant start, Instant end, Pageable pageable) {
        if (severity != null && detectorType != null) {
            return repository.findBySeverityAndDetectorType(severity, detectorType, pageable)
                .map(SecurityEventVO::from);
        }
        if (severity != null) {
            return repository.findBySeverity(severity, pageable)
                .map(SecurityEventVO::from);
        }
        if (detectorType != null) {
            return repository.findByDetectorType(detectorType, pageable)
                .map(SecurityEventVO::from);
        }
        if (start != null && end != null) {
            return repository.findByCreatedAtBetween(start, end, pageable)
                .map(SecurityEventVO::from);
        }
        return findAll(pageable);
    }

    public SecurityStatsVO getStats() {
        long total = repository.count();
        long blocked = repository.countByActionTaken("block");
        long flagged = repository.countByActionTaken("flag");
        long promptInjection = repository.countByDetectorType("prompt_injection");
        long secretLeak = repository.countByDetectorType("secret_leak");
        long last24h = repository.countByCreatedAtAfter(Instant.now().minusSeconds(86400));

        return new SecurityStatsVO(total, blocked, flagged,
            promptInjection, secretLeak, last24h);
    }
}
