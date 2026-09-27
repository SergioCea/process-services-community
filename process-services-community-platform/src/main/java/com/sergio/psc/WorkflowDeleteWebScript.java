package com.sergio.psc;

import com.sergio.psc.port.in.WorkflowDefinitionUseCases;
import org.springframework.extensions.webscripts.*;

import java.util.HashMap;
import java.util.Map;

public class WorkflowDeleteWebScript extends DeclarativeWebScript {
    private WorkflowDefinitionUseCases workflowDefinitionUseCases;

    public void setWorkflowDefinitionUseCases(WorkflowDefinitionUseCases workflowDefinitionUseCases) {
        this.workflowDefinitionUseCases = workflowDefinitionUseCases;
    }

    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {
        String id = req.getParameter("id");
        if (id == null || id.trim().isEmpty()) {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST, "No definition id provided");
        }

        boolean allVersions = "true".equalsIgnoreCase(req.getParameter("all"));
        try {
            workflowDefinitionUseCases.delete(id, allVersions);
            Map<String, Object> model = new HashMap<>();
            model.put("id", id);
            model.put("message", allVersions
                    ? "Successfully undeployed all versions of workflow: " + id
                    : "Successfully undeployed workflow definition: " + id);
            return model;
        } catch (IllegalArgumentException e) {
            throw new WebScriptException(Status.STATUS_NOT_FOUND, e.getMessage());
        } catch (Exception e) {
            throw new WebScriptException(Status.STATUS_INTERNAL_SERVER_ERROR, "Error deleting workflow definition: " + e.getMessage());
        }
    }
}
