package com.andriosol.andriodocpro.notification

import io.micronaut.context.annotation.Requires
import jakarta.inject.Singleton
import org.slf4j.LoggerFactory

/**
 * Dev-only stand-in for real email delivery: logs the message instead of sending it.
 * No SMTP is configured for this project — wire a real [EmailSender] implementation
 * (and disable this one) before this ever runs in production.
 */
@Singleton
@Requires(missingProperty = "email.sender.stub.enabled")
class LogEmailSender : EmailSender {

    override fun send(to: String, subject: String, body: String) {
        log.warn(
            "No email provider configured (dev only, do not use in production) — " +
                "would send to {} | subject: {} | body: {}",
            to, subject, body
        )
    }

    companion object {
        private val log = LoggerFactory.getLogger(LogEmailSender::class.java)
    }
}
