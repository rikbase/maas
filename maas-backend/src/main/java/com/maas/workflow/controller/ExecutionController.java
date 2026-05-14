package com.maas.workflow.controller;

import com.maas.common.dto.ApiResponse;
import com.maas.workflow.dto.ExecutionTrendVO;
import com.maas.workflow.dto.ExecutionVO;
import com.maas.workflow.entity.ExecutionStatus;
import com.maas.workflow.service.WorkflowService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/executions")
public class ExecutionController {

    private final WorkflowService workflowService;

    public ExecutionController(WorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    @GetMapping
    public ApiResponse<Page<ExecutionVO>> list(
            Pageable pageable,
            @RequestParam(required = false) ExecutionStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant end) {
        return ApiResponse.success(workflowService.listAllExecutions(pageable, status, start, end));
    }

    @GetMapping("/{id}")
    public ApiResponse<ExecutionVO> get(@PathVariable UUID id) {
        return ApiResponse.success(workflowService.getExecution(id));
    }

    @GetMapping("/trends")
    public ApiResponse<List<ExecutionTrendVO>> trends(
            @RequestParam(defaultValue = "week") String range) {
        return ApiResponse.success(workflowService.getExecutionTrends(range));
    }
}
