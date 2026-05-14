package com.maas.workflow.dto;

import com.maas.workflow.entity.WorkflowExecution;
import com.maas.workflow.entity.ExecutionStatus;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ExecutionVO(
    UUID id,
    UUID workflowId,
    String workflowName,
    UUID versionId,
    ExecutionStatus status,
    String triggerType,
    String triggerInfo,
    Instant startedAt,
    Instant finishedAt,
    Instant createdAt,
    List<StepExecutionVO> stepExecutions
) {
    public static ExecutionVO from(WorkflowExecution e, List<StepExecutionVO> steps, String workflowName) {
        return new ExecutionVO(
            e.getId(), e.getWorkflowId(), workflowName, e.getVersionId(),
            e.getStatus(), e.getTriggerType(), e.getTriggerInfo(),
            e.getStartedAt(), e.getFinishedAt(), e.getCreatedAt(),
            steps
        );
    }
}
