package com.sergio.psc;

import com.sergio.psc.domain.WorkflowDefinition;
import com.sergio.psc.port.in.WorkflowDefinitionUseCases;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.*;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class WorkflowDeployWebScript extends DeclarativeWebScript {
    private WorkflowDefinitionUseCases workflowDefinitionUseCases;

    public void setWorkflowDefinitionUseCases(WorkflowDefinitionUseCases workflowDefinitionUseCases) {
        this.workflowDefinitionUseCases = workflowDefinitionUseCases;
    }

    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {
        Map<String, Object> model = new HashMap<>();
        try {
            String content = req.getContent().getContent();
            if (content == null || content.trim().isEmpty()) {
                throw new WebScriptException(Status.STATUS_BAD_REQUEST, "No content provided");
            }

            String xml;
            String name = null;
            String title = null;
            Map<String, String> forms = new LinkedHashMap<>();
            if (content.trim().startsWith("{")) {
                JSONObject request = new JSONObject(content);
                if (!request.has("xml")) {
                    throw new WebScriptException(Status.STATUS_BAD_REQUEST, "No BPMN XML provided");
                }
                xml = request.getString("xml");
                name = request.has("name") ? request.getString("name") : null;
                title = request.has("title") ? request.getString("title") : null;
                if (request.has("forms")) {
                    JSONObject submittedForms = request.getJSONObject("forms");
                    for (String elementId : submittedForms.keySet()) {
                        forms.put(elementId, submittedForms.get(elementId).toString());
                    }
                }
            } else {
                xml = content;
            }

            WorkflowDefinition definition = workflowDefinitionUseCases.deploy(xml, name, title, forms);
            model.put("id", definition.getId());
            model.put("name", definition.getName());
            model.put("title", definition.getTitle());
            model.put("version", definition.getVersion());
            return model;
        } catch (WebScriptException e) {
            throw e;
        } catch (JSONException | IllegalArgumentException e) {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Invalid workflow deployment request: " + e.getMessage());
        } catch (Exception e) {
            throw new WebScriptException(Status.STATUS_INTERNAL_SERVER_ERROR, "Error deploying workflow: " + e.getMessage());
        }
    }
}
