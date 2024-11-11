package com.ejebring.cf.plugins

import io.ktor.server.application.*
import io.ktor.server.plugins.openapi.*
import io.ktor.server.plugins.swagger.*
import io.ktor.server.routing.*

fun Application.configureHTTP() {
    routing {
        openAPI(path = "openapi", swaggerFile = "openapi/documentation.yaml")
    }
    routing {
        swaggerUI(path = "openapi", swaggerFile = "openapi/documentation.yaml")
    }
}
