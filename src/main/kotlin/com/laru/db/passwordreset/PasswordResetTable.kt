package com.laru.db.passwordreset

import com.laru.db.user.UserTable
import org.jetbrains.exposed.dao.id.IntIdTable


object PasswordResetTable : IntIdTable(name = "password_reset") {
    val userId = integer("user_id").references(UserTable.id)
    val resetToken = varchar("reset_token", 255).uniqueIndex()
    val expiresAt = long("expires_at")
    val used = bool("used").default(false)
}
