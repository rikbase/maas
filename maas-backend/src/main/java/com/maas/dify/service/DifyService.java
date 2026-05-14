package com.maas.dify.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.maas.common.util.EncryptionUtil;
import com.maas.dify.dto.*;
import com.maas.dify.entity.DifyConfig;
import com.maas.dify.repository.DifyConfigRepository;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
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
        c.setApiKeyEncrypted(encryptionUtil.encrypt(req.apiKey()));
        c.setAdminEmail(req.adminEmail());
        c.setAdminPasswordEncrypted(encryptionUtil.encrypt(req.adminPassword()));
        c.setStatus("disconnected");
        return DifyConfigVO.from(repository.save(c));
    }

    public DifyConfigVO update(UUID id, DifyConfigUpdateRequest req) {
        DifyConfig c = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Dify config not found: " + id));
        c.setName(req.name());
        c.setBaseUrl(req.baseUrl().replaceAll("/$", ""));
        c.setAdminEmail(req.adminEmail());
        if (req.apiKey() != null && !req.apiKey().isBlank()) {
            c.setApiKeyEncrypted(encryptionUtil.encrypt(req.apiKey()));
        }
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

        String baseUrl = c.getBaseUrl();

        // If admin credentials are configured, test via console login
        if (c.getAdminEmail() != null && c.getAdminPasswordEncrypted() != null) {
            return loginToDify(id);
        }

        // Fallback: check server reachability
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl.replaceAll("/$", "") + "/health"))
                    .timeout(Duration.ofSeconds(10))
                    .GET()
                    .build();

            HttpResponse<String> res = httpClient.send(req, HttpResponse.BodyHandlers.ofString());

            c.setStatus(res.statusCode() < 500 ? "connected" : "error");
            c.setLastTestAt(Instant.now());
            repository.save(c);
            return new DifyTestResult(res.statusCode() < 500,
                    "HTTP " + res.statusCode(), null);
        } catch (Exception e) {
            c.setStatus("error");
            c.setLastTestAt(Instant.now());
            repository.save(c);
            String errMsg = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
            return new DifyTestResult(false, errMsg, null);
        }
    }

    public DifyTestResult loginToDify(UUID id) {
        DifyConfig c = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Dify config not found: " + id));

        String email = c.getAdminEmail();
        if (email == null) {
            return new DifyTestResult(false, "Admin email not configured", null);
        }

        String password = encryptionUtil.decrypt(c.getAdminPasswordEncrypted());
        String base64Password = Base64.getEncoder().encodeToString(password.getBytes());

        try {
            String json = objectMapper.writeValueAsString(
                    java.util.Map.of("email", email, "password", base64Password)
            );

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(c.getBaseUrl() + "/console/api/login"))
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(15))
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> res = httpClient.send(req, HttpResponse.BodyHandlers.ofString());

            if (res.statusCode() == 200) {
                JsonNode body = objectMapper.readTree(res.body());
                boolean success = body.has("result") && "success".equals(body.get("result").asText());
                c.setStatus(success ? "connected" : "error");
                c.setLastTestAt(Instant.now());
                repository.save(c);
                return new DifyTestResult(success, success ? "Login successful" : "Login failed: " + res.body(), null);
            } else {
                c.setStatus("error");
                c.setLastTestAt(Instant.now());
                repository.save(c);
                return new DifyTestResult(false, "HTTP " + res.statusCode() + ": " + res.body(), null);
            }
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

        String email = c.getAdminEmail();
        if (email == null) {
            return ssoErrorPage("Admin email not configured");
        }

        String password;
        try {
            password = encryptionUtil.decrypt(c.getAdminPasswordEncrypted());
        } catch (Exception e) {
            return ssoErrorPage("Failed to decrypt admin credentials");
        }

        String base64Password = Base64.getEncoder().encodeToString(password.getBytes());
        String loginUrl = c.getBaseUrl() + "/console/api/login";
        String webUrl = deriveWebUrl(c.getBaseUrl());

        return """
<!DOCTYPE html>
<html>
<head>
<meta charset="utf-8">
<title>Redirecting to Dify Console...</title>
<style>
  body { font-family: -apple-system, BlinkMacSystemFont, sans-serif; display: flex;
         justify-content: center; align-items: center; height: 100vh; margin: 0;
         background: #f5f5f5; color: #333; }
  .msg { text-align: center; }
  .spinner { border: 3px solid #e0e0e0; border-top: 3px solid #1976d2;
             border-radius: 50%%; width: 32px; height: 32px;
             animation: spin 0.8s linear infinite; margin: 0 auto 16px; }
  @keyframes spin { to { transform: rotate(360deg); } }
  .error { color: #c62828; font-size: 14px; margin-top: 12px; display: none; }
</style>
</head>
<body>
  <div class="msg">
    <div class="spinner"></div>
    <p>Redirecting to Dify Console...</p>
    <div id="error" class="error"></div>
  </div>
  <script>
    fetch('%s', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      credentials: 'include',
      body: JSON.stringify({ email: '%s', password: '%s' })
    }).then(function(r) {
      if (r.ok) {
        window.location.href = '%s';
      } else {
        return r.text().then(function(t) {
          document.querySelector('.spinner').style.display = 'none';
          document.getElementById('error').style.display = 'block';
          document.getElementById('error').textContent = 'Login failed: ' + r.status;
        });
      }
    }).catch(function(e) {
      document.querySelector('.spinner').style.display = 'none';
      document.getElementById('error').style.display = 'block';
      document.getElementById('error').textContent = 'Connection error';
    });
  </script>
</body>
</html>""".formatted(loginUrl, escapeHtmlJs(email), escapeHtmlJs(base64Password), webUrl);
    }

    private String deriveWebUrl(String apiBaseUrl) {
        // Dify API is on port 5001, Web UI is on port 3000
        return apiBaseUrl.replace(":5001", ":3000").replaceAll("/$", "") + "/signin";
    }

    private String escapeHtml(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&#39;");
    }

    private String escapeHtmlJs(String s) {
        return escapeHtml(s).replace("\\", "\\\\").replace("'", "\\'").replace("\"", "\\\"");
    }

    public String ssoErrorPage(String message) {
        return """
<!DOCTYPE html>
<html>
<head><meta charset="utf-8"><title>SSO Error</title></head>
<body style="font-family:sans-serif;padding:40px;text-align:center">
  <h2>SSO Error</h2>
  <p>%s</p>
  <p>Please configure admin credentials in Maas Dify settings.</p>
</body>
</html>""".formatted(escapeHtml(message));
    }
}
