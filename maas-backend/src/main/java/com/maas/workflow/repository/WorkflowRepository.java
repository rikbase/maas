package com.maas.workflow.repository;

import com.maas.workflow.entity.Workflow;
import com.maas.workflow.entity.WorkflowStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface WorkflowRepository extends JpaRepository<Workflow, UUID> {
    List<Workflow> findByStatus(WorkflowStatus status);
}
