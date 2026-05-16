package com.maas.gateway.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ChatCompletionChunk(
    String id,
    String object,
    long created,
    String model,
    String provider,
    List<ChunkChoice> choices
) {
    public record ChunkChoice(int index, Delta delta, String finishReason) {}
    public record Delta(String role, String content) {}
}
