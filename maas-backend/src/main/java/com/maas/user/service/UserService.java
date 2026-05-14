package com.maas.user.service;

import com.maas.common.exception.BusinessException;
import com.maas.user.dto.*;
import com.maas.user.entity.User;
import com.maas.user.entity.UserRole;
import com.maas.user.entity.UserStatus;
import com.maas.user.repository.UserRepository;
import com.maas.user.util.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.username())
            .orElseThrow(() -> new BusinessException(401, "Invalid username or password"));

        if (user.getStatus() != UserStatus.active) {
            throw new BusinessException(401, "User is disabled");
        }

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BusinessException(401, "Invalid username or password");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole().name());
        return new LoginResponse(token, UserVO.from(user));
    }

    public UserVO getCurrentUser(UUID userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new BusinessException(404, "User not found"));
        return UserVO.from(user);
    }

    public List<UserVO> list() {
        return userRepository.findAll().stream()
            .map(UserVO::from)
            .toList();
    }

    public UserVO get(UUID id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new BusinessException(404, "User not found"));
        return UserVO.from(user);
    }

    public UserVO create(CreateUserRequest request) {
        if (userRepository.findByUsername(request.username()).isPresent()) {
            throw new BusinessException(409, "Username already exists");
        }

        UserRole role = UserRole.viewer;
        if (request.role() != null) {
            try {
                role = UserRole.valueOf(request.role());
            } catch (IllegalArgumentException e) {
                throw new BusinessException(400, "Invalid role: " + request.role());
            }
        }

        User user = new User(
            request.username(),
            passwordEncoder.encode(request.password()),
            request.displayName(),
            role
        );
        user = userRepository.save(user);
        return UserVO.from(user);
    }

    public UserVO update(UUID id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new BusinessException(404, "User not found"));

        if (request.displayName() != null) {
            user.setDisplayName(request.displayName());
        }
        if (request.password() != null && !request.password().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(request.password()));
        }
        if (request.role() != null) {
            try {
                user.setRole(UserRole.valueOf(request.role()));
            } catch (IllegalArgumentException e) {
                throw new BusinessException(400, "Invalid role: " + request.role());
            }
        }
        if (request.status() != null) {
            try {
                user.setStatus(UserStatus.valueOf(request.status()));
            } catch (IllegalArgumentException e) {
                throw new BusinessException(400, "Invalid status: " + request.status());
            }
        }

        user = userRepository.save(user);
        return UserVO.from(user);
    }

    public void delete(UUID id) {
        if (!userRepository.existsById(id)) {
            throw new BusinessException(404, "User not found");
        }
        userRepository.deleteById(id);
    }
}
