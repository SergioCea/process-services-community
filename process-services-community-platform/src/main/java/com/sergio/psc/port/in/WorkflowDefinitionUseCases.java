package com.sergio.psc.port.in;

import com.sergio.psc.domain.WorkflowDefinition;

import java.util.List;
import java.util.Map;

public interface WorkflowDefinitionUseCases {
    WorkflowDefinition deploy(String xml, String name, String title, Map<String, String> forms);

    WorkflowDefinition get(String id);

    List<WorkflowDefinition> list();

    void delete(String id, boolean allVersions);
}