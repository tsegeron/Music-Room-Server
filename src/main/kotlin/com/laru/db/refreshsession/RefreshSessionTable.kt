package com.laru.db.refreshsession

import com.laru.db.user.UserTable
import org.jetbrains.exposed.dao.id.IntIdTable


object RefreshSessionTable : IntIdTable(name = "refresh_session") {
    val userId = integer("user_id").references(UserTable.id)
    val refreshToken = uuid("refresh_token")
    val deviceId = varchar("device_id", 128)
    val expiresIn = long("expires_in").default(2_592_000_000L) // 30 days
    val createdAt = long("created_at").clientDefault { System.currentTimeMillis() }
}
