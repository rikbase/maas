package com.maas.provider.controller;

import com.maas.common.dto.ApiResponse;
import com.maas.provider.dto.ProviderCreateRequest;
import com.maas.provider.dto.ProviderUpdateRequest;
import com.maas.provider.dto.ProviderVO;
import com.maas.provider.entity.ProviderStatus;
import com.maas.provider.service.ProviderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/providers")
public class ProviderController {
    private final ProviderService providerService;

    public ProviderController(ProviderService providerService) {
        this.providerService = providerService;
    }

    @GetMapping
    public ApiResponse<List<ProviderVO>> list() {
        return ApiResponse.success(providerService.listAll());
    }

    @GetMapping("/{id}")
    public ApiResponse<ProviderVO> get(@PathVariable UUID id) {
        return ApiResponse.success(providerService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('admin')")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ProviderVO> create(@Valid @RequestBody ProviderCreateRequest request) {
        return ApiResponse.success(providerService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('admin')")
    public ApiResponse<ProviderVO> update(@PathVariable UUID id, @Valid @RequestBody ProviderUpdateRequest request) {
        return ApiResponse.success(providerService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('admin')")
    public ApiResponse<Void> delete(@PathVariable UUID id) {
        providerService.delete(id);
        return ApiResponse.success(null);
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('admin')")
    public ApiResponse<Void> setStatus(@PathVariable UUID id, @RequestParam ProviderStatus status) {
        providerService.setStatus(id, status);
        return ApiResponse.success(null);
    }

    @PostMapping("/{id}/health-check")
    @PreAuthorize("hasRole('admin')")
    public ApiResponse<ProviderVO> checkHealth(@PathVariable UUID id) {
        return ApiResponse.success(providerService.checkHealth(id));
    }

    @PutMapping("/{id}/health-status")
    @PreAuthorize("hasRole('admin')")
    public ApiResponse<ProviderVO> setHealthStatus(@PathVariable UUID id, @RequestParam String healthStatus) {
        return ApiResponse.success(providerService.setHealthStatus(id, healthStatus));
    }
}
