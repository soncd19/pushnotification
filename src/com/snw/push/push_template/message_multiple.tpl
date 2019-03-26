{
    "data": {
        "title": "${title}",
        "body": "${body}"
    },
    "registration_ids": [
    <#list registrationIds as registrationId>
        "${registrationId}"<#sep>,</#sep>
    </#list>
    ]
}