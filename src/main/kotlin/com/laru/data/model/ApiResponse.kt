package com.laru.data.model

import kotlinx.serialization.Serializable


@Serializable
data class ApiResponse<T>(
    val code: Int = 200,
    val message: String? = null,
    val data: T? = null
)
