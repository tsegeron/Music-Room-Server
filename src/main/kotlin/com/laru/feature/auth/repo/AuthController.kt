package com.laru.feature.auth.repo

import io.ktor.server.application.*

interface AuthController {
    suspend fun performSignUp(call: ApplicationCall)
    suspend fun performSignIn(call: ApplicationCall)
    suspend fun performAuthWithGoogle(call: ApplicationCall, audience: String)
    suspend fun performPasswordReset(call: ApplicationCall)
    suspend fun performLogout(call: ApplicationCall)
    suspend fun refreshToken(call: ApplicationCall)
}
