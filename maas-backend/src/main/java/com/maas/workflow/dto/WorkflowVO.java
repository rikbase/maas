package com.maas.workflow.dto;

import com.maas.workflow.entity.Workflow;
import com.maas.workflow.entity.WorkflowStatus;
import java.time.Instant;
import java.util.UUID;

public record WorkflowVO(
    UUID id,
    String name,
    String description,
    WorkflowStatus status,
    Instant createdAt,
    Instant updatedAt,
    Integer latestVersion,
    String lastRunStatus
) {
    public static WorkflowVO from(Workflow w, Integer latestVersion, String lastRunStatus) {
        return new WorkflowVO(
            w.getId(), w.getName(), w.getDescription(),
            w.getStatus(), w.getCreatedAt(), w.getUpdatedAt(),
            latestVersion, lastRunStatus
        );
    }
}
