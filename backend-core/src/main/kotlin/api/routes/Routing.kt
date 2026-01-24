package com.viroge.newsletter.api.routes

import com.viroge.newsletter.api.auth.requireAdminToken
import com.viroge.newsletter.api.dto.SubscriberRequest
import com.viroge.newsletter.api.receiveJsonOrNull
import com.viroge.newsletter.domain.toResponse
import com.viroge.newsletter.service.SubscriberService
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.request.receiveParameters
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRoutes(service: SubscriberService) {
    routing {
        configureMetaRoutes()
        configurePublicRoutes(service)   // squarespace + unsubscribe pages
        configureAdminRoutes(service)    // /v1/subscriptions (token-protected)
    }
}

private fun Route.configureMetaRoutes() {
    get("/") { call.respondText("Hello World!") }
    get("/health") { call.respondText("OK") }
}

private fun Route.configurePublicRoutes(service: SubscriberService) {

    // Squarespace-friendly endpoint
    route("/v1/squarespace") {
        post("/subscribe") {
            val request = call.receiveJsonOrNull<SubscriberRequest>()
                ?: return@post call.respond(HttpStatusCode.BadRequest)

            if (request.email.isBlank()) {
                return@post call.respond(HttpStatusCode.BadRequest)
            }

            service.subscribe(request.email)
            call.respond(mapOf("ok" to true))
        }
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
        val token = params["token"]
            ?: return@post call.respond(HttpStatusCode.BadRequest)

        service.confirmUnsubscribe(token)

        call.respondText(
            "<html><body><h2>You’re unsubscribed.</h2></body></html>",
            ContentType.Text.Html
        )
    }
}

private fun Route.configureAdminRoutes(service: SubscriberService) {
    route("/v1/subscriptions") {

        post {
            if (!call.requireAdminToken()) return@post
            val request = call.receiveJsonOrNull<SubscriberRequest>()
                ?: return@post call.respond(HttpStatusCode.BadRequest)

            if (request.email.isBlank()) return@post call.respond(HttpStatusCode.BadRequest)

            val subscriber = service.subscribe(request.email)
            call.respond(subscriber.toResponse())
        }

        get {
            if (!call.requireAdminToken()) return@get
            val subscribers = service.getAll()
            call.respond(subscribers.map { it.toResponse() })
        }

        get("/{email}") {
            if (!call.requireAdminToken()) return@get
            val email = call.parameters["email"]
                ?: return@get call.respond(HttpStatusCode.BadRequest)

            val subscriber = service.getByEmail(email)
                ?: return@get call.respond(HttpStatusCode.NotFound)

            call.respond(subscriber.toResponse())
        }
    }
}
