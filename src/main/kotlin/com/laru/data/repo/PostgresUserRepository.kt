package com.laru.data.repo

import com.laru.data.model.User
import com.laru.db.user.UserDao
import com.laru.db.user.userDaoToModel
import com.laru.db.suspendTransaction
import com.laru.db.user.UserTable
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.update


class PostgresUserRepository: UserRepository {
    override suspend fun addUser(user: User): Int? = suspendTransaction {
        try {
            UserTable.insertAndGetId {
                it[username] = user.username
                it[email] = user.email
                it[password] = user.password
                it[salt] = user.salt
            }.value
        } catch (e: ExposedSQLException) {
            // TODO provide logging
            null
        }
    }

    override suspend fun updateUser(user: User): Unit = suspendTransaction {
        UserTable.update({ UserTable.email eq user.email }) { // NOTE add changing username?
            it[password] = user.password
            it[salt] = user.salt
        }
    }

    override suspend fun deleteUser(email: String): Boolean = suspendTransaction {
        val rowsDeleted = UserTable.deleteWhere {
            UserTable.email eq email
        }
        rowsDeleted == 1
    }

    override suspend fun getUserById(id: Int): User? = suspendTransaction {
        UserDao.findById(id)?.let(::userDaoToModel)
    }

    override suspend fun getUserByEmail(email: String): User? = suspendTransaction {
        UserDao
            .find { (UserTable.email eq email) }
            .limit(1)
            .map(::userDaoToModel)
            .firstOrNull()
    }
}
