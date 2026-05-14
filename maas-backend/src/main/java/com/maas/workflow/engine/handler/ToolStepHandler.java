package com.maas.workflow.engine.handler;

import com.maas.mcp.repository.McpToolRepository;
import com.maas.registry.entity.ToolDefinition;
import com.maas.registry.entity.ToolSource;
import com.maas.registry.repository.ToolDefinitionRepository;
import com.maas.workflow.engine.StepHandler;
import com.maas.workflow.engine.StepOutput;
import com.maas.workflow.engine.TemplateEngine;
import com.maas.workflow.entity.StepType;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
public class ToolStepHandler implements StepHandler {

    private final ToolDefinitionRepository toolDefRepository;
    private final McpToolRepository mcpToolRepository;

    public ToolStepHandler(ToolDefinitionRepository toolDefRepository, McpToolRepository mcpToolRepository) {
        this.toolDefRepository = toolDefRepository;
        this.mcpToolRepository = mcpToolRepository;
    }

    @Override
    public StepType supportedType() {
        return StepType.tool;
    }

    @Override
    public StepOutput execute(Map<String, Object> config, Map<String, Object> context, TemplateEngine templateEngine) {
        try {
            String toolIdStr = (String) config.get("toolId");
            if (toolIdStr == null) {
                return StepOutput.error("toolId is required for tool step");
            }
            UUID toolId = UUID.fromString(toolIdStr);

            ToolDefinition toolDef = toolDefRepository.findById(toolId)
                .orElseThrow(() -> new IllegalArgumentException("Tool definition not found: " + toolId));

            if (toolDef.getSource() == ToolSource.built_in) {
                return StepOutput.success(Map.of("result", "built-in tool executed", "toolName", toolDef.getName()));
            } else if (toolDef.getSource() == ToolSource.mcp) {
                var mcpTool = mcpToolRepository.findById(toolId).orElse(null);
                if (mcpTool != null && mcpTool.getInputSchema() != null) {
                    return StepOutput.success(Map.of(
                        "result", "MCP tool executed",
                        "toolName", mcpTool.getName(),
                        "inputSchema", mcpTool.getInputSchema()
                    ));
                }
                return StepOutput.success(Map.of("result", "MCP tool executed", "toolName", toolDef.getName()));
            } else {
                return StepOutput.success(Map.of("result", "API tool invoked", "toolName", toolDef.getName()));
            }
        } catch (Exception e) {
            return StepOutput.error("Tool execution failed: " + e.getMessage());
        }
    }
}
