package com.maas.workflow.repository;

import com.maas.workflow.entity.WorkflowTrigger;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface WorkflowTriggerRepository extends JpaRepository<WorkflowTrigger, UUID> {
    List<WorkflowTrigger> findByWorkflowId(UUID workflowId);
    List<WorkflowTrigger> findByTriggerTypeAndStatus(String triggerType, String status);
    void deleteByWorkflowId(UUID workflowId);
}
