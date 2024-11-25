package com.laru.data.model

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.util.UUID


@Serializable
data class RefreshSession(
    val userId: Int,
    @Contextual val refreshToken: UUID,
    val deviceId: String,
    val expiresIn: Long = 2_592_000_000L, // 30 days
    val createdAt: Long = System.currentTimeMillis(),
)
