package com.laru.feature.auth.model

import kotlinx.serialization.Serializable


@Serializable
data class LogoutRequest(
    val refreshToken: String
)
