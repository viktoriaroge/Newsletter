package com.viroge.newsletter.api.routes

import com.viroge.newsletter.api.dto.SubscriberRequest
import com.viroge.newsletter.service.SubscriberService
import com.viroge.newsletter.api.receiveJsonOrNull
import com.viroge.newsletter.domain.toResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receiveParameters
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route

fun Route.configureSubscriptionRoutes(service: SubscriberService) {

    route("/v1/subscriptions") {

        post {
            val request = call.receiveJsonOrNull<SubscriberRequest>()
            val subscriber = service.subscribe(request?.email ?: "")
            call.respond(subscriber.toResponse())
        }

        get("/{email}") {
            val email = call.parameters["email"]!!
            val subscriber = service.getByEmail(email)
            call.respond(subscriber.toResponse())
        }

        get {
            val subscribers = service.getAll()
            call.respond(subscribers.map { it.toResponse() })
        }
    }

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

    route("/v1/squarespace") {

        post("/subscribe") {
            val request = call.receiveJsonOrNull<SubscriberRequest>()
            service.subscribe(request?.email ?: "")
            call.respond(mapOf("ok" to true))
        }
    }
}
