package com.laru.db.user

import org.jetbrains.exposed.dao.id.IntIdTable

object UserTable : IntIdTable("user") {
    val username = varchar("username", 255)
    val email = varchar("email", 255)
    val password = varchar("password", 255).nullable() // null if registered via Google
    val salt = varchar("salt", 64).nullable() // null if registered via Google
}
