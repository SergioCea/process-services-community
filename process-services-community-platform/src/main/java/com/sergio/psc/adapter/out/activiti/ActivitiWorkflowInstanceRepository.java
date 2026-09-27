package com.sergio.psc.adapter.out.activiti;

import com.sergio.psc.domain.WorkflowInstance;
import com.sergio.psc.port.out.WorkflowInstanceRepository;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;

import java.util.Collections;
import java.util.Map;

public class ActivitiWorkflowInstanceRepository implements WorkflowInstanceRepository {
    private static final String ID_PREFIX = "activiti$";

    private ProcessEngine processEngine;

    public void setProcessEngine(ProcessEngine processEngine) {
        this.processEngine = processEngine;
    }

    @Override
    public WorkflowInstance start(String processDefinitionId, Map<String, Object> variables) {
        String engineId = normalizeId(processDefinitionId);
        ProcessDefinition definition = processEngine.getRepositoryService().createProcessDefinitionQuery()
                .processDefinitionId(engineId)
                .singleResult();
        if (definition == null) {
            throw new IllegalArgumentException("Workflow definition not found: " + processDefinitionId);
        }
        if (definition.isSuspended()) {
            throw new IllegalStateException("Workflow definition is suspended: " + processDefinitionId);
        }

        ProcessInstance instance = processEngine.getRuntimeService().startProcessInstanceById(
                definition.getId(),
                variables == null ? Collections.<String, Object>emptyMap() : variables
        );
        return new WorkflowInstance(instance.getId(), ID_PREFIX + definition.getId(), instance.getBusinessKey());
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
}