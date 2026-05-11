package com.maas.monitor.controller;

import com.maas.apikey.dto.KeyVO;
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
    public ApiResponse<UsageStats> stats(@RequestAttribute KeyVO apiKey) {
        long count = usageService.getRequestCountToday(apiKey.id());
        BigDecimal cost = usageService.getTotalCostToday(apiKey.id());
        return ApiResponse.success(new UsageStats(count, cost));
    }

    record UsageStats(long requestCount, BigDecimal totalCost) {}
}
