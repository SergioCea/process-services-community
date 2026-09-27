package com.sergio.psc.application;

import com.sergio.psc.domain.WorkflowInstance;
import com.sergio.psc.port.in.WorkflowInstanceUseCases;
import com.sergio.psc.port.out.WorkflowInstanceRepository;

import java.util.Collections;
import java.util.Map;

public class WorkflowInstanceService implements WorkflowInstanceUseCases {
    private final WorkflowInstanceRepository repository;

    public WorkflowInstanceService(WorkflowInstanceRepository repository) {
        if (repository == null) {
            throw new IllegalArgumentException("Workflow instance repository is required");
        }
        this.repository = repository;
    }

    @Override
    public WorkflowInstance start(String processDefinitionId, Map<String, Object> variables) {
        if (processDefinitionId == null || processDefinitionId.trim().isEmpty()) {
            throw new IllegalArgumentException("Workflow definition id is required");
        }
        return repository.start(processDefinitionId, variables == null ? Collections.<String, Object>emptyMap() : variables);
    }
}