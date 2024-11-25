package com.laru.service.security.token

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import java.util.*

class JwtTokenService: TokenService {

    override fun generate(config: TokenConfig, vararg claims: TokenClaim): String {
        return JWT.create()
            .withAudience(config.audience)
            .withIssuer(config.issuer)
            .apply { claims.forEach { withClaim(it.name, it.value) } }
            .withExpiresAt(Date(System.currentTimeMillis() + config.expiresIn))
            .sign(Algorithm.HMAC256(config.secret))
    }
}
