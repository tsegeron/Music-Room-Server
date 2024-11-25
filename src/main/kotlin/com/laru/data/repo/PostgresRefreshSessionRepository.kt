package com.laru.data.repo

import com.laru.data.model.RefreshSession
import com.laru.db.suspendTransaction
import com.laru.db.refreshsession.RefreshSessionDao
import com.laru.db.refreshsession.RefreshSessionTable
import com.laru.db.refreshsession.refreshSessionDaoToModel
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.util.UUID


class PostgresRefreshSessionRepository : RefreshSessionRepository {

    override suspend fun createSession(userId: Int, refreshToken: UUID, deviceId: String)
            : Unit = suspendTransaction {
        RefreshSessionDao.new {
            this.userId = userId
            this.refreshToken = refreshToken
            this.deviceId = deviceId
        }
    }

    override suspend fun addSession(session: RefreshSession): Unit = suspendTransaction {
        RefreshSessionDao.new {
            userId = session.userId
            refreshToken = session.refreshToken
            deviceId = session.deviceId
            expiresIn = session.expiresIn
            createdAt = session.createdAt
        }
    }

    override suspend fun updateSession(session: RefreshSession): Unit = suspendTransaction {
        RefreshSessionTable
            .update({ (RefreshSessionTable.userId eq session.userId) and (RefreshSessionTable.deviceId eq session.deviceId) }) {
                it[refreshToken] = session.refreshToken
                it[createdAt] = session.createdAt
            }
    }

    override suspend fun getSessionsByUserId(userId: Int): List<RefreshSession> = suspendTransaction {
        RefreshSessionDao
            .find { RefreshSessionTable.userId eq userId }
            .map(::refreshSessionDaoToModel)
    }

    override suspend fun getSessionsByRefreshToken(refreshToken: UUID): RefreshSession? = suspendTransaction {
        RefreshSessionDao
            .find { RefreshSessionTable.refreshToken eq refreshToken }
            .limit(1)
            .map(::refreshSessionDaoToModel)
            .firstOrNull()
    }

    override suspend fun getSession(userId: Int, deviceId: String): RefreshSession? = suspendTransaction {
        RefreshSessionDao
            .find { (RefreshSessionTable.userId eq userId) and (RefreshSessionTable.deviceId eq deviceId) }
            .limit(1)
            .map(::refreshSessionDaoToModel)
            .firstOrNull()
    }

    override suspend fun deleteSessionsByUserId(userId: Int): Int = suspendTransaction {
        RefreshSessionTable.deleteWhere { RefreshSessionTable.userId eq userId }
    }

    override suspend fun deleteSession(userId: Int, deviceId: String): Boolean = suspendTransaction {
        val rowsDeleted = RefreshSessionTable.deleteWhere(limit = 1) {
            (RefreshSessionTable.userId eq userId) and (RefreshSessionTable.deviceId eq deviceId)
        }
        rowsDeleted == 1
    }

    override suspend fun deleteSessionByRefreshToken(refreshToken: UUID): Boolean = suspendTransaction {
        RefreshSessionTable.deleteWhere { RefreshSessionTable.refreshToken eq refreshToken } == 1
    }

    override suspend fun deleteAll(): Unit = suspendTransaction {
        RefreshSessionTable.deleteAll()
    }
}
