package com.laru.service.email


interface EmailService {
    suspend fun sendEmail(data: EmailData): Boolean
}
