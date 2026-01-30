package com.viroge.newsletter.api.routes

import com.viroge.newsletter.api.auth.requireAdminToken
import com.viroge.newsletter.api.dto.SubscriberRequest
import com.viroge.newsletter.api.rate.FixedWindowRateLimiter
import com.viroge.newsletter.api.rate.clientIp
import com.viroge.newsletter.api.templates.DefaultPages
import com.viroge.newsletter.api.templates.TemplateLoader
import com.viroge.newsletter.api.templates.renderTemplate
import com.viroge.newsletter.api.util.receiveJsonOrNull
import com.viroge.newsletter.api.util.requireValidEmail
import com.viroge.newsletter.domain.toResponse
import com.viroge.newsletter.service.SubscriberService
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.plugins.callid.callId
import io.ktor.server.request.receiveParameters
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRoutes(
    service: SubscriberService,
    limiter: FixedWindowRateLimiter,
    templateLoader: TemplateLoader,
    publicBaseUrl: String
) {
    routing {
        configureMetaRoutes(service)
        // squarespace + unsubscribe pages:
        configurePublicRoutes(service, limiter, templateLoader, publicBaseUrl)
        // /v1/subscriptions (token-protected):
        configureAdminRoutes(service)
    }
}

private fun Route.configureMetaRoutes(service: SubscriberService) {
    get("/") { call.respondText("Hello World!") }
    get("/health") { call.respondText("OK") }
    get("/health/db") {
        try {
            service.pingDb()
            call.respondText("OK")
        } catch (e: Exception) {
            call.respondText("DB_UNAVAILABLE", status = HttpStatusCode.ServiceUnavailable)
        }
    }
}

private fun Route.configurePublicRoutes(
    service: SubscriberService,
    limiter: FixedWindowRateLimiter,
    templateLoader: TemplateLoader,
    publicBaseUrl: String,
) {

    // Squarespace-friendly endpoint
    post("/v1/squarespace/subscribe") {
        val ip = call.clientIp()
        val allowed = limiter.allow("sq:$ip")

        if (!allowed) {
            application.log.warn("Rate limited Squarespace subscribe ip={} requestId={}", ip, call.callId)
            call.response.headers.append("X-Rate-Limited", "true")
            return@post call.respond(mapOf("ok" to true)) // keep UX
        }

        val request = call.receiveJsonOrNull<SubscriberRequest>()
        val emailRaw = request?.email

        val email = try {
            requireValidEmail(emailRaw)
        } catch (_: IllegalArgumentException) {
            application.log.warn("Squarespace subscribe invalid email (requestId={})", call.callId)
            return@post call.respond(mapOf("ok" to true))
        }

        try {
            service.subscribe(email)
        } catch (e: Exception) {
            application.log.error("Squarespace subscribe failed for {} (requestId={})", email, call.callId, e)
            // still ok=true
        }

        call.respond(mapOf("ok" to true))
    }

    // Browser page opened from email
    get("/unsubscribe") {
        val token = call.request.queryParameters["token"]
            ?: return@get call.respond(HttpStatusCode.BadRequest)

        val confirmTemplateUrl = System.getenv("UNSUBSCRIBE_CONFIRM_TEMPLATE_URL")
        val websiteUrl = System.getenv("WEBSITE_URL") ?: "https://example.com"
        val actionUrl = "$publicBaseUrl/v1/unsubscribe/confirm".trimEnd('/')

        val defaultHtml = DefaultPages.unsubscribeConfirmDefault(
            token = token,
            actionUrl = actionUrl,
            websiteUrl = websiteUrl
        )

        val html = templateLoader.loadOrDefault(confirmTemplateUrl, defaultHtml)

        val rendered = renderTemplate(
            html,
            mapOf(
                "TOKEN" to token,
                "ACTION_URL" to actionUrl,
                "WEBSITE_URL" to websiteUrl
            )
        )

        call.respondText(rendered, ContentType.Text.Html)
    }

    // Form submit from the confirmation page
    post("/v1/unsubscribe/confirm") {
        val params = call.receiveParameters()
        val token = params["token"]?.trim()

        val resultTemplateUrl = System.getenv("UNSUBSCRIBE_RESULT_TEMPLATE_URL")
        val websiteUrl = System.getenv("WEBSITE_URL") ?: "https://example.com"

        if (token.isNullOrBlank()) {
            val defaultHtml = DefaultPages.unsubscribeResultDefault(
                title = "Invalid request",
                message = "Missing token.",
                websiteUrl = websiteUrl
            )
            val html = templateLoader.loadOrDefault(resultTemplateUrl, defaultHtml)
            val rendered = renderTemplate(
                html,
                mapOf(
                    "TITLE" to "Invalid request",
                    "MESSAGE" to "Missing token.",
                    "WEBSITE_URL" to websiteUrl
                )
            )
            return@post call.respondText(rendered, ContentType.Text.Html, status = HttpStatusCode.BadRequest)
        }

        val (title, message, status) = try {
            service.confirmUnsubscribe(token)
            Triple("You're unsubscribed", "You will no longer receive emails from us.", HttpStatusCode.OK)
        } catch (_: Exception) {
            Triple(
                "Invalid or expired link",
                "This unsubscribe link is invalid or has expired.",
                HttpStatusCode.BadRequest
            )
        }

        val defaultHtml = DefaultPages.unsubscribeResultDefault(
            title = title,
            message = message,
            websiteUrl = websiteUrl
        )

        val html = templateLoader.loadOrDefault(resultTemplateUrl, defaultHtml)
        val rendered = renderTemplate(
            html,
            mapOf(
                "TITLE" to title,
                "MESSAGE" to message,
                "WEBSITE_URL" to websiteUrl
            )
        )

        call.respondText(rendered, ContentType.Text.Html, status = status)
    }
}

private fun Route.configureAdminRoutes(service: SubscriberService) {
    route("/v1/subscriptions") {

        post {
            if (!call.requireAdminToken()) return@post

            val request = call.receiveJsonOrNull<SubscriberRequest>()
                ?: throw IllegalArgumentException("Request body is required")

            val email = requireValidEmail(request.email)

            val subscriber = service.subscribe(email)
            call.respond(subscriber.toResponse())
        }

        get {
            if (!call.requireAdminToken()) return@get

            val subscribers = service.getAll()
            call.respond(subscribers.map { it.toResponse() })
        }

        get("/{email}") {
            if (!call.requireAdminToken()) return@get

            val email = requireValidEmail(call.parameters["email"])

            val subscriber = service.getByEmail(email)
                ?: throw NoSuchElementException("Subscriber not found")

            call.respond(subscriber.toResponse())
        }
    }
}
