package com.laru.db.passwordreset

import com.laru.data.model.PasswordReset
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID


class PasswordResetDao(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<PasswordResetDao>(PasswordResetTable)

    var userId by PasswordResetTable.userId
    var resetToken by PasswordResetTable.resetToken
    var expiresAt by PasswordResetTable.expiresAt
    var used by PasswordResetTable.used
}

fun passwordResetDaoToModel(dao: PasswordResetDao) = PasswordReset(
    userId = dao.userId,
    resetToken = dao.resetToken,
    expiresAt = dao.expiresAt,
    used = dao.used,
)
