package com.maas.registry.controller;

import com.maas.common.dto.ApiResponse;
import com.maas.registry.dto.*;
import com.maas.registry.entity.ToolSource;
import com.maas.registry.service.ToolDefinitionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tools")
public class ToolDefinitionController {

    private final ToolDefinitionService service;

    public ToolDefinitionController(ToolDefinitionService service) {
        this.service = service;
    }

    @GetMapping
    public ApiResponse<List<ToolVO>> list(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) ToolSource source,
            @RequestParam(required = false) Boolean enabled) {
        if (q != null || source != null || enabled != null) {
            return ApiResponse.success(service.search(q, source, enabled));
        }
        return ApiResponse.success(service.listAll());
    }

    @GetMapping("/{id}")
    public ApiResponse<ToolVO> get(@PathVariable UUID id) {
        return ApiResponse.success(service.findById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('admin')")
    public ApiResponse<ToolVO> create(@Valid @RequestBody ToolCreateRequest req) {
        return ApiResponse.success(service.create(req));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('admin')")
    public ApiResponse<ToolVO> update(@PathVariable UUID id, @RequestBody ToolUpdateRequest req) {
        return ApiResponse.success(service.update(id, req));
    }

    @PatchMapping("/{id}/toggle")
    @PreAuthorize("hasRole('admin')")
    public ApiResponse<ToolVO> toggle(@PathVariable UUID id) {
        return ApiResponse.success(service.toggleEnabled(id));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('admin')")
    public void delete(@PathVariable UUID id) {
        service.delete(id);
    }
}
