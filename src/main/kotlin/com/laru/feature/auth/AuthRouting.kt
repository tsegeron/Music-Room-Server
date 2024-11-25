package com.laru.feature.auth

import com.laru.data.model.ApiResponse
import com.laru.feature.auth.repo.AuthController
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*


fun Route.authRouting(authController: AuthController) {

    route("/auth") {

        post("/signup") {
            authController.performSignUp(call)
        }

        post("/signin/google") {
            val audience = environment.config.property("authentication.GOOGLE_CLIENT_ID").getString()
            authController.performAuthWithGoogle(call, audience)
        }

        post("/signin") {
            authController.performSignIn(call)
        }

        post("/reset-password") {
            authController.performPasswordReset(call)
        }

        post("/logout") {
            authController.performLogout(call)
        }

        post("/refresh") {
            authController.refreshToken(call)
        }

        authenticate("auth-jwt") {
            get { // TODO refactor as isLoggedIn()
                val principal = call.principal<JWTPrincipal>()
                val email = principal!!.payload.getClaim("email").asString()

                call.respond(ApiResponse(HttpStatusCode.OK.value, "Authorized as $email", null))
            }
        }
    }
}
