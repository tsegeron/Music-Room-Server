package com.laru.feature.auth.model

import kotlinx.serialization.Serializable


@Serializable
data class SignInRequest(
    val email: String,
    val password: String,
    val deviceId: String,
)
