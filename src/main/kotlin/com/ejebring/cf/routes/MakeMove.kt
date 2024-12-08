package com.ejebring.cf.routes

import com.ejebring.cf.GameSchema
import com.ejebring.cf.UserService
import com.ejebring.cf.plugins.getLoggedInUser
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

suspend fun makeMove(call: RoutingCall, gameSchema: GameSchema, userService: UserService) {
    val name = getLoggedInUser(call, userService)
    val gameId = call.parameters["gameId"]?.toInt() ?: throw IllegalArgumentException("Invalid gameId")
    val column = call.parameters["column"]?.toInt() ?: throw IllegalArgumentException("Invalid column")

    var game = gameSchema.getGameById(gameId)
    try {
        game.playMove(column, name)
        if (game.winner != "TBD" && game.winner != "NONE") {
            userService.winIncrement(game.winner)
        }
        gameSchema.updateGame(game, gameId)
    } catch (e: IllegalArgumentException) {
        call.respond(HttpStatusCode.BadRequest, e.message ?: "Invalid move")
        return
    }

    call.respond(HttpStatusCode.OK, game)
}