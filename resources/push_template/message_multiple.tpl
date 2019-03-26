{
    "data": {
        "title": "${title}",
        "body": "${body}"
    },
    "registration_ids": [
    <#list registrationIds as registrationId>
        <#assign ids = registrationId?split("@")>
        "${ids[1]}"<#sep>,</#sep>
    </#list>
    ]
}