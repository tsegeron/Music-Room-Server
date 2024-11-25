package com.laru.db.refreshsession

import com.laru.data.model.RefreshSession
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID


class RefreshSessionDao(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<RefreshSessionDao>(RefreshSessionTable)

    var userId by RefreshSessionTable.userId
    var refreshToken by RefreshSessionTable.refreshToken
    var deviceId by RefreshSessionTable.deviceId
    var expiresIn by RefreshSessionTable.expiresIn
    var createdAt by RefreshSessionTable.createdAt
}

fun refreshSessionDaoToModel(dao: RefreshSessionDao) = RefreshSession(
    userId = dao.userId,
    refreshToken = dao.refreshToken,
    deviceId = dao.deviceId,
    expiresIn = dao.expiresIn,
    createdAt = dao.createdAt,
)
