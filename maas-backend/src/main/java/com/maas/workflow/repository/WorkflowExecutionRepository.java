package com.maas.workflow.repository;

import com.maas.workflow.entity.WorkflowExecution;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface WorkflowExecutionRepository extends JpaRepository<WorkflowExecution, UUID>, JpaSpecificationExecutor<WorkflowExecution> {
    List<WorkflowExecution> findByWorkflowIdOrderByCreatedAtDesc(UUID workflowId);

    Page<WorkflowExecution> findByWorkflowIdOrderByCreatedAtDesc(UUID workflowId, Pageable pageable);

    @Query(value = """
        SELECT DATE(created_at) as date,
               COUNT(*) as total,
               COUNT(CASE WHEN status = 'completed' THEN 1 END) as completedCount,
               COUNT(CASE WHEN status = 'failed' THEN 1 END) as failedCount,
               COUNT(CASE WHEN status = 'running' THEN 1 END) as runningCount,
               COUNT(CASE WHEN status = 'pending' THEN 1 END) as pendingCount,
               COUNT(CASE WHEN status = 'cancelled' THEN 1 END) as cancelledCount
        FROM workflow_execution
        WHERE created_at >= :since
        GROUP BY DATE(created_at)
        ORDER BY DATE(created_at)
    """, nativeQuery = true)
    List<Object[]> findExecutionTrendsRaw(@Param("since") Instant since);
}
