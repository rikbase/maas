package com.maas.security.controller;

import com.maas.common.dto.ApiResponse;
import com.maas.security.dto.SecurityRuleCreateRequest;
import com.maas.security.dto.SecurityRuleUpdateRequest;
import com.maas.security.dto.SecurityRuleVO;
import com.maas.security.service.SecurityRuleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/security/rules")
public class SecurityRuleController {

    private final SecurityRuleService service;

    public SecurityRuleController(SecurityRuleService service) {
        this.service = service;
    }

    @GetMapping
    public ApiResponse<List<SecurityRuleVO>> list() {
        return ApiResponse.success(service.findAll());
    }

    @GetMapping("/{id}")
    public ApiResponse<SecurityRuleVO> get(@PathVariable UUID id) {
        return ApiResponse.success(service.findById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('admin')")
    public ApiResponse<SecurityRuleVO> create(@Valid @RequestBody SecurityRuleCreateRequest req) {
        return ApiResponse.success(service.create(req));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('admin')")
    public ApiResponse<SecurityRuleVO> update(@PathVariable UUID id, @RequestBody SecurityRuleUpdateRequest req) {
        return ApiResponse.success(service.update(id, req));
    }

    @PatchMapping("/{id}/toggle")
    @PreAuthorize("hasRole('admin')")
    public ApiResponse<SecurityRuleVO> toggle(@PathVariable UUID id) {
        return ApiResponse.success(service.toggleEnabled(id));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('admin')")
    public void delete(@PathVariable UUID id) {
        service.delete(id);
    }
}
