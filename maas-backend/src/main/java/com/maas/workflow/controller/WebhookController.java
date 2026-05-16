package com.maas.workflow.controller;

import com.maas.common.dto.ApiResponse;
import com.maas.workflow.service.TriggerService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/webhook")
public class WebhookController {

    private final TriggerService triggerService;

    public WebhookController(TriggerService triggerService) {
        this.triggerService = triggerService;
    }

    @PostMapping("/{triggerId}")
    public ApiResponse<String> fireWebhook(
            @PathVariable UUID triggerId,
            @RequestBody(required = false) Map<String, Object> payload) {
        triggerService.fireWebhook(triggerId, payload);
        return ApiResponse.success("ok");
    }

    @GetMapping("/{triggerId}")
    public ApiResponse<String> fireWebhookGet(@PathVariable UUID triggerId) {
        triggerService.fireWebhook(triggerId, Map.of());
        return ApiResponse.success("ok");
    }
}
