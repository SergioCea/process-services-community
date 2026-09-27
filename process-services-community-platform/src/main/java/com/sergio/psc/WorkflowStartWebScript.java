package com.sergio.psc;

import com.sergio.psc.domain.WorkflowInstance;
import com.sergio.psc.port.in.WorkflowInstanceUseCases;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.*;

import java.util.HashMap;
import java.util.Map;

public class WorkflowStartWebScript extends DeclarativeWebScript {
    private WorkflowInstanceUseCases workflowInstanceUseCases;

    public void setWorkflowInstanceUseCases(WorkflowInstanceUseCases workflowInstanceUseCases) {
        this.workflowInstanceUseCases = workflowInstanceUseCases;
    }

    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {
        try {
            String content = req.getContent().getContent();
            if (content == null || content.trim().isEmpty()) {
                throw new WebScriptException(Status.STATUS_BAD_REQUEST, "No process start request provided");
            }

            JSONObject request = new JSONObject(content);
            String processDefinitionId = request.getString("id");
            if (processDefinitionId.trim().isEmpty()) {
                throw new WebScriptException(Status.STATUS_BAD_REQUEST, "No process definition id provided");
            }
            Map<String, Object> variables = request.has("variables")
                    ? WorkflowWebScriptUtils.toVariables(request.getJSONObject("variables"))
                    : new HashMap<String, Object>();
            WorkflowInstance instance = workflowInstanceUseCases.start(processDefinitionId, variables);

            Map<String, Object> model = new HashMap<>();
            model.put("id", instance.getId());
            model.put("processDefinitionId", instance.getProcessDefinitionId());
            model.put("businessKey", instance.getBusinessKey());
            return model;
        } catch (WebScriptException e) {
            throw e;
        } catch (JSONException e) {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Invalid process start request: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new WebScriptException(Status.STATUS_NOT_FOUND, e.getMessage());
        } catch (IllegalStateException e) {
            throw new WebScriptException(Status.STATUS_CONFLICT, e.getMessage());
        } catch (Exception e) {
            throw new WebScriptException(Status.STATUS_INTERNAL_SERVER_ERROR, "Error starting process: " + e.getMessage());
        }
    }

}