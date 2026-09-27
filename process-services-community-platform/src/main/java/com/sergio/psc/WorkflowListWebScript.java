package com.sergio.psc;

import com.sergio.psc.port.in.WorkflowDefinitionUseCases;
import org.springframework.extensions.webscripts.*;

import java.util.HashMap;
import java.util.Map;

public class WorkflowListWebScript extends DeclarativeWebScript {
    private WorkflowDefinitionUseCases workflowDefinitionUseCases;

    public void setWorkflowDefinitionUseCases(WorkflowDefinitionUseCases workflowDefinitionUseCases) {
        this.workflowDefinitionUseCases = workflowDefinitionUseCases;
    }

    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {
        try {
            Map<String, Object> model = new HashMap<>();
            model.put("workflows", workflowDefinitionUseCases.list());
            return model;
        } catch (Exception e) {
            throw new WebScriptException(Status.STATUS_INTERNAL_SERVER_ERROR, "Error listing workflows: " + e.getMessage());
        }
    }
}
