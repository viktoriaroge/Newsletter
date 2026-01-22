package com.viroge.newsletter.api.routes

import com.viroge.newsletter.api.dto.SubscriberRequest
import com.viroge.newsletter.api.dto.toResponse
import com.viroge.newsletter.application.SubscriberService
import com.viroge.newsletter.api.receiveJsonOrNull
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route

fun Route.subscriptionRoutes(service: SubscriberService) {

    route("/v1/subscriptions") {

        post {
            val request = call.receiveJsonOrNull<SubscriberRequest>()
            val subscriber = service.subscribe(request?.email ?: "")
            call.respond(subscriber.toResponse())
        }
    }
}
