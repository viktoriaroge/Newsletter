package com.viroge.newsletter.api.auth

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.respond

private const val ADMIN_HEADER = "X-Admin-Token"

suspend fun ApplicationCall.requireAdminToken(): Boolean {
    val expected = System.getenv("ADMIN_API_TOKEN").orEmpty()
    if (expected.isBlank()) {
        // Fail closed in prod; but this also protects from accidentally running without a token.
        respond(HttpStatusCode.InternalServerError, "ADMIN_API_TOKEN is not set")
        return false
    }

    val provided = request.headers[ADMIN_HEADER]
    if (provided == null || provided != expected) {
        respond(HttpStatusCode.Unauthorized, "Unauthorized")
        return false
    }

    return true
}
