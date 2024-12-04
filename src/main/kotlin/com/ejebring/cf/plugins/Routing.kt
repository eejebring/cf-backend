package com.ejebring.cf.plugins

import com.ejebring.cf.ChallengeSchema
import com.ejebring.cf.User
import com.ejebring.cf.UserService
import com.ejebring.cf.routes.challenge
import com.ejebring.cf.routes.getUser
import com.ejebring.cf.routes.newUser
import io.ktor.http.*
import io.ktor.serialization.gson.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.resources.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import login
import org.jetbrains.exposed.sql.Database

@Serializable
data class UserOutputObject(val name: String, val wins: Int, val updatedAt: String) {
    constructor(user: User) : this(user.name, user.wins, user.updatedAt.toString())
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
    val challengeService = ChallengeSchema(database)

    routing {
        get("/users") {
            val users = userService.allUsers().map { UserOutputObject(it) }
            call.respond(HttpStatusCode.OK, users)
        }
        get("/user/{username}") {
            val name = call.parameters["username"]?.toString() ?: throw IllegalArgumentException("Invalid username")
            getUser(call, userService, name)
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
            get("/challenges") {
                val name = call.principal<JWTPrincipal>()!!.payload.subject!!
                call.respond(HttpStatusCode.OK, challengeService.getUserChallenges(name))
            }
            post("/challenge/{username}") {
                challenge(call, userService, challengeService)
            }
            post("/move/{gameId}/{column}") {}
        }
    }
}