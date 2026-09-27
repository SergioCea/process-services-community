package com.sergio.psc.port.out;

import com.sergio.psc.domain.WorkflowTask;

import java.util.List;
import java.util.Map;

public interface WorkflowTaskRepository {
    List<WorkflowTask> findVisibleTasks(String userId);

    WorkflowTask claim(String taskId, String userId);

    void complete(String taskId, String userId, Map<String, Object> variables);
}