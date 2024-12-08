package com.ejebring.cf.plugins

import com.ejebring.cf.ChallengeSchema
import com.ejebring.cf.GameSchema
import com.ejebring.cf.User
import com.ejebring.cf.UserService
import com.ejebring.cf.routes.challenge
import com.ejebring.cf.routes.getUser
import com.ejebring.cf.routes.makeMove
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
    val gameSchema = GameSchema(database)

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
            get("/games") {
                val name = getLoggedInUser(call, userService)
                call.respond(HttpStatusCode.OK, gameSchema.getUserGames(name))
            }
            get("/game/{id}") {
                val name = getLoggedInUser(call, userService)
                val gameId = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid gameId")

                if (!gameSchema.getUserGames(name).contains(gameId)) {
                    call.respond(HttpStatusCode.BadRequest, "You are not a player in this game")
                    return@get
                }
                call.respond(HttpStatusCode.OK, gameSchema.getGameById(gameId))
            }
            get("/challenges") {
                val name = getLoggedInUser(call, userService)
                call.respond(HttpStatusCode.OK, challengeService.getUserChallenges(name))
            }
            post("/challenge/{username}") {
                challenge(call, userService, challengeService, gameSchema)
            }
            post("/move/{gameId}/{column}") {
                makeMove(call, gameSchema, userService)
            }
        }
    }
}