package com.viroge.newsletter.api.plugins

import io.ktor.server.application.Application
import io.ktor.server.http.content.staticResources
import io.ktor.server.routing.routing

fun Application.configureSwagger() {
    routing {
        staticResources("/", "")
        staticResources("/swagger", "swagger")
    }
}
