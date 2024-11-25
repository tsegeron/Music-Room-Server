package com.laru.plugins

import com.laru.data.repo.PasswordResetRepository
import com.laru.data.repo.RefreshSessionRepository
import com.laru.data.repo.UserRepository
import com.laru.feature.auth.repo.AuthControllerImpl
import com.laru.feature.auth.authRouting
import com.laru.service.email.DefaultEmailService
import com.laru.service.email.EmailService
import com.laru.service.security.hashing.HashingService
import com.laru.service.security.token.TokenConfig
import com.laru.service.security.token.TokenService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.simplejavamail.api.mailer.config.TransportStrategy
import org.simplejavamail.mailer.MailerBuilder

fun Application.configureRouting(
    userRepository: UserRepository,
    sessionRepository: RefreshSessionRepository,
    passwordResetRepository: PasswordResetRepository,
    jwtService: TokenService,
    hashingService: HashingService,
    tokenConfig: TokenConfig,
    emailService: EmailService
) {
    val authController = AuthControllerImpl(
        userRepository = userRepository,
        sessionRepository = sessionRepository,
        passwordResetRepository = passwordResetRepository,
        jwtService = jwtService,
        hashingService = hashingService,
        emailService = emailService,
        tokenConfig = tokenConfig
    )

    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.respondText(text = "500: $cause", status = HttpStatusCode.InternalServerError)
        }
    }

    routing {
        staticResources("/.well-known", "static/well-known", index = null)

        authRouting(authController)
    }
}
