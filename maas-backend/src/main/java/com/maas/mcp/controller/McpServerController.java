package com.maas.mcp.controller;

import com.maas.common.dto.ApiResponse;
import com.maas.mcp.dto.*;
import com.maas.mcp.service.McpServerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/mcp/servers")
public class McpServerController {

    private final McpServerService service;

    public McpServerController(McpServerService service) {
        this.service = service;
    }

    @GetMapping
    public ApiResponse<List<McpServerVO>> list() {
        return ApiResponse.success(service.findAll());
    }

    @GetMapping("/{id}")
    public ApiResponse<McpServerVO> get(@PathVariable UUID id) {
        return ApiResponse.success(service.findById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('admin')")
    public ApiResponse<McpServerVO> create(@Valid @RequestBody McpServerCreateRequest req) {
        return ApiResponse.success(service.create(req));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('admin')")
    public ApiResponse<McpServerVO> update(@PathVariable UUID id, @RequestBody McpServerUpdateRequest req) {
        return ApiResponse.success(service.update(id, req));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('admin')")
    public void delete(@PathVariable UUID id) {
        service.delete(id);
    }

    // Tools sub-resource
    @GetMapping("/{id}/tools")
    public ApiResponse<List<McpToolVO>> listTools(@PathVariable UUID id) {
        return ApiResponse.success(service.listTools(id));
    }

    @PostMapping("/{id}/tools")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('admin')")
    public ApiResponse<McpToolVO> createTool(@PathVariable UUID id, @Valid @RequestBody McpToolCreateRequest req) {
        return ApiResponse.success(service.createTool(id, req));
    }

    @DeleteMapping("/{id}/tools/{toolId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('admin')")
    public void deleteTool(@PathVariable UUID id, @PathVariable UUID toolId) {
        service.deleteTool(toolId);
    }
}
