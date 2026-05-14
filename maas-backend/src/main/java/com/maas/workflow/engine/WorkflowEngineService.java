package com.maas.workflow.engine;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.maas.workflow.dto.ExecutionVO;
import com.maas.workflow.dto.StepExecutionVO;
import com.maas.workflow.entity.*;
import com.maas.workflow.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

@Service
@Transactional
public class WorkflowEngineService {

    private static final Logger log = LoggerFactory.getLogger(WorkflowEngineService.class);

    private final WorkflowRepository workflowRepository;
    private final WorkflowVersionRepository versionRepository;
    private final WorkflowExecutionRepository executionRepository;
    private final StepExecutionRepository stepExecutionRepository;
    private final TemplateEngine templateEngine;
    private final Map<StepType, StepHandler> handlers;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public WorkflowEngineService(
            WorkflowRepository workflowRepository,
            WorkflowVersionRepository versionRepository,
            WorkflowExecutionRepository executionRepository,
            StepExecutionRepository stepExecutionRepository,
            TemplateEngine templateEngine,
            List<StepHandler> handlerList) {
        this.workflowRepository = workflowRepository;
        this.versionRepository = versionRepository;
        this.executionRepository = executionRepository;
        this.stepExecutionRepository = stepExecutionRepository;
        this.templateEngine = templateEngine;
        this.handlers = new HashMap<>();
        for (StepHandler h : handlerList) {
            this.handlers.put(h.supportedType(), h);
        }
    }

    public ExecutionVO execute(UUID workflowId, Map<String, Object> triggerData) {
        Workflow workflow = workflowRepository.findById(workflowId)
            .orElseThrow(() -> new RuntimeException("Workflow not found: " + workflowId));

        WorkflowVersion version = versionRepository.findByWorkflowIdOrderByVersionDesc(workflowId)
                .stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No published version for workflow: " + workflowId));

        WorkflowExecution execution = new WorkflowExecution();
        execution.setWorkflowId(workflowId);
        execution.setVersionId(version.getId());
        execution.setStatus(ExecutionStatus.running);
        execution.setTriggerType("manual");
        try {
            execution.setTriggerInfo(triggerData != null ? objectMapper.writeValueAsString(triggerData) : null);
        } catch (Exception ignored) {
            execution.setTriggerInfo(triggerData != null ? triggerData.toString() : null);
        }
        execution.setStartedAt(Instant.now());
        execution = executionRepository.save(execution);

        try {
            DAG dag = parseDag(version.getDefinitionJson());
            List<String> topoOrder = topologicalSort(dag);

            Map<String, StepOutput> context = new LinkedHashMap<>();
            context.put("trigger", StepOutput.success(triggerData != null ? triggerData : Map.of()));

            List<StepExecution> stepRecords = new ArrayList<>();

            for (String nodeId : topoOrder) {
                NodeDef node = dag.nodes.get(nodeId);
                if (node == null) continue;
                if (node.type == StepType.start || node.type == StepType.end) continue;

                StepHandler handler = handlers.get(node.type);
                if (handler == null) {
                    StepOutput output = StepOutput.error("No handler for step type: " + node.type);
                    context.put(nodeId, output);
                    stepRecords.add(createStepRecord(execution.getId(), nodeId, node.type, StepStatus.failed, null, null, "No handler for step type: " + node.type, Instant.now(), Instant.now()));
                    continue;
                }

                Map<String, Object> flatContext = flattenContext(context);
                Instant stepStarted = Instant.now();
                StepOutput output = handler.execute(node.config, flatContext, templateEngine);
                Instant stepFinished = Instant.now();
                context.put(nodeId, output);

                StepStatus stepStatus = "completed".equals(output.status()) ? StepStatus.completed : StepStatus.failed;
                stepRecords.add(createStepRecord(execution.getId(), nodeId, node.type, stepStatus, node.config, output.data(), output.error(), stepStarted, stepFinished));

                if (output.status().equals("failed")) {
                    execution.setStatus(ExecutionStatus.failed);
                    execution.setFinishedAt(Instant.now());
                    executionRepository.save(execution);
                    return buildExecutionVO(execution, stepRecords, workflow.getName());
                }
            }

            execution.setStatus(ExecutionStatus.completed);
            execution.setFinishedAt(Instant.now());
            execution = executionRepository.save(execution);

            return buildExecutionVO(execution, stepRecords, workflow.getName());
        } catch (Exception e) {
            log.error("Workflow execution failed: {}", e.getMessage(), e);
            execution.setStatus(ExecutionStatus.failed);
            execution.setFinishedAt(Instant.now());
            executionRepository.save(execution);
            return buildExecutionVO(execution, List.of(), workflow.getName());
        }
    }

