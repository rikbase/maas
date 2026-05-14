package com.maas.workflow.repository;

import com.maas.workflow.entity.StepExecution;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface StepExecutionRepository extends JpaRepository<StepExecution, UUID> {
    List<StepExecution> findByExecutionId(UUID executionId);

    List<StepExecution> findByExecutionIdIn(List<UUID> executionIds);
}
