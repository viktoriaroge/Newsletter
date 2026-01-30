package com.viroge.newsletter.api.rate

import io.ktor.server.application.*
import io.ktor.server.plugins.origin

fun ApplicationCall.clientIp(): String {
    // Prefer forwarded header if present (Fly/Proxies)
    // Common: X-Forwarded-For: client, proxy1, proxy2
    val xff = request.headers["X-Forwarded-For"]
    if (!xff.isNullOrBlank()) {
        return xff.split(',').first().trim()
    }

    // Fly might include this sometimes
    request.headers["Fly-Client-IP"]?.let { if (it.isNotBlank()) return it.trim() }

    return request.origin.remoteHost
}
