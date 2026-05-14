package com.maas.security.service;

import com.maas.security.dto.SecurityRuleCreateRequest;
import com.maas.security.dto.SecurityRuleUpdateRequest;
import com.maas.security.dto.SecurityRuleVO;
import com.maas.security.entity.RuleSeverity;
import com.maas.security.entity.SecurityRule;
import com.maas.security.repository.SecurityRuleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class SecurityRuleService {

    private final SecurityRuleRepository repository;

    public SecurityRuleService(SecurityRuleRepository repository) {
        this.repository = repository;
    }

    public List<SecurityRuleVO> findAll() {
        return repository.findAll().stream()
            .map(SecurityRuleVO::from)
            .toList();
    }

    public SecurityRuleVO findById(UUID id) {
        return repository.findById(id)
            .map(SecurityRuleVO::from)
            .orElseThrow(() -> new RuntimeException("SecurityRule not found: " + id));
    }

    public SecurityRuleVO create(SecurityRuleCreateRequest req) {
        SecurityRule rule = new SecurityRule(
            req.name(),
            req.detectorType(),
            req.configJson() != null ? req.configJson() : "{}",
            req.severity() != null ? req.severity() : RuleSeverity.medium,
            req.action() != null ? req.action() : com.maas.security.entity.RuleAction.block
        );
        rule.setDescription(req.description());
        rule.setScopeJson(req.scopeJson() != null ? req.scopeJson() : "{}");
        rule.setThreshold(req.threshold() != null ? req.threshold() : 0.85);
        return SecurityRuleVO.from(repository.save(rule));
    }

    public SecurityRuleVO update(UUID id, SecurityRuleUpdateRequest req) {
        SecurityRule rule = repository.findById(id)
            .orElseThrow(() -> new RuntimeException("SecurityRule not found: " + id));
        if (req.name() != null) rule.setName(req.name());
        if (req.description() != null) rule.setDescription(req.description());
        if (req.detectorType() != null) rule.setDetectorType(req.detectorType());
        if (req.configJson() != null) rule.setConfigJson(req.configJson());
        if (req.scopeJson() != null) rule.setScopeJson(req.scopeJson());
        if (req.severity() != null) rule.setSeverity(req.severity());
        if (req.action() != null) rule.setAction(req.action());
        if (req.threshold() != null) rule.setThreshold(req.threshold());
        if (req.enabled() != null) rule.setEnabled(req.enabled());
        return SecurityRuleVO.from(repository.save(rule));
    }

    public SecurityRuleVO toggleEnabled(UUID id) {
        SecurityRule rule = repository.findById(id)
            .orElseThrow(() -> new RuntimeException("SecurityRule not found: " + id));
        rule.setEnabled(!rule.isEnabled());
        return SecurityRuleVO.from(repository.save(rule));
    }

    public void delete(UUID id) {
        repository.deleteById(id);
    }
}
