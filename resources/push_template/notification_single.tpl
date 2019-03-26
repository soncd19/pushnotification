{
    <#assign ids = registrationId?split("@")>
    "to":"${ids[1]}",
    "data": {
        "message": "${message}"
    }
}