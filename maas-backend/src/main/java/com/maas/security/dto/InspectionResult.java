package com.maas.security.dto;

import com.maas.security.entity.RuleAction;
import java.util.List;

public record InspectionResult(
    boolean passed,
    RuleAction action,
    List<String> matchedRuleNames
) {
    public static InspectionResult ok() {
        return new InspectionResult(true, RuleAction.audit, List.of());
    }

    public static InspectionResult blocked(List<String> ruleNames) {
        return new InspectionResult(false, RuleAction.block, ruleNames);
    }

    public static InspectionResult flagged(List<String> ruleNames) {
        return new InspectionResult(true, RuleAction.flag, ruleNames);
    }
}
