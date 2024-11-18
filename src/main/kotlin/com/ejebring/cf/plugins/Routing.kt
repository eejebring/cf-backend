package com.ejebring.cf.plugins

import com.ejebring.cf.Login
import com.ejebring.cf.TokenService
import com.ejebring.cf.User
import com.ejebring.cf.UserService
import io.ktor.http.*
import io.ktor.serialization.gson.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.resources.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.Database

fun Application.configureRouting() {
    install(Resources)
    install(ContentNegotiation) {
        gson {
        }
        json()
    }

    val database = Database.connect(
        url = "jdbc:sqlite:sample.db",
        driver = "org.sqlite.JDBC",
    )
    val userService = UserService(database)

    routing {
        get("/users") {
            call.respond("hi")//userService.readAll())
        }
        post("/user") {
            val login = call.receive<Login>()
            println("User: ${login.username} ${login.passcode}")
            try {
                login.validate()
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, e.message ?: "Invalid user")
                return@post
            }

            if (userService.findByUsername(login.username) != null) {
                call.respond(HttpStatusCode.Conflict, "User already exists")
                return@post
            }

            val id = userService.create(User(login))
            val token = TokenService.newToken(id, login.username)

            call.respond(HttpStatusCode.OK, token)
        }
        post("/login") {
            val login = call.receive<Login>()
            println("Login: ${login.username} ${login.passcode}")
            try {
                login.validate()
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, e.message ?: "Invalid user")
                return@post
            }

            val user = userService.findByUsername(login.username)

            if (user == null || user.passcode != login.passcode) {
                call.respond(HttpStatusCode.Unauthorized, "Incorrect username or password")
                return@post
            }

            val token = TokenService.newToken(user.id!!, user.name)
            call.respond(HttpStatusCode.OK, token)
        }
    }
}
