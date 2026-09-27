package com.sergio.psc.port.in;

import com.sergio.psc.domain.WorkflowTask;

import java.util.List;
import java.util.Map;

public interface WorkflowTaskUseCases {
    List<WorkflowTask> listTasks(String userId);

    WorkflowTask claimTask(String taskId, String userId);

    void completeTask(String taskId, String userId, Map<String, Object> variables);
}