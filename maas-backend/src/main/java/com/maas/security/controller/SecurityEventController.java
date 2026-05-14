package com.maas.security.controller;

import com.maas.common.dto.ApiResponse;
import com.maas.security.dto.SecurityEventVO;
import com.maas.security.dto.SecurityStatsVO;
import com.maas.security.service.SecurityEventService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/api/security/events")
public class SecurityEventController {

    private final SecurityEventService service;

    public SecurityEventController(SecurityEventService service) {
        this.service = service;
    }

    @GetMapping
    public ApiResponse<Page<SecurityEventVO>> list(
            @RequestParam(required = false) String severity,
            @RequestParam(required = false) String detectorType,
            @RequestParam(required = false) Instant start,
            @RequestParam(required = false) Instant end,
            Pageable pageable) {
        return ApiResponse.success(service.findByFilters(severity, detectorType, start, end, pageable));
    }

    @GetMapping("/stats")
    public ApiResponse<SecurityStatsVO> stats() {
        return ApiResponse.success(service.getStats());
    }
}
