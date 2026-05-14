package com.maas.security.detector;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.maas.security.dto.InspectionResult;
import com.maas.security.entity.DetectorType;
import com.maas.security.entity.SecurityRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
public class PromptInjectionDetector implements Detector {

    private static final Logger log = LoggerFactory.getLogger(PromptInjectionDetector.class);
    private final ObjectMapper objectMapper;

    private static final List<String> BUILT_IN_PATTERNS = List.of(
        "ignore all previous instructions",
        "ignore all prior instructions",
        "ignore previous instructions",
        "ignore the above",
        "forget everything",
        "you are now",
        "act as if",
        "pretend to be",
        "you are free",
        "you have been released",
        "you don't need to follow",
        "you don't have to follow",
        "you don't have to abide",
        "you are not bound by",
        "bypass your",
        "override your",
        "system prompt",
        "do anything now",
        "you can do anything",
        "new role",
        "new persona",
        "jailbreak",
        "dan ",
        "do anything now"
    );

    public PromptInjectionDetector(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public DetectorType supportedType() {
        return DetectorType.prompt_injection;
    }

    @Override
    public InspectionResult detect(String content, SecurityRule rule) {
        List<String> matches = detectMatches(content, rule);
        if (matches.isEmpty()) {
            return InspectionResult.ok();
        }
        return new InspectionResult(false, rule.getAction(), matches);
    }

    @Override
    public List<String> detectMatches(String content, SecurityRule rule) {
        if (content == null || content.isBlank()) return List.of();

        String lower = content.toLowerCase();
        List<String> patterns = getPatterns(rule);
        List<String> matches = new ArrayList<>();

        for (String pattern : patterns) {
            if (lower.contains(pattern)) {
                matches.add(pattern);
            }
        }
        return matches;
    }

    private List<String> getPatterns(SecurityRule rule) {
        List<String> custom = parseCustomPatterns(rule);
        if (custom.isEmpty()) return BUILT_IN_PATTERNS;

        List<String> combined = new ArrayList<>(BUILT_IN_PATTERNS);
        combined.addAll(custom);
        return combined;
    }

    private List<String> parseCustomPatterns(SecurityRule rule) {
        if (rule.getConfigJson() == null || rule.getConfigJson().isBlank()) {
            return Collections.emptyList();
        }
        try {
            Map<String, Object> config = objectMapper.readValue(rule.getConfigJson(),
                new TypeReference<Map<String, Object>>() {});
            Object raw = config.get("patterns");
            if (raw instanceof List<?> list) {
                List<String> result = new ArrayList<>();
                for (Object item : list) {
                    if (item instanceof String s) result.add(s.toLowerCase());
                }
                return result;
            }
        } catch (Exception e) {
            log.warn("Failed to parse prompt_injection config_json: {}", e.getMessage());
        }
        return Collections.emptyList();
    }
}
