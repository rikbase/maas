package com.maas.mcp.controller;

import com.maas.common.dto.ApiResponse;
import com.maas.mcp.service.McpRuntimeClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/mcp")
public class McpExecutionController {

    private final McpRuntimeClient runtimeClient;

    public McpExecutionController(McpRuntimeClient runtimeClient) {
        this.runtimeClient = runtimeClient;
    }

    @GetMapping("/servers/{id}/runtime-tools")
    public ApiResponse<List<McpRuntimeClient.McpToolDefinition>> listRuntimeTools(@PathVariable UUID id) {
        McpRuntimeClient.McpToolListResult result = runtimeClient.listTools(id);
        return ApiResponse.success(result.tools());
    }

    @PostMapping("/execute")
    public ApiResponse<McpRuntimeClient.McpToolCallResult> executeTool(@RequestBody ExecuteRequest req) {
        McpRuntimeClient.McpToolCallResult result = runtimeClient.executeTool(
            req.serverId(), req.toolName(), req.arguments());
        return ApiResponse.success(result);
    }

    @PostMapping("/servers/{id}/disconnect")
    public ApiResponse<Void> disconnect(@PathVariable UUID id) {
        runtimeClient.disconnect(id);
        return ApiResponse.success(null);
    }

    record ExecuteRequest(UUID serverId, String toolName, Map<String, Object> arguments) {}
}
