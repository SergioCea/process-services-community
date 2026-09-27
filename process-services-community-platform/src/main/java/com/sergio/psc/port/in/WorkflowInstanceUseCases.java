package com.sergio.psc.port.in;

import com.sergio.psc.domain.WorkflowInstance;

import java.util.Map;

public interface WorkflowInstanceUseCases {
    WorkflowInstance start(String processDefinitionId, Map<String, Object> variables);
}