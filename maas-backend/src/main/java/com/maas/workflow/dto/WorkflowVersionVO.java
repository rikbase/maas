package com.maas.workflow.dto;

import com.maas.workflow.entity.WorkflowVersion;
import com.maas.workflow.entity.WorkflowStatus;
import java.time.Instant;
import java.util.UUID;

public record WorkflowVersionVO(
    UUID id,
    UUID workflowId,
    Integer version,
    String definitionJson,
    WorkflowStatus status,
    UUID createdBy,
    Instant createdAt
) {
    public static WorkflowVersionVO from(WorkflowVersion v) {
        return new WorkflowVersionVO(
            v.getId(), v.getWorkflowId(), v.getVersion(),
            v.getDefinitionJson(), v.getStatus(), v.getCreatedBy(),
            v.getCreatedAt()
        );
    }
}
