package com.sergio.psc;

import com.sergio.psc.port.in.WorkflowTaskUseCases;
import org.springframework.extensions.webscripts.*;

import java.util.HashMap;
import java.util.Map;

public class WorkflowTasksGetWebScript extends DeclarativeWebScript {
    private WorkflowTaskUseCases workflowTaskUseCases;

    public void setWorkflowTaskUseCases(WorkflowTaskUseCases workflowTaskUseCases) {
        this.workflowTaskUseCases = workflowTaskUseCases;
    }

    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {
        String userId = WorkflowWebScriptUtils.getAuthenticatedUser();
        if (userId == null || userId.trim().isEmpty()) {
            throw new WebScriptException(Status.STATUS_UNAUTHORIZED, "An authenticated user is required");
        }
        try {
            Map<String, Object> model = new HashMap<>();
            model.put("tasks", workflowTaskUseCases.listTasks(userId));
            return model;
        } catch (Exception e) {
            throw new WebScriptException(Status.STATUS_INTERNAL_SERVER_ERROR, "Error listing workflow tasks: " + e.getMessage());
        }
    }
}