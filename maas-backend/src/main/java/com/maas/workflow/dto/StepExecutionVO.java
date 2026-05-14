package com.maas.workflow.dto;

import com.maas.workflow.entity.StepExecution;
import com.maas.workflow.entity.StepStatus;
import com.maas.workflow.entity.StepType;
import java.time.Instant;
import java.util.UUID;

public record StepExecutionVO(
    UUID id,
    UUID executionId,
    String stepId,
    StepType stepType,
    StepStatus status,
    String input,
    String output,
    String errorMessage,
    Instant startedAt,
    Instant finishedAt
) {
    public static StepExecutionVO from(StepExecution s) {
        return new StepExecutionVO(
            s.getId(), s.getExecutionId(), s.getStepId(),
            s.getStepType(), s.getStatus(), s.getInput(),
            s.getOutput(), s.getErrorMessage(),
            s.getStartedAt(), s.getFinishedAt()
        );
    }
}
