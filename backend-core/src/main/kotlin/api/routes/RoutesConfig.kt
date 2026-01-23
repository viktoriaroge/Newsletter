package com.viroge.newsletter.api.routes

import com.viroge.newsletter.service.SubscriberService
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRoutes(service: SubscriberService) {
    routing {
        get("/") {
            call.respondText("Hello World!")
        }
        get("/health") {
            call.respondText("OK")
        }
        configureSubscriptionRoutes(service)
    }
}
