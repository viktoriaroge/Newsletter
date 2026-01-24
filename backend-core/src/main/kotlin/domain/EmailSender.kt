package com.viroge.newsletter.domain

interface EmailSender {
    suspend fun sendWelcomeEmail(to: String, pdfUrl: String, unsubscribeUrl: String)
}
