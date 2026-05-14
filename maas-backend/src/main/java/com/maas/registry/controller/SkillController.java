package com.maas.registry.controller;

import com.maas.common.dto.ApiResponse;
import com.maas.registry.dto.*;
import com.maas.registry.service.SkillService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/skills")
public class SkillController {

    private final SkillService service;

    public SkillController(SkillService service) {
        this.service = service;
    }

    @GetMapping
    public ApiResponse<List<SkillVO>> list() {
        return ApiResponse.success(service.findAll());
    }

    @GetMapping("/{id}")
    public ApiResponse<SkillVO> get(@PathVariable UUID id) {
        return ApiResponse.success(service.findById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('admin')")
    public ApiResponse<SkillVO> create(@Valid @RequestBody SkillCreateRequest req) {
        return ApiResponse.success(service.create(req));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('admin')")
    public ApiResponse<SkillVO> update(@PathVariable UUID id, @RequestBody SkillUpdateRequest req) {
        return ApiResponse.success(service.update(id, req));
    }

    @PostMapping("/{id}/publish")
    @PreAuthorize("hasRole('admin')")
    public ApiResponse<SkillVO> publish(@PathVariable UUID id, @RequestBody(required = false) Map<String, String> body) {
        String note = body != null ? body.get("note") : null;
        return ApiResponse.success(service.publish(id, note));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('admin')")
    public void delete(@PathVariable UUID id) {
        service.delete(id);
    }

    @GetMapping("/{id}/tools")
    public ApiResponse<List<ToolVO>> listTools(@PathVariable UUID id) {
        return ApiResponse.success(service.listToolsBySkill(id));
    }
}
