package com.viroge.newsletter.application

import com.viroge.newsletter.api.configureCors
import com.viroge.newsletter.api.configurePageStatus
import com.viroge.newsletter.api.routes.configureRoutes
import com.viroge.newsletter.api.configureSerialization
import com.viroge.newsletter.api.configureSwagger
import com.viroge.newsletter.infrastructure.database.DatabaseFactory
import com.viroge.newsletter.infrastructure.database.FlywayFactory
import com.viroge.newsletter.repository.PostgresSubscriberRepository
import com.viroge.newsletter.service.SubscriberService
import io.ktor.server.application.Application
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

    FlywayFactory.migrate()
    DatabaseFactory.init()

    configureSerialization()
    configurePageStatus()
    configureSwagger()
    configureCors()

    val repository = PostgresSubscriberRepository()
    val service = SubscriberService(repository)

    configureRoutes(service)
}
