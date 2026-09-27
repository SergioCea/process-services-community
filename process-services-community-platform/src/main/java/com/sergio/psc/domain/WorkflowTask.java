package com.sergio.psc.domain;

public class WorkflowTask {
    private final String id;
    private final String name;
    private final String description;
    private final String assignee;
    private final String processDefinitionId;
    private final String processInstanceId;
    private final String createTime;
    private final String formKey;
    private final String formSchemaJson;
    private final boolean claimedByCurrentUser;

    public WorkflowTask(String id, String name, String description, String assignee, String processDefinitionId,
                        String processInstanceId, String createTime, String formKey, String formSchemaJson,
                        boolean claimedByCurrentUser) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.assignee = assignee;
        this.processDefinitionId = processDefinitionId;
        this.processInstanceId = processInstanceId;
        this.createTime = createTime;
        this.formKey = formKey;
        this.formSchemaJson = formSchemaJson;
        this.claimedByCurrentUser = claimedByCurrentUser;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getAssignee() {
        return assignee;
    }

    public String getProcessDefinitionId() {
        return processDefinitionId;
    }

    public String getProcessInstanceId() {
        return processInstanceId;
    }

    public String getCreateTime() {
        return createTime;
    }

    public String getFormKey() {
        return formKey;
    }

    public String getFormSchemaJson() {
        return formSchemaJson;
    }

    public boolean isClaimedByCurrentUser() {
        return claimedByCurrentUser;
    }
}