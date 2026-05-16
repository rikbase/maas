package com.maas.workflow.service;

import com.maas.workflow.dto.WorkflowTriggerVO;
import com.maas.workflow.engine.WorkflowEngineService;
import com.maas.workflow.entity.WorkflowTrigger;
import com.maas.workflow.repository.WorkflowTriggerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class TriggerService {
    private static final Logger log = LoggerFactory.getLogger(TriggerService.class);

    private final WorkflowTriggerRepository triggerRepository;
    private final WorkflowEngineService workflowEngineService;

    public TriggerService(WorkflowTriggerRepository triggerRepository,
                          WorkflowEngineService workflowEngineService) {
        this.triggerRepository = triggerRepository;
        this.workflowEngineService = workflowEngineService;
    }

    public List<WorkflowTriggerVO> getTriggers(UUID workflowId) {
        return triggerRepository.findByWorkflowId(workflowId).stream()
            .map(WorkflowTriggerVO::from)
            .toList();
    }

    @Transactional
    public WorkflowTriggerVO createTrigger(UUID workflowId, String triggerType,
                                            String cronExpression, String webhookSecret) {
        WorkflowTrigger t = new WorkflowTrigger();
        t.setWorkflowId(workflowId);
        t.setTriggerType(triggerType);
        t.setCronExpression(cronExpression);
        t.setWebhookSecret(webhookSecret);
        t.setStatus("active");
        return WorkflowTriggerVO.from(triggerRepository.save(t));
    }

    @Transactional
    public WorkflowTriggerVO updateTrigger(UUID triggerId, String cronExpression,
                                            String webhookSecret, String status) {
        WorkflowTrigger t = triggerRepository.findById(triggerId)
            .orElseThrow(() -> new RuntimeException("Trigger not found: " + triggerId));
        if (cronExpression != null) t.setCronExpression(cronExpression);
        if (webhookSecret != null) t.setWebhookSecret(webhookSecret);
        if (status != null) t.setStatus(status);
        return WorkflowTriggerVO.from(triggerRepository.save(t));
    }

    @Transactional
    public void deleteTrigger(UUID triggerId) {
        triggerRepository.deleteById(triggerId);
    }

    // ---- Cron Scheduling ----
    @Scheduled(fixedRate = 60_000)
    public void checkCronTriggers() {
        List<WorkflowTrigger> triggers = triggerRepository
            .findByTriggerTypeAndStatus("cron", "active");
        for (WorkflowTrigger t : triggers) {
            try {
                if (shouldFire(t)) {
                    log.info("Firing cron trigger {} for workflow {}", t.getId(), t.getWorkflowId());
                    workflowEngineService.execute(t.getWorkflowId(), Map.of(
                        "trigger", "cron",
                        "triggerId", t.getId().toString(),
                        "expression", t.getCronExpression()
                    ));
                    t.setLastFiredAt(Instant.now());
                    triggerRepository.save(t);
                }
            } catch (Exception e) {
                log.error("Failed to fire cron trigger {}: {}", t.getId(), e.getMessage());
            }
        }
    }

    private boolean shouldFire(WorkflowTrigger t) {
        if (t.getCronExpression() == null) return false;
        String expr = t.getCronExpression().trim();
        if (expr.isEmpty()) return false;

        Instant now = Instant.now();
        Instant lastFired = t.getLastFiredAt();

        // Parse simple cron-like intervals: "every_X_minutes", "every_X_hours"
        if (expr.startsWith("every_")) {
            long intervalMs = parseInterval(expr);
            if (intervalMs <= 0) return false;
            return lastFired == null || now.toEpochMilli() - lastFired.toEpochMilli() >= intervalMs;
        }

        // For full cron expressions, check minute/hour/day matches
        // Simplified: check "M H" format like "30 9" = 9:30 daily
        return matchCron(expr, now, lastFired);
    }

    private long parseInterval(String expr) {
        try {
            if (expr.endsWith("_minutes")) {
                int n = Integer.parseInt(expr.replace("every_", "").replace("_minutes", ""));
                return n * 60_000L;
            } else if (expr.endsWith("_hours")) {
                int n = Integer.parseInt(expr.replace("every_", "").replace("_hours", ""));
                return n * 3600_000L;
            }
        } catch (NumberFormatException ignored) {}
        return 0;
    }

    private boolean matchCron(String expr, Instant now, Instant lastFired) {
        try {
            String[] parts = expr.split("\\s+");
            if (parts.length < 2) return false;

            int minute = Integer.parseInt(parts[0]);
            int hour = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;

            java.time.ZonedDateTime zdt = now.atZone(java.time.ZoneId.systemDefault());
            if (zdt.getHour() != hour || zdt.getMinute() != minute) return false;

            if (parts.length > 2) {
                int dom = Integer.parseInt(parts[2]);
                if (zdt.getDayOfMonth() != dom) return false;
            }
            if (parts.length > 3) {
                int month = Integer.parseInt(parts[3]);
                if (zdt.getMonthValue() != month) return false;
            }
            if (parts.length > 4) {
                int dow = Integer.parseInt(parts[4]);
                if (zdt.getDayOfWeek().getValue() != dow) return false;
            }

            if (lastFired == null) return true;
            return now.toEpochMilli() - lastFired.toEpochMilli() >= 60_000;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    // ---- Webhook ----
    public void fireWebhook(UUID triggerId, Map<String, Object> payload) {
        WorkflowTrigger t = triggerRepository.findById(triggerId)
            .orElseThrow(() -> new RuntimeException("Webhook trigger not found: " + triggerId));

        if (!"active".equals(t.getStatus())) {
            throw new RuntimeException("Webhook trigger is not active");
        }

        workflowEngineService.execute(t.getWorkflowId(), Map.of(
            "trigger", "webhook",
            "triggerId", t.getId().toString(),
            "payload", payload
        ));

        t.setLastFiredAt(Instant.now());
        triggerRepository.save(t);
    }
}
