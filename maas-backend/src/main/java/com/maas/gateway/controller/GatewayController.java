package com.maas.gateway.controller;

import com.maas.apikey.dto.KeyVO;
import com.maas.gateway.dto.ChatCompletionRequest;
import com.maas.gateway.dto.ChatCompletionResponse;
import com.maas.gateway.service.ModelRouter;
import com.maas.gateway.service.ProviderInvoker;
import com.maas.gateway.service.RateLimiter;
import com.maas.provider.entity.Provider;
import com.maas.provider.entity.ProviderModel;
import com.maas.provider.repository.ProviderModelRepository;
import com.maas.security.detector.SecurityInspector;
import com.maas.security.dto.InspectionResult;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
public class GatewayController {
    private final ModelRouter modelRouter;
    private final ProviderInvoker providerInvoker;
    private final RateLimiter rateLimiter;
    private final SecurityInspector securityInspector;
    private final ProviderModelRepository providerModelRepository;

    public GatewayController(ModelRouter modelRouter, ProviderInvoker providerInvoker,
                             RateLimiter rateLimiter, SecurityInspector securityInspector,
                             ProviderModelRepository providerModelRepository) {
        this.modelRouter = modelRouter;
        this.providerInvoker = providerInvoker;
        this.rateLimiter = rateLimiter;
        this.securityInspector = securityInspector;
        this.providerModelRepository = providerModelRepository;
    }

    @PostMapping("/v1/chat/completions")
    public Object chatCompletion(
            @Valid @RequestBody ChatCompletionRequest request,
            @RequestAttribute KeyVO apiKey) {
        rateLimiter.check(apiKey.id().toString(), 60);

        String content = request.messages() != null && !request.messages().isEmpty()
            ? request.messages().get(request.messages().size() - 1).content()
            : "";

        InspectionResult result = securityInspector.inspectRequest(
            content, "all", apiKey.id(),
            null, request.model()
        );

        if (!result.passed() && result.action() == com.maas.security.entity.RuleAction.block) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                "Request blocked by security policy: " + String.join(", ", result.matchedRuleNames()));
        }

        String preferredProvider = null;
        Provider provider = modelRouter.route(request.model(), preferredProvider);

        if (Boolean.TRUE.equals(request.stream())) {
            SseEmitter emitter = new SseEmitter(180_000L);
            providerInvoker.invokeStream(provider, request,
                apiKey.id().toString(), provider.getId(), emitter);
            return emitter;
        }

        ChatCompletionResponse response = providerInvoker.invoke(provider, request);

        String responseContent = response != null && response.choices() != null && !response.choices().isEmpty()
            ? response.choices().get(0).message().content()
            : "";

        if (responseContent != null && !responseContent.isBlank()) {
            securityInspector.inspectResponse(
                responseContent, "all", apiKey.id(),
                provider != null ? provider.getName() : null, request.model()
            );
        }

        return response;
    }

    @GetMapping("/v1/models")
    public Map<String, Object> listModels() {
        List<ProviderModel> models = providerModelRepository.findAll();

        List<HashMap<String, Object>> data = models.stream()
            .filter(m -> "active".equals(m.getStatus()))
            .map(m -> {
                HashMap<String, Object> entry = new HashMap<>();
                entry.put("id", m.getModelName());
                entry.put("object", "model");
                entry.put("created", System.currentTimeMillis() / 1000);
                entry.put("owned_by", m.getProvider().getName());
                return entry;
            })
            .collect(Collectors.toList());

        return Map.of("object", "list", "data", data);
    }
}
