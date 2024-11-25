package com.laru.data.repo

import com.laru.data.model.RefreshSession
import java.util.*


interface RefreshSessionRepository {
    suspend fun createSession(userId: Int, refreshToken: UUID, deviceId: String)
    suspend fun addSession(session: RefreshSession)
    suspend fun updateSession(session: RefreshSession)

    suspend fun getSession(userId: Int, deviceId: String): RefreshSession?
    suspend fun getSessionsByUserId(userId: Int): List<RefreshSession>
    suspend fun getSessionsByRefreshToken(refreshToken: UUID): RefreshSession?

    suspend fun deleteSession(userId: Int, deviceId: String): Boolean
    suspend fun deleteSessionByRefreshToken(refreshToken: UUID): Boolean
    suspend fun deleteSessionsByUserId(userId: Int): Int

    suspend fun deleteAll()
}
