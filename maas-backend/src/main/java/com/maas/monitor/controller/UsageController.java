package com.maas.monitor.controller;

import com.maas.common.dto.ApiResponse;
import com.maas.monitor.service.UsageService;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

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
            usageService.getTotalCostToday()
        );
        return ApiResponse.success(stats);
    }

    record UsageStats(long requestCount, BigDecimal totalCost) {}
}
