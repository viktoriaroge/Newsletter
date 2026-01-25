package com.viroge.newsletter.api.util

import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.log
import io.ktor.server.request.receive
import kotlin.reflect.KClass

suspend inline fun <reified T : Any> ApplicationCall.receiveJsonOrNull(): T? = receiveJsonOrNull(T::class)

suspend fun <T : Any> ApplicationCall.receiveJsonOrNull(type: KClass<T>): T? {
    return try {
        receive(type)
    } catch (cause: Throwable) {
        application.log.debug("Conversion failed, null returned", cause)
        null
    }
}
