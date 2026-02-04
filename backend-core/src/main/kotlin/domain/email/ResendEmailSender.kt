package com.viroge.newsletter.domain.email

import com.viroge.newsletter.api.templates.TemplateLoader
import com.viroge.newsletter.api.templates.renderTemplate
import com.viroge.newsletter.domain.email.DefaultEmailTemplates.WELCOME_FALLBACK_HTML
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
import java.time.Year

class ResendEmailSender(
    private val apiKey: String,
    private val from: String,
    private val templateLoader: TemplateLoader,
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
        val html: String,
        val text: String,
    )

    override suspend fun sendWelcomeEmail(to: String, pdfUrl: String, epubUrl: String, unsubscribeUrl: String) {
        val templateUrl = System.getenv("WELCOME_EMAIL_TEMPLATE_URL")
        val websiteUrl = System.getenv("WEBSITE_URL").orEmpty()
        val logoUrl = System.getenv("LOGO_URL").orEmpty()
        val year = Year.now().value.toString()
        val htmlTemplate = templateLoader.loadOrDefault(templateUrl, WELCOME_FALLBACK_HTML)

        val html = renderTemplate(
            htmlTemplate,
            mapOf(
                "PDF_URL" to pdfUrl,
                "EPUB_URL" to epubUrl,
                "UNSUBSCRIBE_URL" to unsubscribeUrl,
                "WEBSITE_URL" to websiteUrl,
                "YEAR" to year,
                "LOGO_BLOCK" to logoUrl,
            )
        )

        // Optional: plain-text fallback (Resend supports text)
        val text =
            """
            Thank you for subscribing!
            
            Download: $pdfUrl
            Unsubscribe: $unsubscribeUrl
            """.trimIndent()

        val payload = ResendEmailRequest(
            from = from,
            to = listOf(to),
            subject = "Welcome",
            html = html,
            text = text,
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
