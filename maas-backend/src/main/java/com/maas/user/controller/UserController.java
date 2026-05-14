package com.maas.user.controller;

import com.maas.common.dto.ApiResponse;
import com.maas.user.dto.CreateUserRequest;
import com.maas.user.dto.UpdateUserRequest;
import com.maas.user.dto.UserVO;
import com.maas.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @PreAuthorize("hasRole('admin')")
    public ApiResponse<List<UserVO>> list() {
        return ApiResponse.success(userService.list());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('admin')")
    public ApiResponse<UserVO> get(@PathVariable UUID id) {
        return ApiResponse.success(userService.get(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('admin')")
    public ApiResponse<UserVO> create(@Valid @RequestBody CreateUserRequest request) {
        return ApiResponse.success(userService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('admin')")
    public ApiResponse<UserVO> update(@PathVariable UUID id, @RequestBody UpdateUserRequest request) {
        return ApiResponse.success(userService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('admin')")
    public ApiResponse<Void> delete(@PathVariable UUID id) {
        userService.delete(id);
        return ApiResponse.success(null);
    }
}
