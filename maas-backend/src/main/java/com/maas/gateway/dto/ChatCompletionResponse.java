package com.maas.gateway.dto;

import java.util.List;

public record ChatCompletionResponse(
    String id,
    String object,
    long created,
    String model,
    List<Choice> choices,
    Usage usage
) {
    public record Choice(int index, ChatMessage message, String finishReason) {}
    public record Usage(int promptTokens, int completionTokens, int totalTokens) {}
}
