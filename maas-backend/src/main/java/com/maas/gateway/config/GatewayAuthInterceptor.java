package com.maas.gateway.config;

import com.maas.apikey.dto.KeyVO;
import com.maas.apikey.service.ApiKeyService;
import com.maas.common.exception.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class GatewayAuthInterceptor implements HandlerInterceptor {
    private final ApiKeyService apiKeyService;

    public GatewayAuthInterceptor(ApiKeyService apiKeyService) {
        this.apiKeyService = apiKeyService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new BusinessException(401, "Missing or invalid Authorization header");
        }
        String token = authHeader.substring(7);
        KeyVO key = apiKeyService.authenticate(token);
        request.setAttribute("apiKey", key);
        return true;
    }
}
