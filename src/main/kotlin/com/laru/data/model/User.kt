package com.laru.data.model

import kotlinx.serialization.Serializable


@Serializable
data class User(
    val id: Int = 0,
    val username: String,
    val email: String,
    val password: String?,
    val salt: String?,
)
