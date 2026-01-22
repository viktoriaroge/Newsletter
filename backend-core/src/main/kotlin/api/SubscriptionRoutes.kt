package com.viroge.newsletter.api

import com.viroge.newsletter.application.SubscriberService
import com.viroge.newsletter.domain.receiveJsonOrNull
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import kotlinx.serialization.Serializable

@Serializable
data class SubscribeRequest(val email: String)

fun Route.subscriptionRoutes(service: SubscriberService) {

    route("/v1/subscriptions") {

        post {
            val request = call.receiveJsonOrNull<SubscribeRequest>()
            val subscriber = service.subscribe(request?.email ?: "")
            call.respond(subscriber)
        }
    }
}
