package com.laru.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.laru.service.security.token.TokenConfig
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*


fun Application.configureAuthentication(tokenConfig: TokenConfig) {

    install(Authentication) {
        jwt("auth-jwt") {
            realm = tokenConfig.realm
            verifier(JWT
                .require(Algorithm.HMAC256(tokenConfig.secret))
                .withAudience(tokenConfig.audience)
                .withIssuer(tokenConfig.issuer)
                .build())

            validate { credential ->
                if (credential.payload.getClaim("email").asString() != "") {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }

            challenge { defaultScheme, realm ->
                call.respond(HttpStatusCode.Unauthorized, "Token is not valid or has expired")
            }
        }
    }
}
