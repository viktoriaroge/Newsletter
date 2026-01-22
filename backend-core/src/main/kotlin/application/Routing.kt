package com.viroge.newsletter.application

import com.viroge.newsletter.api.routes.subscriptionRoutes
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting(service: SubscriberService) {
    routing {
        get("/") {
            call.respondText("Hello World!")
        }
        get("/health") {
            call.respondText("OK")
        }
        subscriptionRoutes(service)
    }
}
