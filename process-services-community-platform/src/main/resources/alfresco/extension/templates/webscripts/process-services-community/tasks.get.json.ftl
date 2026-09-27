{
"tasks": [
<#list tasks as task>
    {
    "id": "${task.id?json_string}",
    "name": "${(task.name!"")?json_string}",
    "description": "${(task.description!"")?json_string}",
    "assignee": "${(task.assignee!"")?json_string}",
    "processDefinitionId": "${task.processDefinitionId?json_string}",
    "processInstanceId": "${task.processInstanceId?json_string}",
    "createTime": "${(task.createTime!"")?json_string}",
    "formKey": "${(task.formKey!"")?json_string}",
    "formSchema": ${task.formSchemaJson!"null"},
    "claimedByCurrentUser": ${task.claimedByCurrentUser?string('true', 'false')}
    }<#sep>,</#sep>
</#list>
]
}