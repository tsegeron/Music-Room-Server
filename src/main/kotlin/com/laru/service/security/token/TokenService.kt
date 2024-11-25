package com.laru.service.security.token

interface TokenService {

    fun generate(config: TokenConfig, vararg claims: TokenClaim): String
}
