package com.maas.user.controller;

import com.maas.common.dto.ApiResponse;
import com.maas.user.dto.LoginRequest;
import com.maas.user.dto.LoginResponse;
import com.maas.user.dto.UserVO;
import com.maas.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = userService.login(request);
        return ApiResponse.success(response);
    }

    @GetMapping("/me")
    public ApiResponse<UserVO> me(Authentication authentication) {
        UUID userId = UUID.fromString((String) authentication.getDetails());
        UserVO user = userService.getCurrentUser(userId);
        return ApiResponse.success(user);
    }
}
