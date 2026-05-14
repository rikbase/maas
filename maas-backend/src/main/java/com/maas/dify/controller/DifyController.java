package com.maas.dify.controller;

import com.maas.common.dto.ApiResponse;
import com.maas.dify.dto.*;
import com.maas.dify.service.DifyService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/dify")
public class DifyController {

    private final DifyService difyService;

    public DifyController(DifyService difyService) {
        this.difyService = difyService;
    }

    @GetMapping
    public ApiResponse<List<DifyConfigVO>> list() {
        return ApiResponse.success(difyService.list());
    }

    @GetMapping("/{id}")
    public ApiResponse<DifyConfigVO> get(@PathVariable UUID id) {
        return ApiResponse.success(difyService.get(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('admin')")
    public ApiResponse<DifyConfigVO> create(@Valid @RequestBody DifyConfigCreateRequest req) {
        return ApiResponse.success(difyService.create(req));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('admin')")
    public ApiResponse<DifyConfigVO> update(@PathVariable UUID id, @Valid @RequestBody DifyConfigUpdateRequest req) {
        return ApiResponse.success(difyService.update(id, req));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('admin')")
    public void delete(@PathVariable UUID id) {
        difyService.delete(id);
    }

    @PostMapping("/{id}/test")
    @PreAuthorize("hasRole('admin')")
    public ApiResponse<DifyTestResult> testConnection(@PathVariable UUID id) {
        return ApiResponse.success(difyService.testConnection(id));
    }

    @GetMapping("/{id}/sso")
    public ResponseEntity<String> sso(@PathVariable UUID id) {
        String html = difyService.generateSsoHtml(id);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_HTML);
        return ResponseEntity.ok().headers(headers).body(html);
    }

    @PostMapping("/{id}/login")
    @PreAuthorize("hasRole('admin')")
    public ApiResponse<DifyTestResult> login(@PathVariable UUID id) {
        return ApiResponse.success(difyService.loginToDify(id));
    }
}
