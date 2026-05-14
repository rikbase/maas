package com.maas.user.controller;

import com.maas.common.dto.ApiResponse;
import com.maas.log.service.LoginLogService;
import com.maas.user.dto.LoginRequest;
import com.maas.user.dto.LoginResponse;
import com.maas.user.dto.UserVO;
import com.maas.user.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserService userService;
    private final LoginLogService loginLogService;

    public AuthController(UserService userService, LoginLogService loginLogService) {
        this.userService = userService;
        this.loginLogService = loginLogService;
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request,
                                            HttpServletRequest httpRequest,
                                            HttpServletResponse response) {
        String ip = httpRequest.getRemoteAddr();
        String userAgent = httpRequest.getHeader("User-Agent");
        try {
            LoginResponse loginResp = userService.login(request);
            // Record success
            loginLogService.record(request.username(), ip, userAgent, true, null);
            // Set JWT as cookie for OAuth2/OIDC browser flow
            Cookie cookie = new Cookie("maas_token", loginResp.token());
            cookie.setHttpOnly(true);
            cookie.setPath("/");
            cookie.setMaxAge(86400); // 24 hours
            response.addCookie(cookie);
            return ApiResponse.success(loginResp);
        } catch (Exception e) {
            // Record failure
            loginLogService.record(request.username(), ip, userAgent, false, e.getMessage());
            throw e;
        }
    }

    @GetMapping("/me")
    public ApiResponse<UserVO> me(Authentication authentication) {
        UUID userId = UUID.fromString((String) authentication.getDetails());
        UserVO user = userService.getCurrentUser(userId);
        return ApiResponse.success(user);
    }
}
