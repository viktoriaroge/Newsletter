package com.viroge.newsletter.domain.email

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class ResendEmailSender(
    private val apiKey: String,
    private val from: String
) : EmailSender {

    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    @Serializable
    private data class ResendEmailRequest(
        val from: String,
        val to: List<String>,
        val subject: String,
        val html: String
    )

    override suspend fun sendWelcomeEmail(to: String, pdfUrl: String, unsubscribeUrl: String) {
        val html = """
            <p>Thanks for subscribing!</p>
            <p>Here’s your download: <a href="$pdfUrl">Download PDF</a></p>
            <p>If you ever want to unsubscribe: <a href="$unsubscribeUrl">Unsubscribe</a></p>
        """.trimIndent()

        val payload = ResendEmailRequest(
            from = from,
            to = listOf(to),
            subject = "Welcome! Your download inside",
            html = html
        )

        client.post("https://api.resend.com/emails") {
            header(HttpHeaders.Authorization, "Bearer $apiKey")
            contentType(ContentType.Application.Json)
            setBody(payload)
        }.let { resp ->
            if (!resp.status.isSuccess()) {
                error("Resend failed: HTTP ${resp.status}")
            }
        }
    }
}
