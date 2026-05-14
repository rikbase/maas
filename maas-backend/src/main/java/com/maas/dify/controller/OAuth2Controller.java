package com.maas.dify.controller;

import com.maas.common.dto.ApiResponse;
import com.maas.dify.dto.oauth2.AuthorizeRequest;
import com.maas.dify.dto.oauth2.AuthorizeResponse;
import com.maas.dify.dto.oauth2.TokenRequest;
import com.maas.dify.dto.oauth2.TokenResponse;
import com.maas.dify.service.OAuth2Service;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/oauth2")
public class OAuth2Controller {

    private final OAuth2Service oAuth2Service;

    public OAuth2Controller(OAuth2Service oAuth2Service) {
        this.oAuth2Service = oAuth2Service;
    }

    @PostMapping("/authorize")
    public ApiResponse<AuthorizeResponse> authorize(@Valid @RequestBody AuthorizeRequest req,
                                                    Authentication auth) {
        String userId = (String) auth.getDetails();
        return ApiResponse.success(oAuth2Service.authorize(req, userId));
    }

    @GetMapping("/callback")
    public ResponseEntity<String> callback(@RequestParam String code, @RequestParam String state) {
        String html = oAuth2Service.handleCallback(code, state);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_HTML);
        return ResponseEntity.ok().headers(headers).body(html);
    }

    @PostMapping("/token")
    public ApiResponse<TokenResponse> token(@Valid @RequestBody TokenRequest req) {
        return ApiResponse.success(oAuth2Service.token(req));
    }
}
