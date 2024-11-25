package com.laru.data.model

import kotlinx.serialization.Serializable


@Serializable
data class PasswordReset(
    val userId: Int,
    val resetToken: String,
    val expiresAt: Long,
    val used: Boolean = false
)
