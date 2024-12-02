package com.ejebring.cf.plugins

import com.ejebring.cf.User
import com.ejebring.cf.UserService
import com.ejebring.cf.routes.getUser
import com.ejebring.cf.routes.newUser
import io.ktor.http.*
import io.ktor.serialization.gson.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.resources.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import login
import org.jetbrains.exposed.sql.Database

@Serializable
data class UserOutputObject(val id: Int, val name: String, val wins: Int, val updatedAt: String) {
    constructor(user: User) : this(user.id, user.name, user.wins, user.updatedAt.toString())
}

fun Application.configureRouting() {
    install(Resources)
    install(ContentNegotiation) {
        gson {
        }
        json()
    }

    val database = Database.connect(
        url = "jdbc:sqlite:game.db",
        driver = "org.sqlite.JDBC",
    )
    val userService = UserService(database)

    routing {
        get("/users") {
            val users = userService.allUsers().map { UserOutputObject(it) }
            call.respond(HttpStatusCode.OK, users)
        }
        get("/user/{id}") {
            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
            getUser(call, userService, id)
        }
        post("/user") {
            newUser(call, userService)
        }
        post("/login") {
            login(call, userService)
        }

        authenticate("jwt-auth") {
            get("/games") {}
            get("/game/{id}") {}
            get("/challenges") {}
            post("/challenge/{id}") {}
            post("/move/{gameId}/{column}") {}
        }
    }
}