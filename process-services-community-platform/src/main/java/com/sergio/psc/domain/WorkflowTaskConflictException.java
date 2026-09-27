package com.sergio.psc.domain;

public class WorkflowTaskConflictException extends RuntimeException {
    public WorkflowTaskConflictException(String message) {
        super(message);
    }
}