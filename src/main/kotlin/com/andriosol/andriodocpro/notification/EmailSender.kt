package com.andriosol.andriodocpro.notification

/** Sends transactional emails (password resets, document-share invitations, ...). */
interface EmailSender {
    fun send(to: String, subject: String, body: String)
}
