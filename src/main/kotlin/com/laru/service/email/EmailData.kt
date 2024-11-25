package com.laru.service.email


data class EmailData(
    val emailTo: String,
    val subject: String,
    val body: String
)
