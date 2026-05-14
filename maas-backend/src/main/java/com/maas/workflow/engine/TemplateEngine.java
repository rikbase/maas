package com.maas.workflow.engine;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class TemplateEngine {

    private static final Pattern VAR_PATTERN = Pattern.compile("\\{\\{(.+?)\\}\\}");
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String resolve(String template, Map<String, Object> context) {
        if (template == null) return null;
        Matcher m = VAR_PATTERN.matcher(template);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String path = m.group(1).trim();
            Object value = resolvePath(path, context);
            String replacement = value != null ? value.toString() : "{{" + path + "}}";
            m.appendReplacement(sb, Matcher.quoteReplacement(replacement));
        }
        m.appendTail(sb);
        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    private Object resolvePath(String path, Map<String, Object> context) {
        String[] parts = path.split("\\.");
        Object current = context;
        for (String part : parts) {
            if (current == null) return null;
            int bracketIdx = part.indexOf('[');
            if (bracketIdx >= 0) {
                String key = part.substring(0, bracketIdx);
                int endBracket = part.indexOf(']', bracketIdx);
                int index = Integer.parseInt(part.substring(bracketIdx + 1, endBracket));
                if (current instanceof Map) {
                    current = ((Map<String, Object>) current).get(key);
                } else if (current instanceof JsonNode) {
                    current = ((JsonNode) current).get(key);
                } else {
                    return null;
                }
                if (current instanceof List) {
                    current = ((List<Object>) current).get(index);
                } else if (current instanceof JsonNode) {
                    current = ((JsonNode) current).get(index);
                } else {
                    return null;
                }
            } else {
                if (current instanceof Map) {
                    current = ((Map<String, Object>) current).get(part);
                } else if (current instanceof JsonNode) {
                    current = ((JsonNode) current).get(part);
                } else {
                    return null;
                }
            }
        }
        return current;
    }
}
