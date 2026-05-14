package com.maas.workflow.repository;

import com.maas.workflow.entity.WorkflowExecution;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.UUID;

public interface WorkflowExecutionRepository extends JpaRepository<WorkflowExecution, UUID>, JpaSpecificationExecutor<WorkflowExecution> {
    List<WorkflowExecution> findByWorkflowIdOrderByCreatedAtDesc(UUID workflowId);

    Page<WorkflowExecution> findByWorkflowIdOrderByCreatedAtDesc(UUID workflowId, Pageable pageable);
}
