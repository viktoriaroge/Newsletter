package com.viroge.newsletter.api.routes

import com.viroge.newsletter.api.auth.requireAdminToken
import com.viroge.newsletter.api.dto.SubscriberRequest
import com.viroge.newsletter.api.rate.FixedWindowRateLimiter
import com.viroge.newsletter.api.rate.clientIp
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

fun Application.configureRoutes(service: SubscriberService, limiter: FixedWindowRateLimiter) {
    routing {
        configureMetaRoutes(service)
        configurePublicRoutes(service, limiter)   // squarespace + unsubscribe pages
        configureAdminRoutes(service)    // /v1/subscriptions (token-protected)
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

        call.respondText(
            """
            <html>
              <body>
                <h2>Unsubscribe?</h2>
                <form method="post" action="/v1/unsubscribe/confirm">
                  <input type="hidden" name="token" value="$token"/>
                  <button type="submit">Yes, unsubscribe</button>
                </form>
              </body>
            </html>
            """.trimIndent(),
            ContentType.Text.Html
        )
    }

    // Form submit from the confirmation page
    post("/v1/unsubscribe/confirm") {
        val params = call.receiveParameters()
        val token = params["token"]?.trim()

        if (token.isNullOrBlank()) {
            return@post call.respondText(
                "<html><body><h2>Invalid request.</h2></body></html>",
                ContentType.Text.Html,
                status = HttpStatusCode.BadRequest
            )
        }

        try {
            service.confirmUnsubscribe(token)
            call.respondText(
                "<html><body><h2>You’re unsubscribed.</h2></body></html>",
                ContentType.Text.Html
            )
        } catch (e: Exception) {
            // token invalid / expired / subscriber missing
            call.respondText(
                "<html><body><h2>Invalid or expired unsubscribe link.</h2></body></html>",
                ContentType.Text.Html,
                status = HttpStatusCode.BadRequest
            )
        }
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
