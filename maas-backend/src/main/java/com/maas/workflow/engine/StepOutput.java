package com.maas.workflow.engine;

public record StepOutput(String status, Object data, String error) {
    public static StepOutput success(Object data) {
        return new StepOutput("completed", data, null);
    }
    public static StepOutput error(String error) {
        return new StepOutput("failed", null, error);
    }
}
