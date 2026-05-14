package com.maas.workflow.dto;

import com.fasterxml.jackson.databind.JsonNode;

public record WorkflowUpdateRequest(
    String name,
    String description,
    JsonNode definitionJson
) {}
