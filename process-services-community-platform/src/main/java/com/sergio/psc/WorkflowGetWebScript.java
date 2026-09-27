package com.sergio.psc;

import com.sergio.psc.domain.WorkflowDefinition;
import com.sergio.psc.port.in.WorkflowDefinitionUseCases;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.*;

import java.util.HashMap;
import java.util.Map;

public class WorkflowGetWebScript extends DeclarativeWebScript {
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

        try {
            WorkflowDefinition definition = workflowDefinitionUseCases.get(id);
            if (definition == null) {
                throw new WebScriptException(Status.STATUS_NOT_FOUND, "Workflow definition not found: " + id);
            }

            Map<String, Object> model = new HashMap<>();
            model.put("id", definition.getId());
            model.put("name", definition.getName());
            model.put("title", definition.getTitle());
            model.put("version", definition.getVersion());
            model.put("description", definition.getDescription());
            model.put("xml", definition.getXml());
            model.put("formsJson", formsAsJson(definition.getForms()));
            return model;
        } catch (WebScriptException e) {
            throw e;
        } catch (JSONException e) {
            throw new WebScriptException(Status.STATUS_INTERNAL_SERVER_ERROR, "Invalid stored workflow form schema");
        } catch (Exception e) {
            throw new WebScriptException(Status.STATUS_INTERNAL_SERVER_ERROR, "Error getting workflow definition: " + e.getMessage());
        }
    }

    private String formsAsJson(Map<String, String> forms) throws JSONException {
        JSONObject result = new JSONObject();
        for (Map.Entry<String, String> form : forms.entrySet()) {
            result.put(form.getKey(), new JSONObject(form.getValue()));
        }
        return result.toString();
    }
}
