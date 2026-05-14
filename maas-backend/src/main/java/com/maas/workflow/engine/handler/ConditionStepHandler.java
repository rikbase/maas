package com.maas.workflow.engine.handler;

import com.maas.workflow.engine.StepHandler;
import com.maas.workflow.engine.StepOutput;
import com.maas.workflow.engine.TemplateEngine;
import com.maas.workflow.entity.StepType;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ConditionStepHandler implements StepHandler {

    @Override
    public StepType supportedType() {
        return StepType.condition;
    }

    @Override
    public StepOutput execute(Map<String, Object> config, Map<String, Object> context, TemplateEngine templateEngine) {
        try {
            String expression = (String) config.get("expression");
            if (expression == null) {
                return StepOutput.error("expression is required for condition step");
            }

            String resolved = templateEngine.resolve(expression, context);
            boolean result = evaluate(resolved);
            return StepOutput.success(Map.of("result", result));
        } catch (Exception e) {
            return StepOutput.error("Condition evaluation failed: " + e.getMessage());
        }
    }

    private boolean evaluate(String expr) {
        expr = expr.trim();

        if (expr.contains("&&")) {
            String[] parts = expr.split("&&", 2);
            return evaluate(parts[0].trim()) && evaluate(parts[1].trim());
        }
        if (expr.contains("||")) {
            String[] parts = expr.split("\\|\\|", 2);
            return evaluate(parts[0].trim()) || evaluate(parts[1].trim());
        }

        if (expr.contains(">=")) return compare(expr, ">=") >= 0;
        if (expr.contains("<=")) return compare(expr, "<=") <= 0;
        if (expr.contains(">")) return compare(expr, ">") > 0;
        if (expr.contains("<")) return compare(expr, "<") < 0;
        if (expr.contains("==")) {
            String[] parts = expr.split("==", 2);
            return parts[0].trim().equals(parts[1].trim());
        }
        if (expr.contains("!=")) {
            String[] parts = expr.split("!=", 2);
            return !parts[0].trim().equals(parts[1].trim());
        }

        return Boolean.parseBoolean(expr);
    }

    private double compare(String expr, String op) {
        String[] parts = expr.split(java.util.regex.Pattern.quote(op), 2);
        double left = Double.parseDouble(parts[0].trim());
        double right = Double.parseDouble(parts[1].trim());
        return left - right;
    }
}
