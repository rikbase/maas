package com.maas.workflow.controller;

import com.maas.common.dto.ApiResponse;
import com.maas.workflow.dto.WorkflowTriggerVO;
import com.maas.workflow.service.TriggerService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/workflows/{workflowId}/triggers")
public class TriggerController {

    private final TriggerService triggerService;

    public TriggerController(TriggerService triggerService) {
        this.triggerService = triggerService;
    }

    @GetMapping
    public ApiResponse<List<WorkflowTriggerVO>> list(@PathVariable UUID workflowId) {
        return ApiResponse.success(triggerService.getTriggers(workflowId));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('admin')")
    public ApiResponse<WorkflowTriggerVO> create(
            @PathVariable UUID workflowId,
            @RequestBody CreateTriggerRequest req) {
        return ApiResponse.success(triggerService.createTrigger(
            workflowId, req.triggerType(), req.cronExpression(), req.webhookSecret()));
    }

    @PutMapping("/{triggerId}")
    @PreAuthorize("hasRole('admin')")
    public ApiResponse<WorkflowTriggerVO> update(
            @PathVariable UUID workflowId,
            @PathVariable UUID triggerId,
            @RequestBody UpdateTriggerRequest req) {
        return ApiResponse.success(triggerService.updateTrigger(
            triggerId, req.cronExpression(), req.webhookSecret(), req.status()));
    }

    @DeleteMapping("/{triggerId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('admin')")
    public void delete(@PathVariable UUID workflowId, @PathVariable UUID triggerId) {
        triggerService.deleteTrigger(triggerId);
    }

    record CreateTriggerRequest(String triggerType, String cronExpression, String webhookSecret) {}
    record UpdateTriggerRequest(String cronExpression, String webhookSecret, String status) {}
}
