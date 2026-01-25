package com.viroge.newsletter.api.plugins

import com.viroge.newsletter.api.dto.ApiError
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.install
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond
import org.slf4j.LoggerFactory

private val log = LoggerFactory.getLogger("ApiErrors")

fun Application.configureErrorHandling() {
    install(StatusPages) {

        exception<IllegalArgumentException> { call, cause ->
            log.warn("400: {}", cause.message)
            call.respond(
                HttpStatusCode.BadRequest,
                ApiError(
                    code = "bad_request",
                    message = cause.message ?: "Bad request",
                    requestId = call.requestIdOrNull()
                )
            )
        }

        exception<NoSuchElementException> { call, cause ->
            log.warn("404: {}", cause.message)
            call.respond(
                HttpStatusCode.NotFound,
                ApiError(
                    code = "not_found",
                    message = cause.message ?: "Not found",
                    requestId = call.requestIdOrNull()
                )
            )
        }

        exception<Throwable> { call, cause ->
            log.error("500: Unhandled exception", cause)
            call.respond(
                HttpStatusCode.InternalServerError,
                ApiError(
                    code = "internal_error",
                    message = "Something went wrong",
                    requestId = call.requestIdOrNull()
                )
            )
        }
    }
}

private fun ApplicationCall.requestIdOrNull(): String? =
    attributes.getOrNull(RequestIdKey)