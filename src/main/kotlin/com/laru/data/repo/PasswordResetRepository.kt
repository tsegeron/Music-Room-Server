package com.laru.data.repo

import com.laru.data.model.PasswordReset


interface PasswordResetRepository {
    suspend fun createSession(userId: Int, resetToken: String, expiresAt: Long)
    suspend fun updateSession(session: PasswordReset)

    suspend fun getSessionByUserId(userId: Int): PasswordReset?
    suspend fun getSessionByToken(resetToken: String): PasswordReset?

    suspend fun deleteSession(userId: Int): Boolean
    suspend fun deleteSessionsByUserId(userId: Int): Int
    suspend fun deleteSessionByToken(resetToken: String): Boolean

    suspend fun deleteAll()
}
