package com.viroge.newsletter.application

import io.ktor.server.application.*
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.EngineMain
import io.ktor.server.netty.Netty

fun main(args: Array<String>) {
    EngineMain.main(args)

    embeddedServer(Netty, port = 8080) {
        module()
    }.start(wait = true)
}

fun Application.module() {
    configureSerialization()

    val repository = InMemorySubscriberRepository()
    val service = SubscriberService(repository)

    configureRouting(service)
}
