package com.maas.dify.controller;

import com.maas.common.dto.ApiResponse;
import com.maas.dify.dto.oauth2.AuthorizeRequest;
import com.maas.dify.dto.oauth2.AuthorizeResponse;
import com.maas.dify.dto.oauth2.TokenRequest;
import com.maas.dify.dto.oauth2.TokenResponse;
import com.maas.dify.service.DifyService;
import com.maas.dify.service.OAuth2Service;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/oauth2")
public class OAuth2Controller {

    private final OAuth2Service oAuth2Service;
    private final DifyService difyService;

    public OAuth2Controller(OAuth2Service oAuth2Service, DifyService difyService) {
        this.oAuth2Service = oAuth2Service;
        this.difyService = difyService;
    }

    @PostMapping("/authorize")
    public ApiResponse<AuthorizeResponse> authorize(@Valid @RequestBody AuthorizeRequest req,
                                                    Authentication auth) {
        String userId = (String) auth.getDetails();
        return ApiResponse.success(oAuth2Service.authorize(req, userId));
    }

    @GetMapping("/authorize")
    public ResponseEntity<Void> authorizeGet(
            @RequestParam String client_id,
            @RequestParam(required = false) String redirect_uri,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String response_type,
            HttpServletRequest request) {

        // Try to extract JWT from cookie or Authorization header
        String jwt = null;
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie c : cookies) {
                if ("maas_token".equals(c.getName())) {
                    jwt = c.getValue();
                    break;
                }
            }
        }
        if (jwt == null) {
            String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                jwt = authHeader.substring(7);
            }
        }

        if (jwt == null) {
            // Not authenticated — redirect to MaaS login
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create("/login?redirect=" + request.getRequestURI() + "?" + request.getQueryString()))
                    .build();
        }

        try {
            String userId = oAuth2Service.validateJwtAndGetUserId(jwt);
            String targetRedirect = redirect_uri != null ? redirect_uri : oAuth2Service.getDefaultRedirectUri(client_id);
            String authCode = oAuth2Service.generateCode(client_id, userId, state != null ? state : "");

            String location = targetRedirect + "?code=" + authCode + "&state=" + (state != null ? state : "");
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create(location))
                    .build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create("/login?error=invalid_token"))
                    .build();
        }
    }

    @GetMapping("/callback")
    public void callback(@RequestParam String code, @RequestParam String state,
                         HttpServletResponse response) throws Exception {
        try {
            UUID configId = oAuth2Service.consumeCode(code, state);
            DifyService.SsoLoginResult login = difyService.ssoLogin(configId);

            // Forward Dify session cookies to the browser.
            // Domain=localhost so cookies work across all localhost ports
            // (Dify API:5001, Dify web:3000, MaaS:8080)
            for (Map.Entry<String, String> cookie : login.cookies().entrySet()) {
                jakarta.servlet.http.Cookie c = new jakarta.servlet.http.Cookie(cookie.getKey(), cookie.getValue());
                c.setPath("/");
                c.setDomain("localhost");
                // Dify: access_token & refresh_token are HttpOnly, csrf_token is not
                c.setHttpOnly(!"csrf_token".equals(cookie.getKey()));
                response.addCookie(c);
            }

            response.sendRedirect(login.webUrl() + "/apps");
        } catch (Exception e) {
            response.setContentType(MediaType.TEXT_HTML_VALUE);
            response.getWriter().write(difyService.ssoErrorPage(e.getMessage()));
        }
    }

    @PostMapping("/token")
    public ApiResponse<TokenResponse> token(@Valid @RequestBody TokenRequest req) {
        return ApiResponse.success(oAuth2Service.token(req));
    }

    @GetMapping("/userinfo")
    public ResponseEntity<?> userinfo(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "invalid_token", "error_description", "No access token provided"));
        }
        String accessToken = authHeader.substring(7);
        try {
            Map<String, Object> userInfo = oAuth2Service.getUserInfo(accessToken);
            return ResponseEntity.ok(userInfo);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "invalid_token", "error_description", e.getMessage()));
        }
    }
}
