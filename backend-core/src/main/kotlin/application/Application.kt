package com.viroge.newsletter.application

import com.viroge.newsletter.api.plugins.configureCors
import com.viroge.newsletter.api.plugins.configureErrorHandling
import com.viroge.newsletter.api.plugins.configureRequestLogging
import com.viroge.newsletter.api.routes.configureRoutes
import com.viroge.newsletter.api.plugins.configureSerialization
import com.viroge.newsletter.api.plugins.configureSwagger
import com.viroge.newsletter.api.rate.FixedWindowRateLimiter
import com.viroge.newsletter.domain.email.EmailSender
import com.viroge.newsletter.domain.email.NoOpEmailSender
import com.viroge.newsletter.domain.email.ResendEmailSender
import com.viroge.newsletter.infrastructure.database.DatabaseFactory
import com.viroge.newsletter.infrastructure.database.FlywayFactory
import com.viroge.newsletter.repository.InMemorySubscriberRepository
import com.viroge.newsletter.repository.PostgresSubscriberRepository
import com.viroge.newsletter.repository.SubscriberRepository
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

    val useDb = !System.getenv("JDBC_DATABASE_URL").isNullOrBlank()
    if (useDb) {
        DatabaseFactory.init()
        FlywayFactory.migrate()
    }

    configureRequestLogging()
    configureSerialization()
    configureErrorHandling()
    configureCors()
    configureSwagger()

    val repo: SubscriberRepository =
        if (useDb) PostgresSubscriberRepository()
        else InMemorySubscriberRepository()

    val usesEmailService = !System.getenv("RESEND_API_KEY").isNullOrBlank()
            && !System.getenv("EMAIL_FROM").isNullOrBlank()

    val emailSender: EmailSender =
        if (usesEmailService) ResendEmailSender(
            apiKey = System.getenv("RESEND_API_KEY") ?: "",
            from = System.getenv("EMAIL_FROM") ?: "",
        )
        else NoOpEmailSender()

    val service = SubscriberService(
        repo = repo,
        emailSender = emailSender,
        publicBaseUrl = System.getenv("PUBLIC_BASE_URL") ?: "http://localhost:8080",
        pdfUrl = System.getenv("PDF_URL") ?: "",
        unsubscribeSecret = System.getenv("UNSUBSCRIBE_SECRET") ?: "dev-secret"
    )

    val limiter = FixedWindowRateLimiter(limit = 5, windowSeconds = 60)

    configureRoutes(service, limiter)
}
