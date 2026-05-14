package com.maas.security.detector;

import com.maas.security.dto.InspectionResult;
import com.maas.security.entity.DetectorType;
import com.maas.security.entity.SecurityEvent;
import com.maas.security.entity.RuleAction;
import com.maas.security.entity.SecurityRule;
import com.maas.security.repository.SecurityEventRepository;
import com.maas.security.repository.SecurityRuleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class SecurityInspector {

    private static final Logger log = LoggerFactory.getLogger(SecurityInspector.class);

    private final SecurityRuleRepository ruleRepository;
    private final SecurityEventRepository eventRepository;
    private final Map<DetectorType, Detector> detectors;

    public SecurityInspector(SecurityRuleRepository ruleRepository,
                             SecurityEventRepository eventRepository,
                             List<Detector> detectorList) {
        this.ruleRepository = ruleRepository;
        this.eventRepository = eventRepository;
        this.detectors = detectorList.stream()
            .collect(Collectors.toUnmodifiableMap(Detector::supportedType, d -> d));
    }

    public InspectionResult inspectRequest(String content, String direction,
                                           UUID apiKeyId, String provider, String model) {
        return inspect(content, direction, apiKeyId, provider, model, "request");
    }

    public InspectionResult inspectResponse(String content, String direction,
                                            UUID apiKeyId, String provider, String model) {
        return inspect(content, direction, apiKeyId, provider, model, "response");
    }

    private InspectionResult inspect(String content, String direction,
                                     UUID apiKeyId, String provider, String model,
                                     String currentDir) {
        List<SecurityRule> rules = ruleRepository.findByEnabledTrue();
        if (rules.isEmpty()) return InspectionResult.ok();

        List<String> allMatchedRules = new ArrayList<>();
        RuleAction highestAction = RuleAction.audit;

        for (SecurityRule rule : rules) {
            if (!directionMatches(rule, currentDir)) continue;

            Detector detector = detectors.get(rule.getDetectorType());
            if (detector == null) {
                log.warn("No detector found for type: {}", rule.getDetectorType());
                continue;
            }

            InspectionResult result = detector.detect(content, rule);
            if (!result.matchedRuleNames().isEmpty()) {
                allMatchedRules.addAll(result.matchedRuleNames());
            }

            if (!result.passed()) {
                highestAction = maxAction(highestAction, rule.getAction());
                allMatchedRules.add(rule.getName());

                SecurityEvent event = new SecurityEvent(
                    rule.getId(), apiKeyId, provider, model,
                    currentDir, rule.getDetectorType().name(),
                    rule.getSeverity().name(), rule.getAction().name(),
                    truncate(content, 500),
                    truncate(detector.detectMatches(content, rule).toString(), 1000)
                );
                eventRepository.save(event);
            }
        }

        if (allMatchedRules.isEmpty()) {
            return InspectionResult.ok();
        }

        return new InspectionResult(
            highestAction != RuleAction.block,
            highestAction,
            allMatchedRules
        );
    }

    private boolean directionMatches(SecurityRule rule, String currentDir) {
        String scope = rule.getScopeJson();
        if (scope == null || scope.isBlank() || scope.contains("all")) return true;
        return scope.contains(currentDir);
    }

    private RuleAction maxAction(RuleAction a, RuleAction b) {
        return a.ordinal() > b.ordinal() ? a : b;
    }

    private String truncate(String s, int maxLen) {
        if (s == null) return null;
        return s.length() <= maxLen ? s : s.substring(0, maxLen);
    }
}
