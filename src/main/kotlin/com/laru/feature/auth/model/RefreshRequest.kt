package com.laru.feature.auth.model

import kotlinx.serialization.Serializable


@Serializable
data class RefreshRequest(
    val accessToken: String,
    val refreshToken: String
)
