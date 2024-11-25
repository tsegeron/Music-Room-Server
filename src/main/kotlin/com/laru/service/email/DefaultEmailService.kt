package com.laru.service.email

import org.simplejavamail.MailException
import org.simplejavamail.api.mailer.Mailer
import org.simplejavamail.email.EmailBuilder


class DefaultEmailService(
    private val mailer: Mailer,
    private val emailFrom: String
): EmailService {
    override suspend fun sendEmail(data: EmailData): Boolean {
        val username = data.emailTo.substringBefore("@")
        val email = EmailBuilder.startingBlank()
            .from("Music Room", emailFrom)
            .to(username, data.emailTo)
            .withSubject(data.subject)
            .withPlainText(data.body)
            .buildEmail()

        return try {
            mailer.sendMail(email)
            true
        } catch (e: MailException) {
            e.printStackTrace()
            false
        }
    }
}
