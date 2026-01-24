package com.viroge.newsletter.domain.email

class FakeEmailSender : EmailSender {
    data class Sent(val to: String, val pdfUrl: String, val unsubscribeUrl: String)

    val sent = mutableListOf<Sent>()
    var shouldFail: Boolean = false

    override suspend fun sendWelcomeEmail(to: String, pdfUrl: String, unsubscribeUrl: String) {
        if (shouldFail) error("Simulated email failure")
        sent += Sent(to, pdfUrl, unsubscribeUrl)
    }
}
