package com.laru.service.security.hashing


data class SaltedHash(
    val hash: String,
    val salt: String,
)
