package com.sergio.psc.application;

import com.sergio.psc.domain.WorkflowDefinition;
import com.sergio.psc.port.in.WorkflowDefinitionUseCases;
import com.sergio.psc.port.out.WorkflowDefinitionRepository;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class WorkflowDefinitionService implements WorkflowDefinitionUseCases {
    private final WorkflowDefinitionRepository repository;

    public WorkflowDefinitionService(WorkflowDefinitionRepository repository) {
        if (repository == null) {
            throw new IllegalArgumentException("Workflow definition repository is required");
        }
        this.repository = repository;
    }

    @Override
    public WorkflowDefinition deploy(String xml, String name, String title, Map<String, String> forms) {
        if (xml == null || xml.trim().isEmpty()) {
            throw new IllegalArgumentException("BPMN XML is required");
        }
        return repository.deploy(xml, name, title, forms == null ? Collections.<String, String>emptyMap() : forms);
    }

    @Override
    public WorkflowDefinition get(String id) {
        requireId(id);
        return repository.get(id);
    }

    @Override
    public List<WorkflowDefinition> list() {
        List<WorkflowDefinition> definitions = repository.list();
        return definitions == null ? Collections.<WorkflowDefinition>emptyList() : definitions;
    }

    @Override
    public void delete(String id, boolean allVersions) {
        requireId(id);
        repository.delete(id, allVersions);
    }

    private void requireId(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Workflow definition id is required");
        }
    }
}