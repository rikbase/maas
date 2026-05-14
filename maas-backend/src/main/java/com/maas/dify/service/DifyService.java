package com.maas.dify.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.maas.common.util.EncryptionUtil;
import com.maas.dify.dto.*;
import com.maas.dify.entity.DifyConfig;
import com.maas.dify.repository.DifyConfigRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional
public class DifyService {

    private final DifyConfigRepository repository;
    private final EncryptionUtil encryptionUtil;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public DifyService(DifyConfigRepository repository, EncryptionUtil encryptionUtil) {
        this.repository = repository;
        this.encryptionUtil = encryptionUtil;
    }

    @Transactional(readOnly = true)
    public List<DifyConfigVO> list() {
        return repository.findAll().stream()
                .map(DifyConfigVO::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public DifyConfigVO get(UUID id) {
        DifyConfig c = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Dify config not found: " + id));
        return DifyConfigVO.from(c);
    }

    public DifyConfigVO create(DifyConfigCreateRequest req) {
        DifyConfig c = new DifyConfig();
        c.setName(req.name());
        c.setBaseUrl(req.baseUrl().replaceAll("/$", ""));
        c.setApiKeyEncrypted(encryptionUtil.encrypt(req.authCode()));
        c.setStatus("disconnected");
        if (req.adminEmail() != null) c.setAdminEmail(req.adminEmail());
        if (req.adminPassword() != null && !req.adminPassword().isBlank()) {
            c.setAdminPasswordEncrypted(encryptionUtil.encrypt(req.adminPassword()));
        }
        return DifyConfigVO.from(repository.save(c));
    }

    public DifyConfigVO update(UUID id, DifyConfigUpdateRequest req) {
        DifyConfig c = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Dify config not found: " + id));
        c.setName(req.name());
        c.setBaseUrl(req.baseUrl().replaceAll("/$", ""));
        if (req.authCode() != null && !req.authCode().isBlank()) {
            c.setApiKeyEncrypted(encryptionUtil.encrypt(req.authCode()));
        }
        if (req.adminEmail() != null) c.setAdminEmail(req.adminEmail());
        if (req.adminPassword() != null && !req.adminPassword().isBlank()) {
            c.setAdminPasswordEncrypted(encryptionUtil.encrypt(req.adminPassword()));
        }
        c.setUpdatedAt(Instant.now());
        return DifyConfigVO.from(repository.save(c));
    }

    public void delete(UUID id) {
        repository.deleteById(id);
    }

    public DifyTestResult testConnection(UUID id) {
        DifyConfig c = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Dify config not found: " + id));

        String baseUrl = c.getBaseUrl().replaceAll("/$", "");

        // Prefer admin credentials for testing (same auth method as SSO / Open Console)
        if (c.getAdminEmail() != null && c.getAdminPasswordEncrypted() != null) {
            return testWithAdminLogin(c, baseUrl);
        }

        // Fallback: test with API key (authCode)
        return testWithApiKey(c, baseUrl);
    }

    private DifyTestResult testWithAdminLogin(DifyConfig c, String baseUrl) {
        try {
            String password = encryptionUtil.decrypt(c.getAdminPasswordEncrypted());
            String email = c.getAdminEmail();
            String base64Password = java.util.Base64.getEncoder().encodeToString(password.getBytes());

            String requestBody = objectMapper.writeValueAsString(Map.of(
                    "email", email,
                    "password", base64Password,
                    "language", "en-US",
                    "remember_me", true
            ));

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/console/api/login"))
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(10))
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> res = httpClient.send(req, HttpResponse.BodyHandlers.ofString());

            boolean connected = res.statusCode() == 200;
            c.setStatus(connected ? "connected" : "error");
            c.setLastTestAt(Instant.now());
            repository.save(c);

            if (connected) {
                return new DifyTestResult(true, "Connection successful. Admin: " + email, "admin login");
            } else {
                String msg = "Login failed: HTTP " + res.statusCode();
                try {
                    JsonNode body = objectMapper.readTree(res.body());
                    if (body.has("message")) msg = body.get("message").asText();
                } catch (Exception ignored) {}
                return new DifyTestResult(false, msg, null);
            }
        } catch (Exception e) {
            c.setStatus("error");
            c.setLastTestAt(Instant.now());
            repository.save(c);
            String errMsg = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
            return new DifyTestResult(false, errMsg, null);
        }
    }

    private DifyTestResult testWithApiKey(DifyConfig c, String baseUrl) {
        String authCode;
        try {
            authCode = encryptionUtil.decrypt(c.getApiKeyEncrypted());
        } catch (Exception e) {
            return new DifyTestResult(false, "Failed to decrypt auth code", null);
        }

        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/v1/datasets?page=1&limit=1"))
                    .header("Authorization", "Bearer " + authCode)
                    .timeout(Duration.ofSeconds(10))
                    .GET()
                    .build();

            HttpResponse<String> res = httpClient.send(req, HttpResponse.BodyHandlers.ofString());

            boolean connected = res.statusCode() == 200;
            c.setStatus(connected ? "connected" : "error");
            c.setLastTestAt(Instant.now());
            repository.save(c);

            String msg;
            String appName = null;
            if (connected) {
                msg = "Connection successful";
                try {
                    JsonNode body = objectMapper.readTree(res.body());
                    appName = body.has("data") ? "datasets accessible" : null;
                } catch (Exception ignored) {}
            } else {
                msg = "Auth failed: HTTP " + res.statusCode();
                try {
                    JsonNode body = objectMapper.readTree(res.body());
                    if (body.has("message")) msg = body.get("message").asText();
                } catch (Exception ignored) {}
            }
            return new DifyTestResult(connected, msg, appName);
        } catch (Exception e) {
            c.setStatus("error");
            c.setLastTestAt(Instant.now());
            repository.save(c);
            String errMsg = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
            return new DifyTestResult(false, errMsg, null);
        }
    }

