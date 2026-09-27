{
"entries": [
<#list workflows as w>
    {
    "id": "${w.id?json_string}",
    "name": "${(w.name!"")?json_string}",
    "title": "${(w.title!"")?json_string}",
    "version": "${(w.version!"")?json_string}",
    "description": "${(w.description!"")?json_string}"
    }<#if w_has_next>,</#if>
</#list>
]
}
