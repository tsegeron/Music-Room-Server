package com.laru.feature.auth.repo

import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import com.google.api.client.http.apache.v2.ApacheHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.laru.data.model.ApiResponse
import com.laru.data.model.User
import com.laru.data.repo.PasswordResetRepository
import com.laru.data.repo.RefreshSessionRepository
import com.laru.data.repo.UserRepository
import com.laru.feature.auth.model.*
import com.laru.service.email.EmailData
import com.laru.service.email.EmailService
import com.laru.service.security.hashing.HashingService
import com.laru.service.security.hashing.SaltedHash
import com.laru.service.security.token.TokenClaim
import com.laru.service.security.token.TokenConfig
import com.laru.service.security.token.TokenService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import kotlinx.serialization.Serializable
import java.util.*
import kotlin.time.Duration.Companion.minutes


class AuthControllerImpl(
    private val userRepository: UserRepository,
    private val sessionRepository: RefreshSessionRepository,
    private val passwordResetRepository: PasswordResetRepository,
    private val jwtService: TokenService,
    private val hashingService: HashingService,
    private val emailService: EmailService,
    private val tokenConfig: TokenConfig,
): AuthController {

    /**
     * Performs registration and login
     */
    override suspend fun performSignUp(call: ApplicationCall) {
        val receive = try {
            call.receive<SignUpRequest>()
        } catch (e: Exception) {
            call.respond(HttpStatusCode.BadRequest, ApiResponse(HttpStatusCode.BadRequest.value, "Bad Request", null))
            return
        }

        if (!isCredentialsValid(receive.email, receive.password)) {
            call.respond(HttpStatusCode.Conflict, ApiResponse(HttpStatusCode.Conflict.value, "Invalid email/password", null))
            return
        }

        val userDb = userRepository.getUserByEmail(receive.email)
        if (userDb != null) {
            call.respond(HttpStatusCode.Conflict, ApiResponse(HttpStatusCode.Conflict.value, "User already exists", null))
            return
        }

        val saltedHash = hashingService.generateSaltedHash(receive.password)
        val newUserId: Int? = userRepository.addUser(User(0, receive.username, receive.email, saltedHash.hash, saltedHash.salt))
        if (newUserId == null) {
            call.respond(HttpStatusCode.InternalServerError, ApiResponse(HttpStatusCode.InternalServerError.value, "User wasn't created in the database", null))
            return
        }

        val accessToken = jwtService.generate(tokenConfig, TokenClaim("email", receive.email))
        val refreshToken = UUID.randomUUID()
        sessionRepository.createSession(newUserId, refreshToken, receive.deviceId)
        call.respond(ApiResponse(HttpStatusCode.Created.value, null, AuthResponse(accessToken, refreshToken.toString())))
    }

    /**
     * Performs login via email and password
     */
    override suspend fun performSignIn(call: ApplicationCall) {
        val receive = try {
            call.receive<SignInRequest>()
        } catch (e: Exception) {
            call.respond(HttpStatusCode.BadRequest, ApiResponse(HttpStatusCode.BadRequest.value, "Bad Request", null))
            return
        }

        val userDb = userRepository.getUserByEmail(receive.email)
        if (userDb == null) {
            call.respond(HttpStatusCode.NotFound, ApiResponse(HttpStatusCode.NotFound.value, "User Not Registered", null))
            return
        }
        if (userDb.password == null || userDb.salt == null) {
            call.respond(HttpStatusCode.Forbidden, ApiResponse(HttpStatusCode.Forbidden.value, "Not allowed, try using Google authorization", null))
            return
        }

        val isValidPassword = hashingService.verify(receive.password, SaltedHash(userDb.password, userDb.salt))
        if (!isValidPassword) {
            call.respond(HttpStatusCode.BadRequest, ApiResponse(HttpStatusCode.BadRequest.value, "Incorrect password", null))
            return
        }

        val accessToken = jwtService.generate(tokenConfig, TokenClaim("email", receive.email))
        val refreshToken = UUID.randomUUID()
        createSession(userDb.id, refreshToken, receive.deviceId)
        call.respond(ApiResponse(HttpStatusCode.OK.value, null, AuthResponse(accessToken, refreshToken.toString())))
    }

    /**
     * Performs signin/signup using google idToken
     *
     * @param audience CLIENT_ID of the app from Google Cloud
     *
     * @see: CLIENT_ID is obtained in <a href="https://console.cloud.google.com">Google Cloud<a/>
     * @see: Google's guide for <a href="https://developer.android.com/identity/sign-in/credential-manager-siwg">users authentication with Google<a/>
     */
    override suspend fun performAuthWithGoogle(call: ApplicationCall, audience: String) {
        val clientAccessToken = call.request.headers[HttpHeaders.Authorization]?.substringAfter("Bearer ")
        if (clientAccessToken == null) {
            call.respond(HttpStatusCode.BadRequest, ApiResponse(HttpStatusCode.BadRequest.value, "Client access token is missing", null))
            return
        }

        val receive = try {
            call.receive<SignInGoogleRequest>()
        } catch (e: Exception) {
            call.respond(HttpStatusCode.BadRequest, ApiResponse(HttpStatusCode.BadRequest.value, "Bad Request", null))
            return
        }

        val transport = ApacheHttpTransport()
        val factory = GsonFactory.getDefaultInstance()
        val verifier = GoogleIdTokenVerifier.Builder(transport, factory)
            .setAudience(listOf(audience))
            .build()

        val idToken = try {
            verifier.verify(clientAccessToken) ?: throw BadRequestException("Invalid token")
        } catch (e: Exception) {
            call.respond(HttpStatusCode.BadRequest, ApiResponse(HttpStatusCode.BadRequest.value, e.message ?:"Invalid Token", null))
            return
        }

        val payload = idToken.payload

        var userDbId = userRepository.getUserByEmail(payload.email)?.id
        if (userDbId == null) { // if user is not registered
            val name = payload["name"]?.toString() ?: payload.email.substringBefore("@")
//            val pictureUrl = payload["picture"]?.toString() // TODO manage profile pic
            userDbId = userRepository.addUser(User(0, name, payload.email, null, null))
            if (userDbId == null) {
                call.respond(HttpStatusCode.InternalServerError, ApiResponse(HttpStatusCode.InternalServerError.value, "User wasn't created in the Database", null))
                return
            }
        }

        val accessToken = jwtService.generate(tokenConfig, TokenClaim("email", payload.email))
        val refreshToken = UUID.randomUUID()
        createSession(userDbId, refreshToken, receive.deviceId)
        call.respond(ApiResponse(HttpStatusCode.OK.value, null, AuthResponse(accessToken, refreshToken.toString())))
    }

    /**
     * Sends instructions (link to the app with resetToken) to the provided mail address
     * Sets a new password
     */
    @Serializable
    data class PasswordRestoreEmailRequest(
        val email: String
    )
    @Serializable
    data class PasswordRestoreRequest(
        val newPassword: String
    )
    override suspend fun performPasswordReset(call: ApplicationCall) {
        val token = call.request.queryParameters["reset_token"]
        if (token == null) {
            sendInstructionsToEmail(call)
        } else {
            val receive = try {
                call.receive<PasswordRestoreRequest>()
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ApiResponse(HttpStatusCode.BadRequest.value, "No password", null)
                )
                return
            }
            // TODO Check new password

            val resetSession = passwordResetRepository.getSessionByToken(token) // TODO check session
            if (resetSession == null || resetSession.used || resetSession.expiresAt < System.currentTimeMillis()) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ApiResponse(HttpStatusCode.BadRequest.value, "Session expired", null)
                )
                return
            }

            val userDb = userRepository.getUserById(resetSession.userId)
            if (userDb == null) {
                call.respond(HttpStatusCode.Conflict, ApiResponse(HttpStatusCode.Conflict.value, "User does not exist", null))
                return
            }

            val saltedHash = hashingService.generateSaltedHash(receive.newPassword)
            sessionRepository.deleteSessionsByUserId(userDb.id)
            userRepository.updateUser(User(0, userDb.username, userDb.email, saltedHash.hash, saltedHash.salt)) // TODO refactor
