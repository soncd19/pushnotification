{
    "data": {
        "message": ${notification}
    },
    "registration_ids": [
    <#list registrationIds as registrationId>
        "${registrationId}"<#sep>,</#sep>
    </#list>
    ]
}