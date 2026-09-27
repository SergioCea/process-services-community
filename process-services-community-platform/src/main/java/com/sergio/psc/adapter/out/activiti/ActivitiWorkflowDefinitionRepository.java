package com.sergio.psc.adapter.out.activiti;

import com.sergio.psc.domain.WorkflowDefinition;
import com.sergio.psc.port.out.WorkflowDefinitionRepository;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.DeploymentBuilder;
import org.activiti.engine.repository.ProcessDefinition;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class ActivitiWorkflowDefinitionRepository implements WorkflowDefinitionRepository {
    private static final String ID_PREFIX = "activiti$";
    private static final String FORMS_RESOURCE = "process-services-community-forms.json";
    private static final String DEPLOYMENT_CATEGORY = "com.sergio.process-services-community";

    private ProcessEngine processEngine;

    public void setProcessEngine(ProcessEngine processEngine) {
        this.processEngine = processEngine;
    }

    @Override
    public WorkflowDefinition deploy(String xml, String name, String title, Map<String, String> forms) {
        if (xml == null || xml.trim().isEmpty()) {
            throw new IllegalArgumentException("BPMN XML is required");
        }

        String deploymentTitle = firstNonEmpty(title, name, "Process Services Community Deployment");
        String resourceName = resourceName(name);
        DeploymentBuilder builder = processEngine.getRepositoryService().createDeployment()
                .name(deploymentTitle)
                .category(DEPLOYMENT_CATEGORY)
                .addString(resourceName, xml);

        builder.addString(FORMS_RESOURCE, serializeForms(forms == null ? Collections.<String, String>emptyMap() : forms));

        Deployment deployment = builder.deploy();
        ProcessDefinition definition = processEngine.getRepositoryService().createProcessDefinitionQuery()
                .deploymentId(deployment.getId())
                .singleResult();
        if (definition == null) {
            throw new IllegalStateException("The deployment contains no process definition");
        }
        return toWorkflowDefinition(definition, null, forms == null ? Collections.<String, String>emptyMap() : forms);
    }

    @Override
    public WorkflowDefinition get(String id) {
        String processDefinitionId = normalizeId(id);
        ProcessDefinition definition = processEngine.getRepositoryService().createProcessDefinitionQuery()
                .processDefinitionId(processDefinitionId)
                .singleResult();
        return definition == null ? null : toWorkflowDefinition(definition, readModel(definition), readForms(definition));
    }

    @Override
    public List<WorkflowDefinition> list() {
        List<ProcessDefinition> processDefinitions = processEngine.getRepositoryService().createProcessDefinitionQuery()
                .orderByProcessDefinitionName().asc()
                .orderByProcessDefinitionVersion().desc()
                .list();
        Set<String> processServicesDeployments = new LinkedHashSet<>();
        List<Deployment> categorizedDeployments = processEngine.getRepositoryService().createDeploymentQuery()
                .deploymentCategory(DEPLOYMENT_CATEGORY)
                .list();
        for (Deployment deployment : categorizedDeployments) {
            processServicesDeployments.add(deployment.getId());
        }

        List<WorkflowDefinition> definitions = new ArrayList<>();
        for (ProcessDefinition definition : processDefinitions) {
            String deploymentId = definition.getDeploymentId();
            if (!processServicesDeployments.contains(deploymentId) && hasProcessServicesFormsResource(deploymentId)) {
                processServicesDeployments.add(deploymentId);
            }
            if (!processServicesDeployments.contains(deploymentId)) {
                continue;
            }
            definitions.add(toWorkflowDefinition(definition, null, Collections.<String, String>emptyMap()));
        }
        return definitions;
    }

    @Override
    public void delete(String id, boolean allVersions) {
        String processDefinitionId = normalizeId(id);
        ProcessDefinition definition = processEngine.getRepositoryService().createProcessDefinitionQuery()
                .processDefinitionId(processDefinitionId)
                .singleResult();
        if (definition == null) {
            throw new IllegalArgumentException("Workflow definition not found: " + id);
        }

        Set<String> deployments = new LinkedHashSet<>();
        if (allVersions) {
            List<ProcessDefinition> versions = processEngine.getRepositoryService().createProcessDefinitionQuery()
                    .processDefinitionKey(definition.getKey())
                    .list();
            for (ProcessDefinition version : versions) {
                deployments.add(version.getDeploymentId());
            }
        } else {
            deployments.add(definition.getDeploymentId());
        }

        for (String deploymentId : deployments) {
            processEngine.getRepositoryService().deleteDeployment(deploymentId, false);
        }
    }

    private WorkflowDefinition toWorkflowDefinition(ProcessDefinition definition, String xml, Map<String, String> forms) {
        Deployment deployment = processEngine.getRepositoryService().createDeploymentQuery()
                .deploymentId(definition.getDeploymentId())
                .singleResult();
        String title = deployment == null ? null : deployment.getName();
        return new WorkflowDefinition(
                ID_PREFIX + definition.getId(),
                definition.getName() == null ? definition.getKey() : definition.getName(),
                firstNonEmpty(title, definition.getName(), definition.getKey()),
                String.valueOf(definition.getVersion()),
                definition.getDescription(),
                xml,
                forms
        );
    }

    private String readModel(ProcessDefinition definition) {
        InputStream input = processEngine.getRepositoryService().getProcessModel(definition.getId());
        if (input == null) {
            return "";
        }
        try {
            return readFully(input);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to read BPMN model for " + definition.getId(), e);
        }
    }

    private Map<String, String> readForms(ProcessDefinition definition) {
        if (!processEngine.getRepositoryService().getDeploymentResourceNames(definition.getDeploymentId()).contains(FORMS_RESOURCE)) {
            return Collections.emptyMap();
        }
        InputStream input = processEngine.getRepositoryService().getResourceAsStream(definition.getDeploymentId(), FORMS_RESOURCE);
        if (input == null) {
            return Collections.emptyMap();
        }
        try {
            JSONObject formsJson = new JSONObject(readFully(input));
            Map<String, String> forms = new LinkedHashMap<>();
            for (String elementId : formsJson.keySet()) {
                Object schema = formsJson.get(elementId);
                forms.put(elementId, schema instanceof JSONObject ? ((JSONObject) schema).toString() : String.valueOf(schema));
            }
            return forms;
        } catch (IOException | JSONException e) {
            throw new IllegalStateException("Unable to read workflow forms for " + definition.getId(), e);
        }
    }

    private boolean hasProcessServicesFormsResource(String deploymentId) {
        return processEngine.getRepositoryService().getDeploymentResourceNames(deploymentId).contains(FORMS_RESOURCE);
    }

    private String serializeForms(Map<String, String> forms) {
        JSONObject formsJson = new JSONObject();
        try {
            for (Map.Entry<String, String> form : forms.entrySet()) {
                if (form.getKey() == null || form.getKey().trim().isEmpty()) {
                    throw new IllegalArgumentException("Form element id is required");
                }
                formsJson.put(form.getKey(), new JSONObject(form.getValue()));
            }
        } catch (JSONException e) {
            throw new IllegalArgumentException("Form schemas must be valid JSON objects", e);
        }
        return formsJson.toString();
    }

    private String normalizeId(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Workflow definition id is required");
        }
        if (id.startsWith(ID_PREFIX)) {
            return id.substring(ID_PREFIX.length());
        }
        if (id.indexOf('$') >= 0) {
            throw new IllegalArgumentException("Unsupported workflow engine id: " + id);
        }
        return id;
    }

    private String resourceName(String name) {
        String baseName = firstNonEmpty(name, null, "process").replaceAll("[^A-Za-z0-9._-]", "_");
        return baseName.endsWith(".bpmn20.xml") ? baseName : baseName + ".bpmn20.xml";
    }

    private String firstNonEmpty(String first, String second, String fallback) {
        if (first != null && !first.trim().isEmpty()) {
            return first;
        }
        if (second != null && !second.trim().isEmpty()) {
            return second;
        }
        return fallback;
    }

    private String readFully(InputStream input) throws IOException {
        try (InputStream stream = input; ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[4096];
            int read;
            while ((read = stream.read(buffer)) != -1) {
                output.write(buffer, 0, read);
            }
            return new String(output.toByteArray(), StandardCharsets.UTF_8);
        }
    }
}