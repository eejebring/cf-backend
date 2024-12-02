package com.ejebring.cf.routes

import com.ejebring.cf.UserService
import com.ejebring.cf.plugins.UserOutputObject
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

suspend fun getUser(call: RoutingCall, userService: UserService, id: Int) {
    val user = userService.findById(id)
    if (user != null) {
        call.respond(
            HttpStatusCode.OK,
            UserOutputObject(user)
        )
    } else {
        call.respond(HttpStatusCode.NotFound)
    }
}