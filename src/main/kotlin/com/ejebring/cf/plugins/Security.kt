package com.ejebring.cf.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import java.time.Instant

const val JWT_DOMAIN = "https://cf.ejebring.com/"
const val JWT_SECRET = "TotallyNotAvailableOnGitHub"
const val JWT_EXPIRATION: Long = 3600

fun Application.configureSecurity() {

    val jwtRealm = "WWW-Authenticate"

    authentication {
        jwt("jwt-auth") {
            realm = jwtRealm
            verifier(
                JWT
                    .require(Algorithm.HMAC256(JWT_SECRET))
                    .withIssuer(JWT_DOMAIN)
                    .build()
            )
            validate { credential ->
                if (credential.payload.issuer.contains(JWT_DOMAIN)) JWTPrincipal(credential.payload) else null
            }
        }
    }
}

fun newToken(userId: Int, username: String): String {

    return JWT.create()
        .withSubject(username)
        .withIssuer(JWT_DOMAIN)
        .withExpiresAt(Instant.now().plusSeconds(JWT_EXPIRATION))
        .sign(Algorithm.HMAC256(JWT_SECRET))
}