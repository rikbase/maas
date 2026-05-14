package com.maas.security.repository;

import com.maas.security.entity.DetectorType;
import com.maas.security.entity.RuleSeverity;
import com.maas.security.entity.SecurityRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SecurityRuleRepository extends JpaRepository<SecurityRule, UUID> {
    List<SecurityRule> findByEnabledTrue();
    List<SecurityRule> findByDetectorType(DetectorType detectorType);
    List<SecurityRule> findBySeverity(RuleSeverity severity);
}
