package com.maas.workflow.dto;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotBlank;

public record WorkflowCreateRequest(
    @NotBlank String name,
    String description,
    JsonNode definitionJson
) {}
