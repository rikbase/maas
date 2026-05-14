package com.maas.workflow.engine.handler;

import com.maas.gateway.dto.ChatCompletionRequest;
import com.maas.gateway.dto.ChatMessage;
import com.maas.gateway.service.ProviderInvoker;
import com.maas.provider.entity.Provider;
import com.maas.provider.entity.ProviderStatus;
import com.maas.provider.repository.ProviderRepository;
import com.maas.workflow.engine.StepHandler;
import com.maas.workflow.engine.StepOutput;
import com.maas.workflow.engine.TemplateEngine;
import com.maas.workflow.entity.StepType;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class AiCallStepHandler implements StepHandler {

    private final ProviderInvoker providerInvoker;
    private final ProviderRepository providerRepository;

    public AiCallStepHandler(ProviderInvoker providerInvoker, ProviderRepository providerRepository) {
        this.providerInvoker = providerInvoker;
        this.providerRepository = providerRepository;
    }

    @Override
    public StepType supportedType() {
        return StepType.ai_call;
    }

    @Override
    public StepOutput execute(Map<String, Object> config, Map<String, Object> context, TemplateEngine te) {
        try {
            String model = (String) config.get("model");
            String providerName = (String) config.get("provider");
            String promptTemplate = (String) config.get("prompt");

            if (model == null || promptTemplate == null) {
                return StepOutput.error("model and prompt are required for ai_call step");
            }

            String prompt = te.resolve(promptTemplate, context);

            Provider provider;
            if (providerName != null) {
                List<Provider> all = providerRepository.findAll();
                provider = all.stream()
                    .filter(p -> p.getName().equalsIgnoreCase(providerName))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Provider not found: " + providerName));
            } else {
                List<Provider> providers = providerRepository.findByStatus(ProviderStatus.enabled);
                if (providers.isEmpty()) {
                    return StepOutput.error("No enabled providers found");
                }
                provider = providers.get(0);
            }

            ChatCompletionRequest req = new ChatCompletionRequest(
                model,
                List.of(new ChatMessage("user", prompt)),
                null, null, false
            );

            var response = providerInvoker.invoke(provider, req);
            return StepOutput.success(response.choices());
        } catch (Exception e) {
            return StepOutput.error("AI call failed: " + e.getMessage());
        }
    }
}
