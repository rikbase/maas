package com.maas.workflow.dto;

import com.maas.workflow.entity.WorkflowTrigger;

import java.time.Instant;
import java.util.UUID;

public record WorkflowTriggerVO(
    UUID id, UUID workflowId, String triggerType,
    String cronExpression, String webhookSecret,
    String status, Instant lastFiredAt,
    Instant createdAt, Instant updatedAt
) {
    public static WorkflowTriggerVO from(WorkflowTrigger t) {
        return new WorkflowTriggerVO(
            t.getId(), t.getWorkflowId(), t.getTriggerType(),
            t.getCronExpression(), t.getWebhookSecret(),
            t.getStatus(), t.getLastFiredAt(),
            t.getCreatedAt(), t.getUpdatedAt()
        );
    }
}
