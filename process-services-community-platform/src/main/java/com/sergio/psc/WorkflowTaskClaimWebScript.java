package com.sergio.psc;

import com.sergio.psc.domain.WorkflowTask;
import com.sergio.psc.domain.WorkflowTaskConflictException;
import com.sergio.psc.port.in.WorkflowTaskUseCases;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.*;

import java.util.HashMap;
import java.util.Map;

public class WorkflowTaskClaimWebScript extends DeclarativeWebScript {
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
                throw new WebScriptException(Status.STATUS_BAD_REQUEST, "No task claim request provided");
            }
            String taskId = new JSONObject(content).getString("id");
            WorkflowTask task = workflowTaskUseCases.claimTask(taskId, userId);
            Map<String, Object> model = new HashMap<>();
            model.put("id", task.getId());
            model.put("assignee", task.getAssignee());
            model.put("claimedByCurrentUser", task.isClaimedByCurrentUser());
            return model;
        } catch (WebScriptException e) {
            throw e;
        } catch (JSONException e) {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Invalid task claim request: " + e.getMessage());
        } catch (WorkflowTaskConflictException e) {
            throw new WebScriptException(Status.STATUS_CONFLICT, e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new WebScriptException(Status.STATUS_NOT_FOUND, e.getMessage());
        } catch (Exception e) {
            throw new WebScriptException(Status.STATUS_INTERNAL_SERVER_ERROR, "Error claiming workflow task: " + e.getMessage());
        }
    }
}