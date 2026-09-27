package com.sergio.psc.port.out;

import com.sergio.psc.domain.WorkflowInstance;

import java.util.Map;

public interface WorkflowInstanceRepository {
    WorkflowInstance start(String processDefinitionId, Map<String, Object> variables);
}