package com.maas.provider.adapter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.maas.provider.entity.Provider;
import com.maas.provider.entity.ProviderType;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

@Component
public class AnthropicAdapter implements ProviderAdapter {
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean supports(Provider provider) {
        return provider.getType() == ProviderType.anthropic;
    }

    @Override
    public boolean checkHealth(Provider provider) {
        try {
            String baseUrl = parseBaseUrl(provider).replaceAll("/+$", "");
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/"))
                .timeout(java.time.Duration.ofSeconds(10))
                .GET()
                .build();
            httpClient.send(request, HttpResponse.BodyHandlers.discarding());
            return true; // server responded => reachable
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public List<String> fetchModels(Provider provider) {
        try {
            String baseUrl = parseBaseUrl(provider).replaceAll("/+$", "");
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/v1/models"))
                .timeout(java.time.Duration.ofSeconds(10))
                .GET();

            try {
                JsonNode config = objectMapper.readTree(provider.getConfigJson());
                JsonNode apiKey = config.get("api_key");
                if (apiKey != null && !apiKey.asText().isEmpty()) {
                    builder.header("x-api-key", apiKey.asText());
                    builder.header("anthropic-version", "2023-06-01");
                }
            } catch (Exception ignored) {
            }

            HttpResponse<String> response = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) return List.of();

            JsonNode root = objectMapper.readTree(response.body());
            List<String> models = new ArrayList<>();
            JsonNode data = root.get("data");
            if (data != null && data.isArray()) {
                for (JsonNode node : data) {
                    JsonNode id = node.get("id");
                    if (id != null) models.add(id.asText());
                }
            }
            return models;
        } catch (Exception e) {
            return List.of();
        }
    }

    private String parseBaseUrl(Provider provider) {
        try {
            JsonNode config = objectMapper.readTree(provider.getConfigJson());
            JsonNode url = config.get("base_url");
            if (url == null || url.asText().isEmpty()) {
                throw new IllegalArgumentException("base_url is missing or empty in provider config");
            }
            return url.asText().replaceAll("/$", "");
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to parse provider config JSON", e);
        }
    }
}