    @Transactional(readOnly = true)
    public String generateSsoHtml(UUID id) {
        DifyConfig c = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Dify config not found: " + id));

        String webUrl = deriveWebUrl(c.getBaseUrl());
        return """
<!DOCTYPE html>
<html>
<head>
<meta charset="utf-8">
<title>Redirecting to Dify...</title>
<style>
  body { font-family: -apple-system, BlinkMacSystemFont, sans-serif; display: flex;
         justify-content: center; align-items: center; height: 100vh; margin: 0;
         background: #f5f5f5; color: #333; }
  .msg { text-align: center; }
  .spinner { border: 3px solid #e0e0e0; border-top: 3px solid #1976d2;
             border-radius: 50%%; width: 32px; height: 32px;
             animation: spin 0.8s linear infinite; margin: 0 auto 16px; }
  @keyframes spin { to { transform: rotate(360deg); } }
</style>
</head>
<body>
  <div class="msg">
    <div class="spinner"></div>
    <p>Redirecting to Dify Console...</p>
  </div>
  <script>window.location.href = '%s';</script>
</body>
</html>""".formatted(webUrl);
    }

    private String deriveWebUrl(String apiBaseUrl) {
        return apiBaseUrl.replace(":5001", ":3000").replaceAll("/$", "");
    }

    public String ssoErrorPage(String message) {
        return """
    <!DOCTYPE html>
    <html>
    <head><meta charset="utf-8"><title>Error</title></head>
    <body style="font-family:sans-serif;display:flex;justify-content:center;align-items:center;height:100vh;margin:0;background:#f5f5f5;color:#333;">
      <div style="text-align:center">
        <h2 style="color:#ef4444;margin-bottom:8px;">Authentication Error</h2>
        <p style="color:#666;">%s</p>
      </div>
    </body>
    </html>""".formatted(escapeHtml(message));
    }

    private String escapeHtml(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&#39;");
    }

    public record SsoLoginResult(String webUrl, Map<String, String> cookies) {}

    /**
     * Log into Dify console using admin credentials and return session cookies.
     * The cookies can be forwarded to the user's browser via Set-Cookie headers.
     */
    public SsoLoginResult ssoLogin(UUID configId) {
        DifyConfig config = repository.findById(configId)
                .orElseThrow(() -> new RuntimeException("Dify config not found: " + configId));

        String email = config.getAdminEmail();
        String encryptedPassword = config.getAdminPasswordEncrypted();

        if (email == null || encryptedPassword == null) {
            throw new IllegalStateException(
                    "Dify admin credentials not configured. Please set admin email and password in Dify config.");
        }

        String password = encryptionUtil.decrypt(encryptedPassword);
        String webUrl = deriveWebUrl(config.getBaseUrl());
        String apiUrl = config.getBaseUrl().replaceAll("/$", "") + "/console/api/login";

        try {
            // Dify's login endpoint expects the password to be Base64-encoded
            String base64Password = java.util.Base64.getEncoder().encodeToString(password.getBytes());
            String requestBody = objectMapper.writeValueAsString(Map.of(
                    "email", email,
                    "password", base64Password,
                    "language", "en-US",
                    "remember_me", true
            ));

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(10))
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> res = httpClient.send(req, HttpResponse.BodyHandlers.ofString());

            if (res.statusCode() != 200) {
                String errMsg = "Dify login failed: HTTP " + res.statusCode();
                try {
                    var body = objectMapper.readTree(res.body());
                    if (body.has("message")) errMsg = body.get("message").asText();
                } catch (Exception ignored) {}
                throw new RuntimeException(errMsg);
            }

            // Extract Set-Cookie headers
            Map<String, String> cookies = new HashMap<>();
            res.headers().map().forEach((headerName, values) -> {
                if (headerName.equalsIgnoreCase("Set-Cookie")) {
                    for (String cookieHeader : values) {
                        String[] parts = cookieHeader.split(";", 2);
                        if (parts.length > 0) {
                            String[] kv = parts[0].split("=", 2);
                            if (kv.length == 2) {
                                cookies.put(kv[0].trim(), kv[1].trim());
                            }
                        }
                    }
                }
            });

            return new SsoLoginResult(webUrl, cookies);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to login to Dify: " + e.getMessage(), e);
        }
    }
}
