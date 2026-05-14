package com.maas.workflow.engine.handler;

import com.maas.workflow.engine.StepHandler;
import com.maas.workflow.engine.StepOutput;
import com.maas.workflow.engine.TemplateEngine;
import com.maas.workflow.entity.StepType;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class LoopStepHandler implements StepHandler {

    @Override
    public StepType supportedType() {
        return StepType.loop;
    }

    @Override
    @SuppressWarnings("unchecked")
    public StepOutput execute(Map<String, Object> config, Map<String, Object> context, TemplateEngine templateEngine) {
        try {
            int iterations = 1;
            if (config.containsKey("iterations")) {
                Object iterObj = config.get("iterations");
                if (iterObj instanceof Number) {
                    iterations = ((Number) iterObj).intValue();
                } else if (iterObj instanceof String) {
                    String resolved = templateEngine.resolve((String) iterObj, context);
                    iterations = Integer.parseInt(resolved);
                }
            }

            List<Map<String, Object>> results = new ArrayList<>();
            for (int i = 0; i < iterations; i++) {
                results.add(Map.of("iteration", i, "status", "completed"));
            }

            return StepOutput.success(Map.of("iterations", iterations, "results", results));
        } catch (Exception e) {
            return StepOutput.error("Loop execution failed: " + e.getMessage());
        }
    }
}
