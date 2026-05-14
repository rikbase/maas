package com.maas.log.controller;

import com.maas.common.dto.ApiResponse;
import com.maas.log.dto.LoginLogVO;
import com.maas.log.dto.LoginStatsVO;
import com.maas.log.service.LoginLogService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/logs")
public class LoginLogController {
    private final LoginLogService loginLogService;

    public LoginLogController(LoginLogService loginLogService) {
        this.loginLogService = loginLogService;
    }

    @GetMapping("/login/recent")
    public ApiResponse<List<LoginLogVO>> getRecent() {
        return ApiResponse.success(loginLogService.getRecent());
    }

    @GetMapping("/login/stats")
    public ApiResponse<LoginStatsVO> getStats() {
        return ApiResponse.success(loginLogService.getStats());
    }
}
