package com.maas.dify.service;

import com.maas.dify.dto.oauth2.AuthorizeRequest;
import com.maas.dify.dto.oauth2.AuthorizeResponse;
import com.maas.dify.dto.oauth2.TokenRequest;
import com.maas.dify.dto.oauth2.TokenResponse;
import com.maas.dify.entity.OAuthClient;
import com.maas.dify.repository.OAuthClientRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service
public class OAuth2Service {

    private final OAuthClientRepository oAuthClientRepository;
    private final AuthorizationCodeStore codeStore;
    private final DifyService difyService;
    private final PasswordEncoder passwordEncoder;

    public OAuth2Service(OAuthClientRepository oAuthClientRepository,
                         AuthorizationCodeStore codeStore,
                         DifyService difyService,
                         PasswordEncoder passwordEncoder) {
        this.oAuthClientRepository = oAuthClientRepository;
        this.codeStore = codeStore;
        this.difyService = difyService;
        this.passwordEncoder = passwordEncoder;
    }

    public AuthorizeResponse authorize(AuthorizeRequest req, String userId) {
        OAuthClient client = oAuthClientRepository.findByClientId(req.clientId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid client_id: " + req.clientId()));

        if (!"active".equals(client.getStatus())) {
            throw new IllegalStateException("OAuth client is not active: " + req.clientId());
        }

        String redirectUri = req.redirectUri();
        // Only validate the path — the callback always goes to our own backend,
        // and the origin differs between dev (localhost:5173) and prod (localhost:8080)
        if (redirectUri == null || !redirectUri.contains("/api/oauth2/callback")) {
            throw new IllegalArgumentException("redirect_uri not allowed: " + redirectUri);
        }

        String code = codeStore.generate(req.clientId(), userId, req.state());
        return new AuthorizeResponse(code, req.state(), redirectUri);
    }

    public String handleCallback(String code, String state) {
        AuthorizationCodeStore.CodeEntry entry = codeStore.consume(code);
        if (entry == null) {
            return difyService.ssoErrorPage("Invalid or expired authorization code");
        }

        // The state carries the dify config UUID — generate SSO HTML for it
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
}