    private StepExecution createStepRecord(UUID executionId, String stepId, StepType type, StepStatus status, Map<String, Object> config, Object output, String error, Instant startedAt, Instant finishedAt) {
        StepExecution se = new StepExecution();
        se.setExecutionId(executionId);
        se.setStepId(stepId);
        se.setStepType(type);
        se.setStatus(status);
        try {
            se.setInput(config != null ? objectMapper.writeValueAsString(config) : "{}");
            se.setOutput(output != null ? objectMapper.writeValueAsString(output) : null);
        } catch (Exception ignored) {}
        se.setErrorMessage(error);
        se.setStartedAt(startedAt);
        se.setFinishedAt(finishedAt);
        return stepExecutionRepository.save(se);
    }

    private Map<String, Object> flattenContext(Map<String, StepOutput> context) {
        Map<String, Object> flat = new LinkedHashMap<>();
        for (var entry : context.entrySet()) {
            StepOutput output = entry.getValue();
            flat.put(entry.getKey(), output.data() != null ? output.data() : Map.of());
        }
        return flat;
    }

    private DAG parseDag(String definitionJson) {
        try {
            Object parsed = objectMapper.readValue(definitionJson, Object.class);
            // Handle double-escaped JSON (JSON string containing JSON)
            if (parsed instanceof String jsonStr) {
                parsed = objectMapper.readValue(jsonStr, Map.class);
            }
            @SuppressWarnings("unchecked")
            Map<String, Object> def = (Map<String, Object>) parsed;
            DAG dag = new DAG();

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> nodes = (List<Map<String, Object>>) def.get("nodes");
            if (nodes != null) {
                for (Map<String, Object> n : nodes) {
                    NodeDef node = new NodeDef();
                    node.id = (String) n.get("id");
                    String typeStr = (String) n.get("type");
                    node.type = StepType.valueOf(typeStr);
                    @SuppressWarnings("unchecked")
                    Map<String, Object> cfg = (Map<String, Object>) n.get("data");
                    node.config = cfg != null ? cfg : Map.of();
                    dag.nodes.put(node.id, node);
                }
            }

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> edges = (List<Map<String, Object>>) def.get("edges");
            if (edges != null) {
                for (Map<String, Object> e : edges) {
                    String from = (String) e.get("source");
                    String to = (String) e.get("target");
                    if (from != null && to != null) {
                        dag.edges.add(new EdgeDef(from, to));
                    }
                }
            }

            return dag;
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse DAG definition: " + e.getMessage(), e);
        }
    }

    private List<String> topologicalSort(DAG dag) {
        Map<String, Integer> inDegree = new HashMap<>();
        Map<String, List<String>> adjacency = new HashMap<>();

        for (String nodeId : dag.nodes.keySet()) {
            inDegree.putIfAbsent(nodeId, 0);
            adjacency.putIfAbsent(nodeId, new ArrayList<>());
        }

        for (EdgeDef edge : dag.edges) {
            adjacency.computeIfAbsent(edge.from, k -> new ArrayList<>()).add(edge.to);
            inDegree.merge(edge.to, 1, Integer::sum);
        }

        Queue<String> queue = new LinkedList<>();
        for (var entry : inDegree.entrySet()) {
            if (entry.getValue() == 0) queue.add(entry.getKey());
        }

        List<String> result = new ArrayList<>();
        while (!queue.isEmpty()) {
            String node = queue.poll();
            result.add(node);
            for (String neighbor : adjacency.getOrDefault(node, List.of())) {
                int deg = inDegree.merge(neighbor, -1, Integer::sum);
                if (deg == 0) queue.add(neighbor);
            }
        }

        if (result.size() != dag.nodes.size()) {
            throw new RuntimeException("Cycle detected in workflow DAG");
        }

        return result;
    }

    private ExecutionVO buildExecutionVO(WorkflowExecution e, List<StepExecution> steps, String workflowName) {
        List<StepExecutionVO> stepVOs = steps.stream().map(StepExecutionVO::from).toList();
        return ExecutionVO.from(e, stepVOs, workflowName);
    }

    static class DAG {
        Map<String, NodeDef> nodes = new LinkedHashMap<>();
        List<EdgeDef> edges = new ArrayList<>();
    }

    static class NodeDef {
        String id;
        StepType type;
        Map<String, Object> config = new HashMap<>();
    }

    record EdgeDef(String from, String to) {}
}
