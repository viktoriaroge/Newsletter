package com.viroge.newsletter.api.plugins

import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCallPipeline
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.plugins.callid.CallId
import io.ktor.server.plugins.callid.callId
import io.ktor.server.plugins.callid.callIdMdc
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.util.AttributeKey
import org.slf4j.event.Level
import java.util.UUID

val RequestIdKey = AttributeKey<String>("requestId")

fun Application.configureRequestLogging() {

    install(CallId) {
        header("X-Request-Id")
        generate { UUID.randomUUID().toString() }
        verify { it.isNotBlank() }
    }

    intercept(ApplicationCallPipeline.Setup) {
        val id = call.callId
        if (id != null) call.attributes.put(RequestIdKey, id)
    }

    intercept(ApplicationCallPipeline.Plugins) {
        call.callId?.let { call.response.headers.append("X-Request-Id", it) }
    }

    install(CallLogging) {
        level = Level.INFO
        callIdMdc("requestId")
        filter { call ->
            true
        }
    }
}
