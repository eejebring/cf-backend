package com.ejebring.cf.routes

import com.ejebring.cf.*
import com.ejebring.cf.plugins.getLoggedInUser
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

suspend fun challenge(
    call: RoutingCall,
    userService: UserService,
    challengeService: ChallengeSchema,
    gameSchema: GameSchema
) {

    val challenged =
        call.parameters["username"]?.toString() ?: throw IllegalArgumentException("Invalid username")
    val challenger = getLoggedInUser(call, userService)

    if (challenger == challenged) {
        call.respond(HttpStatusCode.BadRequest, "You can't challenge yourself")
        return
    }

    if (userService.findByUsername(challenged) == null) {
        call.respond(HttpStatusCode.BadRequest, "User does not exist")
        return
    }

    if (challengeService.getUserChallenges(challenger).any { it.challenged == challenged }) {
        call.respond(HttpStatusCode.BadRequest, "You have already challenged this user")
        return
    }

    val challenge = Challenge(challenger, challenged)

    if (challengeService.getUserChallenges(challenger).any { it.challenger == challenged }) {
        challengeService.remove(challenge)
        gameSchema.create(Game(challenger, challenged))
        call.respond(HttpStatusCode.OK, "Challenge accepted")
        return
    }

    challengeService.create(challenger, challenged)
    call.respond(HttpStatusCode.Accepted, "Challenge sent")
}