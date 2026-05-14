package com.maas.workflow.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.maas.workflow.dto.*;
import com.maas.workflow.entity.*;
import com.maas.workflow.repository.*;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class WorkflowService {

    private final WorkflowRepository workflowRepository;
    private final WorkflowVersionRepository versionRepository;
    private final WorkflowExecutionRepository executionRepository;
    private final StepExecutionRepository stepExecutionRepository;
    private final ObjectMapper objectMapper;

    public WorkflowService(
            WorkflowRepository workflowRepository,
            WorkflowVersionRepository versionRepository,
            WorkflowExecutionRepository executionRepository,
            StepExecutionRepository stepExecutionRepository,
            ObjectMapper objectMapper) {
        this.workflowRepository = workflowRepository;
        this.versionRepository = versionRepository;
        this.executionRepository = executionRepository;
        this.stepExecutionRepository = stepExecutionRepository;
        this.objectMapper = objectMapper;
    }

    private String jsonToString(JsonNode node) {
        if (node == null) return null;
        try {
            if (node.isTextual()) {
                return node.textValue();
            }
            return objectMapper.writeValueAsString(node);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize definitionJson", e);
        }
    }

    public List<WorkflowVO> findAll() {
        return workflowRepository.findAll().stream()
            .map(w -> {
                Integer latestVersion = versionRepository.findByWorkflowIdOrderByVersionDesc(w.getId())
                    .stream().findFirst().map(WorkflowVersion::getVersion).orElse(null);
                String lastRunStatus = executionRepository.findByWorkflowIdOrderByCreatedAtDesc(w.getId())
                    .stream().findFirst().map(e -> e.getStatus().name()).orElse(null);
                return WorkflowVO.from(w, latestVersion, lastRunStatus);
            })
            .toList();
    }

    public WorkflowVO findById(UUID id) {
        Workflow w = workflowRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Workflow not found: " + id));
        Integer latestVersion = versionRepository.findByWorkflowIdOrderByVersionDesc(id)
            .stream().findFirst().map(WorkflowVersion::getVersion).orElse(null);
        String lastRunStatus = executionRepository.findByWorkflowIdOrderByCreatedAtDesc(id)
            .stream().findFirst().map(e -> e.getStatus().name()).orElse(null);
        return WorkflowVO.from(w, latestVersion, lastRunStatus);
    }

    public WorkflowVO create(WorkflowCreateRequest req) {
        Workflow w = new Workflow();
        w.setName(req.name());
        w.setDescription(req.description());
        w.setStatus(WorkflowStatus.draft);
        w = workflowRepository.save(w);

        if (req.definitionJson() != null) {
            WorkflowVersion v = new WorkflowVersion();
            v.setWorkflowId(w.getId());
            v.setVersion(1);
            v.setDefinitionJson(jsonToString(req.definitionJson()));
            v.setStatus(WorkflowStatus.draft);
            versionRepository.save(v);
        }

        return WorkflowVO.from(w, req.definitionJson() != null ? 1 : null, null);
    }

    public WorkflowVO update(UUID id, WorkflowUpdateRequest req) {
        Workflow w = workflowRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Workflow not found: " + id));
        if (req.name() != null) w.setName(req.name());
        if (req.description() != null) w.setDescription(req.description());
        w = workflowRepository.save(w);

        Integer latestVersion = versionRepository.findByWorkflowIdOrderByVersionDesc(id)
            .stream().findFirst().map(WorkflowVersion::getVersion).orElse(null);
        String lastRunStatus = executionRepository.findByWorkflowIdOrderByCreatedAtDesc(id)
            .stream().findFirst().map(e -> e.getStatus().name()).orElse(null);

        if (req.definitionJson() != null) {
            int nextVer = (latestVersion != null ? latestVersion : 0) + 1;
            WorkflowVersion v = new WorkflowVersion();
            v.setWorkflowId(id);
            v.setVersion(nextVer);
            v.setDefinitionJson(jsonToString(req.definitionJson()));
            v.setStatus(WorkflowStatus.draft);
            versionRepository.save(v);
            latestVersion = nextVer;
        }

        return WorkflowVO.from(w, latestVersion, lastRunStatus);
    }

    public void delete(UUID id) {
        workflowRepository.deleteById(id);
    }

    public WorkflowVersionVO publish(UUID id) {
        Workflow w = workflowRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Workflow not found: " + id));
        w.setStatus(WorkflowStatus.published);
        workflowRepository.save(w);

        List<WorkflowVersion> versions = versionRepository.findByWorkflowIdOrderByVersionDesc(id);
        WorkflowVersion latestDraft = versions.stream()
            .filter(v -> v.getStatus() == WorkflowStatus.draft)
            .findFirst().orElse(null);

        if (latestDraft != null) {
            latestDraft.setStatus(WorkflowStatus.published);
            return WorkflowVersionVO.from(versionRepository.save(latestDraft));
        }

        WorkflowVersion published = versions.stream()
            .filter(v -> v.getStatus() == WorkflowStatus.published)
            .findFirst().orElse(null);

        if (published != null) {
            return WorkflowVersionVO.from(published);
        }

        WorkflowVersion v = new WorkflowVersion();
        v.setWorkflowId(id);
        v.setVersion(1);
        v.setDefinitionJson("{}");
        v.setStatus(WorkflowStatus.published);
        return WorkflowVersionVO.from(versionRepository.save(v));
    }

    public List<WorkflowVersionVO> getVersions(UUID workflowId) {
        return versionRepository.findByWorkflowIdOrderByVersionDesc(workflowId).stream()
            .map(WorkflowVersionVO::from)
            .toList();
    }

    public WorkflowVersionVO rollback(UUID workflowId, int version) {
        WorkflowVersion source = versionRepository.findByWorkflowIdAndVersion(workflowId, version)
            .orElseThrow(() -> new RuntimeException("Version not found: " + version));

        List<WorkflowVersion> versions = versionRepository.findByWorkflowIdOrderByVersionDesc(workflowId);
        int maxVer = versions.isEmpty() ? 0 : versions.get(0).getVersion();
        int newVer = maxVer + 1;

        WorkflowVersion v = new WorkflowVersion();
        v.setWorkflowId(workflowId);
        v.setVersion(newVer);
        v.setDefinitionJson(source.getDefinitionJson());
        v.setStatus(WorkflowStatus.draft);
        return WorkflowVersionVO.from(versionRepository.save(v));
    }

    public Page<ExecutionVO> getExecutions(UUID workflowId, Pageable pageable, ExecutionStatus status, Instant start, Instant end) {
        Specification<WorkflowExecution> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (workflowId != null) {
                predicates.add(cb.equal(root.get("workflowId"), workflowId));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (start != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), start));
            }
            if (end != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), end));
            }
            query.orderBy(cb.desc(root.get("createdAt")));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        Page<WorkflowExecution> page = executionRepository.findAll(spec, pageable);

        List<UUID> execIds = page.getContent().stream().map(WorkflowExecution::getId).toList();
        Map<UUID, List<StepExecution>> stepMap = stepExecutionRepository.findByExecutionIdIn(execIds)
            .stream().collect(Collectors.groupingBy(StepExecution::getExecutionId));

        Set<UUID> wfIds = page.getContent().stream().map(WorkflowExecution::getWorkflowId).collect(Collectors.toSet());
        Map<UUID, String> wfNameMap = workflowRepository.findAllById(wfIds).stream()
            .collect(Collectors.toMap(Workflow::getId, Workflow::getName));

        return page.map(e -> {
            List<StepExecution> steps = stepMap.getOrDefault(e.getId(), List.of());
            List<StepExecutionVO> stepVOs = steps.stream().map(StepExecutionVO::from).toList();
            return ExecutionVO.from(e, stepVOs, wfNameMap.get(e.getWorkflowId()));
        });
    }

    public ExecutionVO getExecution(UUID executionId) {
        var e = executionRepository.findById(executionId)
            .orElseThrow(() -> new RuntimeException("Execution not found: " + executionId));
        var steps = stepExecutionRepository.findByExecutionId(executionId).stream()
            .map(StepExecutionVO::from).toList();
        String workflowName = workflowRepository.findById(e.getWorkflowId())
            .map(Workflow::getName).orElse(null);
        return ExecutionVO.from(e, steps, workflowName);
    }

    public Page<ExecutionVO> listAllExecutions(Pageable pageable, ExecutionStatus status, Instant start, Instant end) {
        return getExecutions(null, pageable, status, start, end);
    }

    public List<ExecutionTrendVO> getExecutionTrends(String range) {
        Instant since = Instant.now();
        if ("today".equals(range)) {
            since = since.truncatedTo(ChronoUnit.DAYS);
        } else if ("week".equals(range)) {
            since = since.minus(7, ChronoUnit.DAYS);
        } else if ("month".equals(range)) {
            since = since.minus(30, ChronoUnit.DAYS);
        } else {
            since = since.minus(7, ChronoUnit.DAYS);
        }

        List<Object[]> raw = executionRepository.findExecutionTrendsRaw(since);
        return raw.stream().map(row -> new ExecutionTrendVO(
            ((java.sql.Date) row[0]).toLocalDate(),
            ((Number) row[1]).longValue(),
            ((Number) row[2]).longValue(),
            ((Number) row[3]).longValue(),
            ((Number) row[4]).longValue(),
            ((Number) row[5]).longValue(),
            ((Number) row[6]).longValue()
        )).toList();
    }
}
