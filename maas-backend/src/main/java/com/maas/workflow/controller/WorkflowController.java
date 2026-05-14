package com.maas.workflow.controller;

import com.maas.common.dto.ApiResponse;
import com.maas.workflow.dto.*;
import com.maas.workflow.engine.WorkflowEngineService;
import com.maas.workflow.service.WorkflowService;
import jakarta.validation.Valid;
import com.maas.workflow.entity.ExecutionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/workflows")
public class WorkflowController {

    private final WorkflowService workflowService;
    private final WorkflowEngineService workflowEngineService;

    public WorkflowController(WorkflowService workflowService, WorkflowEngineService workflowEngineService) {
        this.workflowService = workflowService;
        this.workflowEngineService = workflowEngineService;
    }

    @GetMapping
    public ApiResponse<List<WorkflowVO>> list() {
        return ApiResponse.success(workflowService.findAll());
    }

    @GetMapping("/{id}")
    public ApiResponse<WorkflowVO> get(@PathVariable UUID id) {
        return ApiResponse.success(workflowService.findById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('admin')")
    public ApiResponse<WorkflowVO> create(@Valid @RequestBody WorkflowCreateRequest req) {
        return ApiResponse.success(workflowService.create(req));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('admin')")
    public ApiResponse<WorkflowVO> update(@PathVariable UUID id, @RequestBody WorkflowUpdateRequest req) {
        return ApiResponse.success(workflowService.update(id, req));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('admin')")
    public void delete(@PathVariable UUID id) {
        workflowService.delete(id);
    }

    @PostMapping("/{id}/publish")
    @PreAuthorize("hasRole('admin')")
    public ApiResponse<WorkflowVersionVO> publish(@PathVariable UUID id) {
        return ApiResponse.success(workflowService.publish(id));
    }

    @GetMapping("/{id}/versions")
    public ApiResponse<List<WorkflowVersionVO>> getVersions(@PathVariable UUID id) {
        return ApiResponse.success(workflowService.getVersions(id));
    }

    @PostMapping("/{id}/rollback")
    @PreAuthorize("hasRole('admin')")
    public ApiResponse<WorkflowVersionVO> rollback(@PathVariable UUID id, @RequestBody Map<String, Integer> body) {
        return ApiResponse.success(workflowService.rollback(id, body.get("version")));
    }

    @PostMapping("/{id}/execute")
    @PreAuthorize("hasRole('admin')")
    public ApiResponse<ExecutionVO> execute(@PathVariable UUID id, @RequestBody(required = false) Map<String, Object> triggerData) {
        return ApiResponse.success(workflowEngineService.execute(id, triggerData));
    }

    @GetMapping("/{id}/executions")
    public ApiResponse<Page<ExecutionVO>> getExecutions(
            @PathVariable UUID id,
            Pageable pageable,
            @RequestParam(required = false) ExecutionStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant end) {
        return ApiResponse.success(workflowService.getExecutions(id, pageable, status, start, end));
    }
}
