{
    <#assign ids = registrationId?split("@")>
    "to":"${ids[1]}",
    "data": {
        "title": "${title}",
        "body": "${body}"
    }
}
