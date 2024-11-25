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
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import org.simplejavamail.api.mailer.config.TransportStrategy
import org.simplejavamail.mailer.MailerBuilder
import kotlin.test.*
import kotlin.time.Duration.Companion.minutes

class ApplicationTest {
    @Test
    fun testRoot() = testApplication {
        val tokenConfig = TokenConfig("issuer", "audience", "realm", "secret", 60.minutes.inWholeMilliseconds,)
        val emailFrom: String = "emailFrom"
        val emailFromPassword: String = "emailFromPassword"
        val mailer = MailerBuilder
            .withSMTPServer("smtp.gmail.com", 587)
            .withTransportStrategy(TransportStrategy.SMTP_TLS)
            .withSMTPServerUsername(emailFrom)
            .withSMTPServerPassword(emailFromPassword)
            .buildMailer()
        val userRepository = PostgresUserRepository()
        val sessionRepository = PostgresRefreshSessionRepository()
        val passwordResetRepository = PostgresPasswordResetRepository()
        val jwtTokenService = JwtTokenService()
        val hashingService = Sha256HashingService()
        val emailService: EmailService = DefaultEmailService(mailer, emailFrom)

        application {
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
        client.get("/").apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals("Hello World!", bodyAsText())
        }
    }
}
