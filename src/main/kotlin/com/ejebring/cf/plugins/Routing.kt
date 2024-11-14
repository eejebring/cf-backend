package com.ejebring.cf.plugins

import io.ktor.http.HttpStatusCode
import io.ktor.resources.*
import io.ktor.server.application.*
import io.ktor.server.request.receive
import io.ktor.server.resources.*
import io.ktor.server.resources.Resources
import io.ktor.server.resources.post
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable

fun Application.configureRouting() {
    install(Resources)
    routing {
        get("/") {
            call.respondText("Hello World!")
        }
        get<Articles> { article ->
            // Get all articles ...
            call.respond("List of articles sorted starting from ${article.sort}")
        }
        post<Articles> { article ->
            val x = call.receive<Articles>()

            if (x.limit < 0) {
                call.respondText("Limit must be a positive integer", status = HttpStatusCode.BadRequest)
                return@post
            }

            call.respondText("New article created with limit ${x.limit}")
        }
    }
}

@Serializable
@Resource("/articles")
data class Articles(val sort: String? = "new", val limit: Int = -1)
