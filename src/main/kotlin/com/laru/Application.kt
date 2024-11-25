package com.laru

import com.laru.data.repo.PostgresPasswordResetRepository
import com.laru.data.repo.PostgresRefreshSessionRepository
import com.laru.data.repo.PostgresUserRepository
import com.laru.plugins.*
import com.laru.service.email.DefaultEmailService
import com.laru.service.email.EmailService
import com.laru.service.security.hashing.Sha256HashingService
import com.laru.service.security.token.JwtTokenService
import com.laru.service.security.token.TokenConfig
import io.ktor.server.application.*
import org.simplejavamail.api.mailer.config.TransportStrategy
import org.simplejavamail.mailer.MailerBuilder
import kotlin.time.Duration.Companion.minutes


fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    val tokenConfig = TokenConfig(
        issuer = environment.config.property("jwt.issuer").getString(),
        audience = environment.config.property("jwt.audience").getString(),
        realm = environment.config.property("jwt.realm").getString(),
        secret = environment.config.property("jwt.secret").getString(),
        expiresIn = 60.minutes.inWholeMilliseconds,
    )
    val emailFrom: String = environment.config.property("authentication.SMTPServerUsername").getString()
    val mailer = MailerBuilder
        .withSMTPServer("smtp.gmail.com", 587)
        .withTransportStrategy(TransportStrategy.SMTP_TLS)
        .withSMTPServerUsername(emailFrom)
        .withSMTPServerPassword(environment.config.property("authentication.SMTPServerPassword").getString())
        .buildMailer()
    val userRepository = PostgresUserRepository()
    val sessionRepository = PostgresRefreshSessionRepository()
    val passwordResetRepository = PostgresPasswordResetRepository()
    val jwtTokenService = JwtTokenService()
    val hashingService = Sha256HashingService()
    val emailService: EmailService = DefaultEmailService(mailer, emailFrom)


    configureSerialization()
    configureDatabases(environment.config)
    configureAuthentication(tokenConfig)
    configureRouting(
        userRepository = userRepository,
        sessionRepository = sessionRepository,
        passwordResetRepository = passwordResetRepository,
        jwtService = jwtTokenService,
        hashingService = hashingService,
        tokenConfig = tokenConfig,
        emailService = emailService
    )
}
