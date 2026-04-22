package com.pm.platform.service;

import java.util.List;

public class WorkflowViolationException extends RuntimeException {

    private final List<String> allowedTransitions;

    public WorkflowViolationException(String message, List<String> allowedTransitions) {
        super(message);
        this.allowedTransitions = allowedTransitions;
    }

    public List<String> getAllowedTransitions() {
        return allowedTransitions;
    }
}
