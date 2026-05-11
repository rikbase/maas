package com.maas.user.service;

import com.maas.common.exception.BusinessException;
import com.maas.user.dto.LoginRequest;
import com.maas.user.dto.LoginResponse;
import com.maas.user.dto.UserVO;
import com.maas.user.entity.User;
import com.maas.user.entity.UserStatus;
import com.maas.user.repository.UserRepository;
import com.maas.user.util.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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
        UserVO userVO = new UserVO(user.getId(), user.getUsername(), user.getDisplayName(), user.getRole());
        return new LoginResponse(token, userVO);
    }

    public UserVO getCurrentUser(UUID userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new BusinessException(404, "User not found"));
        return new UserVO(user.getId(), user.getUsername(), user.getDisplayName(), user.getRole());
    }
}
