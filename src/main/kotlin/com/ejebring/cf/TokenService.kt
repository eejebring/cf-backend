package com.ejebring.cf

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm

object TokenService {

    const val JWT_DOMAIN = "https://cf.ejebring.com/"
    const val JWT_SECRET = "TotallyNotAvailableOnGitHub"

    fun newToken(userId: Int, username: String): String {

        return JWT.create()
            .withClaim("userId", userId)
            .withClaim("username", username)
            .withIssuer(JWT_DOMAIN)
            .sign(Algorithm.HMAC256(JWT_SECRET))
    }

    fun validate(token: String): Int {

        val verifier = JWT.require(Algorithm.HMAC256(JWT_SECRET))
            .withIssuer(JWT_DOMAIN)
            .build()

        val jwt = verifier.verify(token)

        return jwt.getClaim("userId").asInt()
    }
}