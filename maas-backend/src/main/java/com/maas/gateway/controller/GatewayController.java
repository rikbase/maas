package com.maas.gateway.controller;

import com.maas.apikey.dto.KeyVO;
import com.maas.gateway.dto.ChatCompletionRequest;
import com.maas.gateway.dto.ChatCompletionResponse;
import com.maas.gateway.service.ModelRouter;
import com.maas.gateway.service.ProviderInvoker;
import com.maas.gateway.service.RateLimiter;
import com.maas.provider.entity.Provider;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
public class GatewayController {
    private final ModelRouter modelRouter;
    private final ProviderInvoker providerInvoker;
    private final RateLimiter rateLimiter;

    public GatewayController(ModelRouter modelRouter, ProviderInvoker providerInvoker, RateLimiter rateLimiter) {
        this.modelRouter = modelRouter;
        this.providerInvoker = providerInvoker;
        this.rateLimiter = rateLimiter;
    }

    @PostMapping("/v1/chat/completions")
    public ChatCompletionResponse chatCompletion(
            @Valid @RequestBody ChatCompletionRequest request,
            @RequestAttribute KeyVO apiKey) {
        rateLimiter.check(apiKey.id().toString(), 60);
        String preferredProvider = null;
        Provider provider = modelRouter.route(request.model(), preferredProvider);
        return providerInvoker.invoke(provider, request);
    }

    @GetMapping("/v1/models")
    public String listModels() {
        return "{\"object\":\"list\",\"data\":[]}";
    }
}
