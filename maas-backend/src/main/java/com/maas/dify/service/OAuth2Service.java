package com.maas.dify.service;

import com.maas.dify.dto.oauth2.AuthorizeRequest;
import com.maas.dify.dto.oauth2.AuthorizeResponse;
import com.maas.dify.dto.oauth2.TokenRequest;
import com.maas.dify.dto.oauth2.TokenResponse;
import com.maas.dify.entity.OAuthClient;
import com.maas.dify.repository.OAuthClientRepository;
import com.maas.user.entity.User;
import com.maas.user.repository.UserRepository;
import com.maas.user.util.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;


@Service
public class OAuth2Service {

    private final OAuthClientRepository oAuthClientRepository;
    private final AuthorizationCodeStore codeStore;
    private final DifyService difyService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    public OAuth2Service(OAuthClientRepository oAuthClientRepository,
                         AuthorizationCodeStore codeStore,
                         DifyService difyService,
                         PasswordEncoder passwordEncoder,
                         JwtUtil jwtUtil,
                         UserRepository userRepository) {
        this.oAuthClientRepository = oAuthClientRepository;
        this.codeStore = codeStore;
        this.difyService = difyService;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }

    public AuthorizeResponse authorize(AuthorizeRequest req, String userId) {
        OAuthClient client = oAuthClientRepository.findByClientId(req.clientId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid client_id: " + req.clientId()));

        if (!"active".equals(client.getStatus())) {
            throw new IllegalStateException("OAuth client is not active: " + req.clientId());
        }

        String redirectUri = req.redirectUri();
        if (redirectUri == null || !isValidRedirectUri(redirectUri)) {
            throw new IllegalArgumentException("redirect_uri not allowed: " + redirectUri);
        }

        String code = codeStore.generate(req.clientId(), userId, req.state());
        return new AuthorizeResponse(code, req.state(), redirectUri);
    }

    public String validateJwtAndGetUserId(String jwt) {
        if (!jwtUtil.validateToken(jwt)) {
            throw new IllegalArgumentException("Invalid or expired JWT");
        }
        return jwtUtil.extractUserId(jwt);
    }

    public String getDefaultRedirectUri(String clientId) {
        OAuthClient client = oAuthClientRepository.findByClientId(clientId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid client_id: " + clientId));
        // Use the first redirect URI from the stored URIs
        String uris = client.getRedirectUris();
        if (uris != null && !uris.isBlank()) {
            return uris.split(",")[0].trim();
        }
        return "/";
    }

    public String generateCode(String clientId, String userId, String state) {
        return codeStore.generate(clientId, userId, state);
    }

    /**
     * Validates and consumes an authorization code.
     * @return the Dify config UUID from the state parameter
     * @throws IllegalArgumentException if the code is invalid
     */
    public java.util.UUID consumeCode(String code, String state) {
        AuthorizationCodeStore.CodeEntry entry = codeStore.consume(code);
        if (entry == null) {
            throw new IllegalArgumentException("Invalid or expired authorization code");
        }
        return java.util.UUID.fromString(state);
    }

    @Deprecated
    public String handleCallback(String code, String state) {
        AuthorizationCodeStore.CodeEntry entry = codeStore.consume(code);
        if (entry == null) {
            return difyService.ssoErrorPage("Invalid or expired authorization code");
        }
        return difyService.generateSsoHtml(java.util.UUID.fromString(state));
    }

    public TokenResponse token(TokenRequest req) {
        OAuthClient client = oAuthClientRepository.findByClientId(req.clientId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid client_id"));

        if (!passwordEncoder.matches(req.clientSecret(), client.getClientSecret())) {
            throw new IllegalArgumentException("Invalid client_secret");
        }

        AuthorizationCodeStore.CodeEntry entry = codeStore.consume(req.code());
        if (entry == null) {
            throw new IllegalArgumentException("Invalid or expired authorization code");
        }

        long expiresIn = 3600L;
        return new TokenResponse(
                "stub-access-token-" + entry.userId(),
                "Bearer",
                expiresIn,
                client.getScopes()
        );
    }

    public Map<String, Object> getUserInfo(String accessToken) {
        if (accessToken == null || !accessToken.startsWith("stub-access-token-")) {
            throw new IllegalArgumentException("Invalid access token format");
        }

        String userId = accessToken.substring("stub-access-token-".length());
        User user = userRepository.findById(UUID.fromString(userId))
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return Map.of(
                "sub", user.getId().toString(),
                "name", user.getDisplayName(),
                "preferred_username", user.getUsername(),
                "email", user.getUsername() + "@maas.local",
                "role", user.getRole().name()
        );
    }

    private boolean isValidRedirectUri(String redirectUri) {
        return redirectUri.contains("/api/oauth2/callback")
                || redirectUri.contains("/console/api/oauth/callback");
    }
}
