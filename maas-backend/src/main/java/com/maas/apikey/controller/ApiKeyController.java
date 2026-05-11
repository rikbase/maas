package com.maas.apikey.controller;

import com.maas.apikey.dto.CreateKeyRequest;
import com.maas.apikey.dto.KeyVO;
import com.maas.apikey.service.ApiKeyService;
import com.maas.common.dto.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/keys")
public class ApiKeyController {
    private final ApiKeyService apiKeyService;

    public ApiKeyController(ApiKeyService apiKeyService) {
        this.apiKeyService = apiKeyService;
    }

    @GetMapping
    public ApiResponse<List<KeyVO>> list() {
        return ApiResponse.success(apiKeyService.listAll());
    }

    @GetMapping("/{id}")
    public ApiResponse<KeyVO> get(@PathVariable UUID id) {
        return ApiResponse.success(apiKeyService.getById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<KeyVO> create(@Valid @RequestBody CreateKeyRequest request) {
        return ApiResponse.success(apiKeyService.create(request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable UUID id) {
        apiKeyService.delete(id);
        return ApiResponse.success(null);
    }

    @PostMapping("/{id}/revoke")
    public ApiResponse<Void> revoke(@PathVariable UUID id) {
        apiKeyService.revoke(id);
        return ApiResponse.success(null);
    }
}
