package com.ejebring.cf.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.ejebring.cf.JWT_DOMAIN
import com.ejebring.cf.JWT_SECRET
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*

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
