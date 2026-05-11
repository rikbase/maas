package com.maas.gateway.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.maas.gateway.dto.ChatCompletionRequest;
import com.maas.gateway.dto.ChatCompletionResponse;
import com.maas.provider.entity.Provider;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Component
public class ProviderInvoker {
    private final HttpClient httpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(30))
        .build();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ChatCompletionResponse invoke(Provider provider, ChatCompletionRequest request) {
        try {
            String providerUrl = getProviderUrl(provider);
            String apiKey = getApiKey(provider);

            String body = objectMapper.writeValueAsString(request);

            HttpRequest httpReq = HttpRequest.newBuilder()
                .uri(URI.create(providerUrl + "/v1/chat/completions"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .timeout(Duration.ofSeconds(60))
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

            HttpResponse<String> httpRes = httpClient.send(httpReq, HttpResponse.BodyHandlers.ofString());

            if (httpRes.statusCode() != 200) {
                throw new RuntimeException("Provider returned " + httpRes.statusCode() + ": " + httpRes.body());
            }

            return objectMapper.readValue(httpRes.body(), ChatCompletionResponse.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to invoke provider: " + e.getMessage(), e);
        }
    }

    private String getProviderUrl(Provider provider) {
        try {
            var config = objectMapper.readTree(provider.getConfigJson());
            var url = config.get("base_url");
            if (url == null) throw new IllegalArgumentException("base_url is required");
            return url.asText().replaceAll("/+$", "");
        } catch (Exception e) {
            throw new RuntimeException("Invalid provider config: " + e.getMessage());
        }
    }

    private String getApiKey(Provider provider) {
        try {
            var config = objectMapper.readTree(provider.getConfigJson());
            var key = config.get("api_key");
            return key != null ? key.asText() : "";
        } catch (Exception e) {
            return "";
        }
    }
}
