package com.sergio.psc.domain;

public class WorkflowInstance {
    private final String id;
    private final String processDefinitionId;
    private final String businessKey;

    public WorkflowInstance(String id, String processDefinitionId, String businessKey) {
        this.id = id;
        this.processDefinitionId = processDefinitionId;
        this.businessKey = businessKey;
    }

    public String getId() {
        return id;
    }

    public String getProcessDefinitionId() {
        return processDefinitionId;
    }

    public String getBusinessKey() {
        return businessKey;
    }
}