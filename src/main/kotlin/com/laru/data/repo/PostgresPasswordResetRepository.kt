package com.laru.data.repo

import com.laru.data.model.PasswordReset
import com.laru.db.passwordreset.PasswordResetDao
import com.laru.db.passwordreset.PasswordResetTable
import com.laru.db.passwordreset.passwordResetDaoToModel
import com.laru.db.suspendTransaction
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.update


class PostgresPasswordResetRepository: PasswordResetRepository {
    override suspend fun createSession(userId: Int, resetToken: String, expiresAt: Long): Unit = suspendTransaction {
        PasswordResetDao.new {
            this.userId = userId
            this.resetToken = resetToken
            this.expiresAt = expiresAt
        }
    }

    override suspend fun updateSession(session: PasswordReset): Unit = suspendTransaction {
        PasswordResetTable
            .update(
                where = { PasswordResetTable.userId eq session.userId },
                limit = 1,
                body = {
                    it[resetToken] = session.resetToken
                    it[expiresAt] = session.expiresAt
                    it[used] = session.used
                }
            )
    }

    override suspend fun getSessionByUserId(userId: Int): PasswordReset? = suspendTransaction {
        PasswordResetDao
            .find { PasswordResetTable.userId eq userId }
            .limit(1)
            .map(::passwordResetDaoToModel)
            .firstOrNull()
    }

    override suspend fun getSessionByToken(resetToken: String): PasswordReset? = suspendTransaction {
        PasswordResetDao
            .find { PasswordResetTable.resetToken eq resetToken }
            .limit(1)
            .map(::passwordResetDaoToModel)
            .firstOrNull()
    }

    override suspend fun deleteSession(userId: Int): Boolean = suspendTransaction {
        val rowDeleted = PasswordResetTable.deleteWhere(limit = 1) {
            PasswordResetTable.userId eq userId
        }
        rowDeleted == 1
    }

    override suspend fun deleteSessionsByUserId(userId: Int): Int = suspendTransaction {
        PasswordResetTable.deleteWhere { PasswordResetTable.userId eq userId }
    }

    override suspend fun deleteSessionByToken(resetToken: String): Boolean = suspendTransaction {
        val rowDeleted = PasswordResetTable.deleteWhere(limit = 1) {
            PasswordResetTable.resetToken eq resetToken
        }
        rowDeleted == 1
    }

    override suspend fun deleteAll(): Unit = suspendTransaction {
        PasswordResetTable.deleteAll()
    }
}
