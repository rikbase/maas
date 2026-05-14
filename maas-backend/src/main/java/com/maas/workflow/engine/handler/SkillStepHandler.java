package com.maas.workflow.engine.handler;

import com.maas.registry.repository.SkillRepository;
import com.maas.workflow.engine.StepHandler;
import com.maas.workflow.engine.StepOutput;
import com.maas.workflow.engine.TemplateEngine;
import com.maas.workflow.entity.StepType;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
public class SkillStepHandler implements StepHandler {

    private final SkillRepository skillRepository;

    public SkillStepHandler(SkillRepository skillRepository) {
        this.skillRepository = skillRepository;
    }

    @Override
    public StepType supportedType() {
        return StepType.skill;
    }

    @Override
    public StepOutput execute(Map<String, Object> config, Map<String, Object> context, TemplateEngine templateEngine) {
        try {
            String skillIdStr = (String) config.get("skillId");
            if (skillIdStr == null) {
                return StepOutput.error("skillId is required for skill step");
            }
            UUID skillId = UUID.fromString(skillIdStr);

            var skill = skillRepository.findById(skillId)
                .orElseThrow(() -> new IllegalArgumentException("Skill not found: " + skillId));

            return StepOutput.success(Map.of(
                "result", "skill executed",
                "skillName", skill.getName(),
                "skillType", skill.getType() != null ? skill.getType() : "unknown",
                "config", skill.getConfigJson() != null ? skill.getConfigJson() : "{}"
            ));
        } catch (Exception e) {
            return StepOutput.error("Skill execution failed: " + e.getMessage());
        }
    }
}
