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
import java.util.regex.Pattern;

@Component
public class SecretLeakDetector implements Detector {

    private static final Logger log = LoggerFactory.getLogger(SecretLeakDetector.class);
    private final ObjectMapper objectMapper;

    private static final List<Pattern> BUILT_IN_PATTERNS = List.of(
        Pattern.compile("sk-[a-zA-Z0-9]{20,}"),
        Pattern.compile("maas-[a-zA-Z0-9]{16,}"),
        Pattern.compile("AKIA[0-9A-Z]{16}"),
        Pattern.compile("eyJ[a-zA-Z0-9_\\-]+\\.[a-zA-Z0-9_\\-]+\\.[a-zA-Z0-9_\\-]+"),
        Pattern.compile("-----BEGIN (RSA |EC )?PRIVATE KEY-----"),
        Pattern.compile("ghp_[a-zA-Z0-9]{36}"),
        Pattern.compile("gho_[a-zA-Z0-9]{36}"),
        Pattern.compile("ghu_[a-zA-Z0-9]{36}")
    );

    public SecretLeakDetector(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public DetectorType supportedType() {
        return DetectorType.secret_leak;
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

        List<Pattern> patterns = getPatterns(rule);
        List<String> matches = new ArrayList<>();

        for (Pattern pattern : patterns) {
            var matcher = pattern.matcher(content);
            if (matcher.find()) {
                String match = matcher.group();
                matches.add(match.length() > 20 ? match.substring(0, 20) + "..." : match);
            }
        }
        return matches;
    }

    private List<Pattern> getPatterns(SecurityRule rule) {
        List<Pattern> custom = parseCustomPatterns(rule);
        if (custom.isEmpty()) return BUILT_IN_PATTERNS;

        List<Pattern> combined = new ArrayList<>(BUILT_IN_PATTERNS);
        combined.addAll(custom);
        return combined;
    }

    private List<Pattern> parseCustomPatterns(SecurityRule rule) {
        if (rule.getConfigJson() == null || rule.getConfigJson().isBlank()) {
            return Collections.emptyList();
        }
        try {
            Map<String, Object> config = objectMapper.readValue(rule.getConfigJson(),
                new TypeReference<Map<String, Object>>() {});
            Object raw = config.get("regexPatterns");
            if (raw instanceof List<?> list) {
                List<Pattern> result = new ArrayList<>();
                for (Object item : list) {
                    if (item instanceof String s) result.add(Pattern.compile(s));
                }
                return result;
            }
        } catch (Exception e) {
            log.warn("Failed to parse secret_leak config_json: {}", e.getMessage());
        }
        return Collections.emptyList();
    }
}
