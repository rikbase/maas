package com.maas.gateway.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record ChatCompletionRequest(
    @NotBlank String model,
    @NotEmpty List<ChatMessage> messages,
    Double temperature,
    Integer maxTokens,
    Boolean stream
) {}
