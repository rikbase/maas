package com.maas.workflow.engine;

import com.maas.workflow.entity.StepType;
import java.util.Map;

public interface StepHandler {
    StepType supportedType();
    StepOutput execute(Map<String, Object> config, Map<String, Object> context, TemplateEngine templateEngine);
}
