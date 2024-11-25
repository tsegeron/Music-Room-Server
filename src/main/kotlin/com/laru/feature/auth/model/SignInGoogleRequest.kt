package com.laru.feature.auth.model

import kotlinx.serialization.Serializable


@Serializable
data class SignInGoogleRequest(
    val deviceId: String
)
