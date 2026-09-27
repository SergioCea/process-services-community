package com.sergio.psc.application;

import com.sergio.psc.domain.WorkflowTask;
import com.sergio.psc.port.in.WorkflowTaskUseCases;
import com.sergio.psc.port.out.WorkflowTaskRepository;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class WorkflowTaskService implements WorkflowTaskUseCases {
    private final WorkflowTaskRepository repository;

    public WorkflowTaskService(WorkflowTaskRepository repository) {
        if (repository == null) {
            throw new IllegalArgumentException("Workflow task repository is required");
        }
        this.repository = repository;
    }

    @Override
    public List<WorkflowTask> listTasks(String userId) {
        requireUserId(userId);
        List<WorkflowTask> tasks = repository.findVisibleTasks(userId);
        return tasks == null ? Collections.<WorkflowTask>emptyList() : tasks;
    }

    @Override
    public WorkflowTask claimTask(String taskId, String userId) {
        requireTaskId(taskId);
        requireUserId(userId);
        return repository.claim(taskId, userId);
    }

    @Override
    public void completeTask(String taskId, String userId, Map<String, Object> variables) {
        requireTaskId(taskId);
        requireUserId(userId);
        repository.complete(taskId, userId, variables == null ? Collections.<String, Object>emptyMap() : variables);
    }

    private void requireTaskId(String taskId) {
        if (taskId == null || taskId.trim().isEmpty()) {
            throw new IllegalArgumentException("Workflow task id is required");
        }
    }

    private void requireUserId(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("Authenticated user is required");
        }
    }
}