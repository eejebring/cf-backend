package com.ejebring.cf.routes

import com.ejebring.cf.Login
import com.ejebring.cf.UserService
import com.ejebring.cf.plugins.newToken
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

suspend fun newUser(call: RoutingCall, userService: UserService) {
    val login = call.receive<Login>()
    println("User: ${login.username} ${login.passcode}")
    try {
        login.validate()
    } catch (e: IllegalArgumentException) {
        call.respond(HttpStatusCode.BadRequest, e.message ?: "Invalid user")
        return
    }

    if (userService.findByUsername(login.username) != null) {
        call.respond(HttpStatusCode.Conflict, "User already exists")
        return
    }

    val id = userService.create(login)
    val token = newToken(id, login.username)

    call.respond(HttpStatusCode.OK, token)
}