//            passwordResetRepository.deleteSessionsByUserId(userDb.id) // TODO refactor (commented for the test purposes)
            call.respond(HttpStatusCode.OK, ApiResponse(HttpStatusCode.OK.value, "Password was updated", null))
        }
    }

    private suspend fun sendInstructionsToEmail(call: ApplicationCall) {
        val receive = try {
            call.receive<PasswordRestoreEmailRequest>()
        } catch (e: Exception) {
            call.respond(
                HttpStatusCode.BadRequest,
                ApiResponse(HttpStatusCode.BadRequest.value, "Bad Request", null)
            )
            return
        }

        val userDb = userRepository.getUserByEmail(receive.email)
        val result = userDb?.run {
            passwordResetRepository.deleteSessionsByUserId(userDb.id)
            val resetToken = UUID.randomUUID().toString()
            val expiresAt = System.currentTimeMillis() + 15.minutes.inWholeMilliseconds
            passwordResetRepository.createSession(userDb.id, resetToken, expiresAt)
            val restorationLink = "mroom://music-room/reset-password/$resetToken"


            emailService.sendEmail(
                EmailData(
                    emailTo = receive.email,
                    subject = "Password Restoration",
                    body = "To restore the password follow the link $restorationLink"
                )
            ) } ?: true

        if (result) {
            call.respond(HttpStatusCode.OK, ApiResponse(HttpStatusCode.OK.value, "Instructions sent", null))
        } else {
            call.respond(
                HttpStatusCode.InternalServerError,
                ApiResponse(HttpStatusCode.InternalServerError.value, "Could not send email", null)
            )
        }
    }

    /**
     * Performs logout, deletes sessionToken (i.e. refreshToken)
     */
    override suspend fun performLogout(call: ApplicationCall) {
        val receive = try {
            call.receive<LogoutRequest>()
        } catch (e: Exception) {
            call.respond(HttpStatusCode.BadRequest, ApiResponse(HttpStatusCode.BadRequest.value, "Bad Request", null))
            return
        }

        sessionRepository.deleteSessionByRefreshToken(UUID.fromString(receive.refreshToken))
        call.respond(ApiResponse(HttpStatusCode.OK.value, "Successfully logged out", null))
    }

    /**
     * Provides the new pair of accessToken and refreshToken (i.e. sessionToken)
     */
    override suspend fun refreshToken(call: ApplicationCall) {
        val receive = try {
            call.receive<RefreshRequest>()
        } catch (e: Exception) {
            call.respond(HttpStatusCode.BadRequest, ApiResponse(HttpStatusCode.BadRequest.value, "Bad Request", null))
            return
        }

        val session = sessionRepository.getSessionsByRefreshToken(UUID.fromString(receive.refreshToken))
        if (session == null) {
            call.respond(HttpStatusCode.BadRequest, ApiResponse(HttpStatusCode.BadRequest.value, "refresh_token is invalid", null))
            return
        }

        val user = userRepository.getUserById(session.userId) // cant be null (if null => unregistered)
        if (user == null) {
            call.respond(HttpStatusCode.InternalServerError, ApiResponse(HttpStatusCode.InternalServerError.value, "User Not Found", null))
            return
        }

        val newAccessToken = jwtService.generate(tokenConfig, TokenClaim("email", user.email))
        val newRefreshToken = UUID.randomUUID()
        sessionRepository.updateSession(session.copy(refreshToken = newRefreshToken, createdAt = System.currentTimeMillis()))
        call.respond(ApiResponse(HttpStatusCode.OK.value, null, AuthResponse(newAccessToken, newRefreshToken.toString())))
    }


    /**
     * Checks if the email and password are valid
     *
     * Email is valid if:
     *  1.
     * Password is valid if:
     *  1.
     */
    // TODO implement proper email/password verification ----------------------
    private fun isCredentialsValid(email: String, password: String): Boolean {
        val areFieldsBlank = email.isBlank() || password.isBlank()
        val isPwTooShort = password.length < 8

//        if (areFieldsBlank || isPwTooShort) {
//            return false
//        }
//        return true
        return !(areFieldsBlank || isPwTooShort)
    }

    /**
     * Creates a session if the session on the given [deviceId] is not registered,
     * otherwise updates its refreshToken and createdAt
     *
     * If the number of sessions exceeds [MAX_SESSIONS_ALLOWED], replaces all previous sessions with one new one
     */
    private suspend fun createSession(userId: Int, refreshToken: UUID, deviceId: String) {
        val allSessions = sessionRepository.getSessionsByUserId(userId)
        if (allSessions.size <= MAX_SESSIONS_ALLOWED) {
            val prevSession = allSessions.find { it.userId == userId && it.deviceId == deviceId }

            if (prevSession == null) {
                sessionRepository.createSession(userId, refreshToken, deviceId)
            } else {
                sessionRepository.updateSession(prevSession.copy(refreshToken = refreshToken, createdAt = System.currentTimeMillis()))
            }
        } else {
            sessionRepository.deleteSessionsByUserId(userId)
            sessionRepository.createSession(userId, refreshToken, deviceId)
        }
    }

    companion object {
        const val MAX_SESSIONS_ALLOWED = 5
    }
}
