package com.laru.db.user

import com.laru.data.model.User
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID


class UserDao(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<UserDao>(UserTable)

    var username by UserTable.username
    var email by UserTable.email
    var password by UserTable.password
    var salt by UserTable.salt
}

fun userDaoToModel(dao: UserDao) = User(
    id = dao.id.value,
    username = dao.username,
    email = dao.email,
    password = dao.password,
    salt = dao.salt
)
