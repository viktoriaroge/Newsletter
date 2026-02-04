package com.viroge.newsletter.domain.email

interface EmailSender {
    suspend fun sendWelcomeEmail(to: String, pdfUrl: String, epubUrl: String, unsubscribeUrl: String)
}
