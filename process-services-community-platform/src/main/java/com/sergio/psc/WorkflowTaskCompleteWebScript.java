package com.sergio.psc;

import com.sergio.psc.domain.WorkflowTaskConflictException;
import com.sergio.psc.port.in.WorkflowTaskUseCases;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.*;

import java.util.HashMap;
import java.util.Map;

public class WorkflowTaskCompleteWebScript extends DeclarativeWebScript {
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
            String content = req.getContent().getContent();
            if (content == null || content.trim().isEmpty()) {
                throw new WebScriptException(Status.STATUS_BAD_REQUEST, "No task completion request provided");
            }

            JSONObject request = new JSONObject(content);
            String taskId = request.getString("id");
            Map<String, Object> variables = request.has("variables")
                    ? WorkflowWebScriptUtils.toVariables(request.getJSONObject("variables"))
                    : new HashMap<String, Object>();
            workflowTaskUseCases.completeTask(taskId, userId, variables);

            Map<String, Object> model = new HashMap<>();
            model.put("id", taskId);
            model.put("completed", true);
            return model;
        } catch (WebScriptException e) {
            throw e;
        } catch (JSONException e) {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Invalid task completion request: " + e.getMessage());
        } catch (WorkflowTaskConflictException e) {
            throw new WebScriptException(Status.STATUS_CONFLICT, e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new WebScriptException(Status.STATUS_NOT_FOUND, e.getMessage());
        } catch (Exception e) {
            throw new WebScriptException(Status.STATUS_INTERNAL_SERVER_ERROR, "Error completing workflow task: " + e.getMessage());
        }
    }
}