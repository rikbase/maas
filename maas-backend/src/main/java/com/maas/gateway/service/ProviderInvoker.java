package com.maas.gateway.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.maas.gateway.dto.ChatCompletionChunk;
import com.maas.gateway.dto.ChatCompletionRequest;
import com.maas.gateway.dto.ChatCompletionResponse;
import com.maas.monitor.service.UsageService;
import com.maas.provider.entity.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

@Component
public class ProviderInvoker {
    private static final Logger log = LoggerFactory.getLogger(ProviderInvoker.class);
    private static final BigDecimal COST_PER_TOKEN = new BigDecimal("0.000002");

    private final HttpClient httpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(30))
        .build();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final UsageService usageService;

    public ProviderInvoker(UsageService usageService) {
        this.usageService = usageService;
    }

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

    public void invokeStream(Provider provider, ChatCompletionRequest request,
                             String apiKeyId, UUID providerId, SseEmitter emitter) {
        new Thread(() -> {
            try {
                String providerUrl = getProviderUrl(provider);
                String providerKey = getApiKey(provider);

                // Build modified request with stream: true
                var node = (ObjectNode) objectMapper.valueToTree(request);
                node.put("stream", true);
                String body = objectMapper.writeValueAsString(node);

                HttpRequest httpReq = HttpRequest.newBuilder()
                    .uri(URI.create(providerUrl + "/v1/chat/completions"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + providerKey)
                    .timeout(Duration.ofSeconds(120))
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

                HttpResponse<java.io.InputStream> httpRes = httpClient.send(httpReq,
                    HttpResponse.BodyHandlers.ofInputStream());

                if (httpRes.statusCode() != 200) {
                    String errorBody = new String(httpRes.body().readAllBytes());
                    emitter.completeWithError(
                        new RuntimeException("Provider returned " + httpRes.statusCode() + ": " + errorBody));
                    return;
                }

                // Track usage from streaming chunks
                int promptTokens = 0;
                int completionTokens = 0;
                int totalTokens = 0;
                long startTime = System.currentTimeMillis();

                AtomicReference<String> modelName = new AtomicReference<>(request.model());
                AtomicReference<String> chunkId = new AtomicReference<>("chatcmpl-" + java.util.UUID.randomUUID());

                try (BufferedReader reader = new BufferedReader(new InputStreamReader(httpRes.body()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (line.startsWith("data: ")) {
                            String data = line.substring(6).trim();

                            // End of stream
                            if ("[DONE]".equals(data)) {
                                break;
                            }

                            try {
                                var chunkNode = objectMapper.readTree(data);
                                if (chunkNode.has("id") && !chunkNode.get("id").isNull()) {
                                    chunkId.set(chunkNode.get("id").asText());
                                }
                                if (chunkNode.has("model") && !chunkNode.get("model").isNull()) {
                                    modelName.set(chunkNode.get("model").asText());
                                }

                                // Try to capture usage from final chunk
                                if (chunkNode.has("usage") && !chunkNode.get("usage").isNull()) {
                                    var usage = chunkNode.get("usage");
                                    promptTokens = usage.has("prompt_tokens") ? usage.get("prompt_tokens").asInt() : 0;
                                    completionTokens = usage.has("completion_tokens") ? usage.get("completion_tokens").asInt() : 0;
                                    totalTokens = usage.has("total_tokens") ? usage.get("total_tokens").asInt() : 0;
                                }

                                // Forward chunk to client
                                emitter.send(SseEmitter.event()
                                    .name("message")
                                    .data(data));

                            } catch (Exception e) {
                                log.warn("Failed to parse streaming chunk: {}", e.getMessage());
                            }
                        }
                    }
                }

                long latencyMs = System.currentTimeMillis() - startTime;

                // Record usage
                if (promptTokens > 0 || completionTokens > 0) {
                    BigDecimal cost = BigDecimal.valueOf(promptTokens + completionTokens)
                        .multiply(COST_PER_TOKEN);
                    usageService.record(
                        java.util.UUID.fromString(apiKeyId),
                        providerId,
                        modelName.get(),
                        promptTokens,
                        completionTokens,
                        (int) latencyMs,
                        cost
                    );
                }

                emitter.complete();
            } catch (Exception e) {
                log.error("Streaming error: {}", e.getMessage());
                emitter.completeWithError(e);
            }
        }).start();
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
