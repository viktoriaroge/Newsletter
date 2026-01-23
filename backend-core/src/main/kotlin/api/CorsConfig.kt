package com.viroge.newsletter.api

import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.cors.routing.*

fun Application.configureCors() {
    val allowedOrigins = (System.getenv("CORS_ALLOWED_ORIGINS") ?: "")
        .split(",")
        .map { it.trim() }
        .filter { it.isNotBlank() }

    install(CORS) {
        // Always allow OPTIONS for browser preflight
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Post)
        allowHeader(HttpHeaders.ContentType)

        // If nothing is configured, fail safe (block everything)
        // or for dev you could fallback to localhost only.
        for (origin in allowedOrigins) {
            val uri = java.net.URI(origin)
            val host = if (uri.port == -1) uri.host else "${uri.host}:${uri.port}"
            allowHost(host, schemes = listOf(uri.scheme))
        }
    }
}
