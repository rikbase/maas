package com.maas.monitor.controller;

import com.maas.common.dto.ApiResponse;
import com.maas.monitor.dto.ProviderHealthVO;
import com.maas.monitor.dto.UsageRankVO;
import com.maas.monitor.dto.UsageTrendVO;
import com.maas.monitor.service.UsageService;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/usage")
public class UsageController {
    private final UsageService usageService;

    public UsageController(UsageService usageService) {
        this.usageService = usageService;
    }

    @GetMapping("/stats")
    public ApiResponse<UsageStats> stats() {
        UsageStats stats = new UsageStats(
            usageService.getTotalRequestCountToday(),
            usageService.getTotalCostToday(),
            usageService.getTotalTokensToday(),
            usageService.getTotalPromptTokensToday(),
            usageService.getTotalCompletionTokensToday(),
            usageService.getAvgLatencyToday(),
            usageService.getErrorCountToday()
        );
        return ApiResponse.success(stats);
    }

    @GetMapping("/trends")
    public ApiResponse<List<UsageTrendVO>> trends(@RequestParam(defaultValue = "today") String range) {
        return ApiResponse.success(usageService.getTrends(range));
    }

    @GetMapping("/by-model")
    public ApiResponse<List<UsageRankVO>> byModel(@RequestParam(defaultValue = "today") String range) {
        return ApiResponse.success(usageService.getUsageByModel(range));
    }

    @GetMapping("/by-provider")
    public ApiResponse<List<ProviderHealthVO>> byProvider(@RequestParam(defaultValue = "today") String range) {
        return ApiResponse.success(usageService.getProviderHealth(range));
    }

    record UsageStats(
        long requestCount, BigDecimal totalCost, long totalTokens,
        long promptTokens, long completionTokens,
        double avgLatencyMs, long errorCount
    ) {}
}
