{
"id": "${id?json_string}",
"assignee": "${(assignee!"")?json_string}",
"claimedByCurrentUser": ${claimedByCurrentUser?string('true', 'false')}
}