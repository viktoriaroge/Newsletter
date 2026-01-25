package com.viroge.newsletter.domain.email

class NoOpEmailSender : EmailSender {
    override suspend fun sendWelcomeEmail(to: String, pdfUrl: String, unsubscribeUrl: String) {
        // intentionally no-op (optional: log)
    }
}
