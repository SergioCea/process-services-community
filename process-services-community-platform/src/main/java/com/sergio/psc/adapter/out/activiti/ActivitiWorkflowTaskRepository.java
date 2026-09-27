package com.sergio.psc.adapter.out.activiti;

import com.sergio.psc.domain.WorkflowTask;
import com.sergio.psc.domain.WorkflowTaskConflictException;
import com.sergio.psc.port.out.WorkflowTaskRepository;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.task.Task;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;

public class ActivitiWorkflowTaskRepository implements WorkflowTaskRepository {
    private static final String FORMS_RESOURCE = "process-services-community-forms.json";

    private ProcessEngine processEngine;

    public void setProcessEngine(ProcessEngine processEngine) {
        this.processEngine = processEngine;
    }

    @Override
    public List<WorkflowTask> findVisibleTasks(String userId) {
        List<Task> tasks = processEngine.getTaskService().createTaskQuery()
                .active()
                .taskCandidateOrAssigned(userId)
                .orderByTaskCreateTime().desc()
                .list();
        List<WorkflowTask> visibleTasks = new ArrayList<>();
        for (Task task : tasks) {
            if (isProcessServicesTask(task)) {
                visibleTasks.add(toWorkflowTask(task, userId));
            }
        }
        return visibleTasks;
    }

    @Override
    public WorkflowTask claim(String taskId, String userId) {
        Task task = visibleTask(taskId, userId);
        if (task.getAssignee() != null) {
            if (userId.equals(task.getAssignee())) {
                return toWorkflowTask(task, userId);
            }
            throw new WorkflowTaskConflictException("Task is already claimed by another user");
        }

        processEngine.getTaskService().claim(taskId, userId);
        Task claimedTask = processEngine.getTaskService().createTaskQuery().taskId(taskId).singleResult();
        return toWorkflowTask(claimedTask, userId);
    }

    @Override
    public void complete(String taskId, String userId, Map<String, Object> variables) {
        Task task = processEngine.getTaskService().createTaskQuery()
                .taskId(taskId)
                .taskAssignee(userId)
                .singleResult();
        if (task == null) {
            throw new IllegalArgumentException("Task not found or not assigned to the authenticated user: " + taskId);
        }
        if (!isProcessServicesTask(task)) {
            throw new IllegalArgumentException("Task not found or not assigned to the authenticated user: " + taskId);
        }
        processEngine.getTaskService().complete(taskId, variables == null ? Collections.<String, Object>emptyMap() : variables);
    }

    private Task visibleTask(String taskId, String userId) {
        Task task = processEngine.getTaskService().createTaskQuery()
                .taskId(taskId)
                .taskCandidateOrAssigned(userId)
                .active()
                .singleResult();
        if (task == null || !isProcessServicesTask(task)) {
            throw new IllegalArgumentException("Task not found or not visible to the authenticated user: " + taskId);
        }
        return task;
    }

    private boolean isProcessServicesTask(Task task) {
        ProcessDefinition definition = processEngine.getRepositoryService().createProcessDefinitionQuery()
                .processDefinitionId(task.getProcessDefinitionId())
                .singleResult();
        if (definition == null) {
            return false;
        }
        InputStream resource = processEngine.getRepositoryService().getResourceAsStream(definition.getDeploymentId(), FORMS_RESOURCE);
        if (resource == null) {
            return false;
        }
        try {
            resource.close();
            return true;
        } catch (IOException e) {
            throw new IllegalStateException("Unable to identify workflow task " + task.getId(), e);
        }
    }

    private WorkflowTask toWorkflowTask(Task task, String userId) {
        String formKey = task.getFormKey();
        String formSchemaJson = formKey == null || formKey.trim().isEmpty() ? null : readFormSchema(task);
        Date createTime = task.getCreateTime();
        String createdAt = createTime == null ? null : Instant.ofEpochMilli(createTime.getTime()).toString();
        return new WorkflowTask(
                task.getId(),
                task.getName(),
                task.getDescription(),
                task.getAssignee(),
                task.getProcessDefinitionId(),
                task.getProcessInstanceId(),
                createdAt,
                formKey,
                formSchemaJson,
                userId.equals(task.getAssignee())
        );
    }

    private String readFormSchema(Task task) {
        ProcessDefinition definition = processEngine.getRepositoryService().createProcessDefinitionQuery()
                .processDefinitionId(task.getProcessDefinitionId())
                .singleResult();
        if (definition == null) {
            throw new IllegalStateException("Process definition not found for task " + task.getId());
        }

        InputStream resource = processEngine.getRepositoryService().getResourceAsStream(definition.getDeploymentId(), FORMS_RESOURCE);
        if (resource == null) {
            throw new IllegalStateException("Form resource not found for task " + task.getId());
        }

        try {
            JSONObject forms = new JSONObject(readFully(resource));
            String taskDefinitionKey = task.getTaskDefinitionKey();
            if (taskDefinitionKey == null || !forms.has(taskDefinitionKey)) {
                throw new IllegalStateException("Form schema not found for task " + task.getId());
            }
            return forms.getJSONObject(taskDefinitionKey).toString();
        } catch (IOException | JSONException e) {
            throw new IllegalStateException("Unable to read form schema for task " + task.getId(), e);
        }
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