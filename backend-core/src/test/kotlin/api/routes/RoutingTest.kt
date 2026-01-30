package api.routes

import com.viroge.newsletter.api.plugins.configureErrorHandling
import com.viroge.newsletter.api.plugins.configureSerialization
import com.viroge.newsletter.api.rate.FixedWindowRateLimiter
import com.viroge.newsletter.api.routes.configureRoutes
import com.viroge.newsletter.api.templates.FakeTemplateLoader
import com.viroge.newsletter.repository.InMemorySubscriberRepository
import com.viroge.newsletter.service.SubscriberService
import com.viroge.newsletter.domain.email.FakeEmailSender
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.config.MapApplicationConfig
import io.ktor.server.testing.testApplication
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RoutingTest {

    @Test
    fun `admin endpoints return 401 without token`() = testApplication {
        environment {
            config = MapApplicationConfig(
                "admin.apiToken" to "secret-token"
            )
        }

        val repo = InMemorySubscriberRepository()
        val emailSender = FakeEmailSender()
        val service = SubscriberService(
            repo = repo,
            emailSender = emailSender,
            publicBaseUrl = "http://localhost:8080",
            pdfUrl = "https://example.com/file.pdf",
            unsubscribeSecret = "test-secret-123"
        )

        val limiter = FixedWindowRateLimiter(limit = 5, windowSeconds = 60)
        val templateLoader = FakeTemplateLoader("")
        val publicBaseUrl = "http://localhost:8080"

        application {
            configureSerialization()
            configureRoutes(service, limiter, templateLoader, publicBaseUrl)
        }

        val resp = client.get("/v1/subscriptions")
        assertEquals(HttpStatusCode.Unauthorized, resp.status)
    }

    @Test
    fun `admin endpoints return 200 with token`() = testApplication {
        environment {
            config = MapApplicationConfig(
                "admin.apiToken" to "secret-token"
            )
        }

        val repo = InMemorySubscriberRepository()
        val emailSender = FakeEmailSender()
        val service = SubscriberService(
            repo = repo,
            emailSender = emailSender,
            publicBaseUrl = "http://localhost:8080",
            pdfUrl = "https://example.com/file.pdf",
            unsubscribeSecret = "test-secret-123"
        )

        val limiter = FixedWindowRateLimiter(limit = 5, windowSeconds = 60)
        val templateLoader = FakeTemplateLoader("")
        val publicBaseUrl = "http://localhost:8080"

        application {
            configureSerialization()
            configureRoutes(service, limiter, templateLoader, publicBaseUrl)
        }

        val resp = client.get("/v1/subscriptions") {
            header("X-Admin-Token", "secret-token")
        }
        assertEquals(HttpStatusCode.OK, resp.status)
    }

    @Test
    fun `squarespace subscribe returns ok true`() = testApplication {
        val repo = InMemorySubscriberRepository()
        val emailSender = FakeEmailSender()
        val service = SubscriberService(
            repo = repo,
            emailSender = emailSender,
            publicBaseUrl = "http://localhost:8080",
            pdfUrl = "https://example.com/file.pdf",
            unsubscribeSecret = "test-secret-123"
        )

        val limiter = FixedWindowRateLimiter(limit = 5, windowSeconds = 60)
        val templateLoader = FakeTemplateLoader("")
        val publicBaseUrl = "http://localhost:8080"

        application {
            configureSerialization()
            configureRoutes(service, limiter, templateLoader, publicBaseUrl)
        }

        val resp = client.post("/v1/squarespace/subscribe") {
            contentType(ContentType.Application.Json)
            setBody("""{"email":"test@example.com"}""")
        }

        assertEquals(HttpStatusCode.OK, resp.status)
        val body = resp.bodyAsText()
        assertTrue(body.contains("\"ok\""))
    }

    @Test
    fun `admin subscribe returns 400 for invalid email`() = testApplication {
        environment {
            config = MapApplicationConfig(
                "admin.apiToken" to "secret-token"
            )
        }

        val repo = InMemorySubscriberRepository()
        val emailSender = FakeEmailSender()
        val service = SubscriberService(
            repo = repo,
            emailSender = emailSender,
            publicBaseUrl = "http://localhost:8080",
            pdfUrl = "https://example.com/file.pdf",
            unsubscribeSecret = "test-secret-123"
        )

        val limiter = FixedWindowRateLimiter(limit = 5, windowSeconds = 60)
        val templateLoader = FakeTemplateLoader("")
        val publicBaseUrl = "http://localhost:8080"

        application {
            configureSerialization()
            configureErrorHandling()
            configureRoutes(service, limiter, templateLoader, publicBaseUrl)
        }

        val resp = client.post("/v1/subscriptions") {
            header("X-Admin-Token", "secret-token")
            contentType(ContentType.Application.Json)
            setBody("""{"email":"not-an-email"}""")
        }

        assertEquals(HttpStatusCode.BadRequest, resp.status)

        val body = resp.bodyAsText()
        assertTrue(body.contains("bad_request"), "Expected ApiError response, got: $body")
    }

    @Test
    fun `squarespace subscribe sets rate-limited header after limit`() = testApplication {
        val repo = InMemorySubscriberRepository()
        val emailSender = FakeEmailSender()
        val service = SubscriberService(
            repo = repo,
            emailSender = emailSender,
            publicBaseUrl = "http://localhost:8080",
            pdfUrl = "https://example.com/file.pdf",
            unsubscribeSecret = "test-secret-123"
        )

        val limiter = FixedWindowRateLimiter(limit = 2, windowSeconds = 60)
        val templateLoader = FakeTemplateLoader("")
        val publicBaseUrl = "http://localhost:8080"

        application {
            configureSerialization()
            configureRoutes(service, limiter, templateLoader, publicBaseUrl)
        }

        suspend fun postOnce(): HttpResponse =
            client.post("/v1/squarespace/subscribe") {
                header("X-Forwarded-For", "1.2.3.4")
                contentType(ContentType.Application.Json)
                setBody("""{"email":"test@example.com"}""")
            }

        val r1 = postOnce()
        assertEquals(HttpStatusCode.OK, r1.status)
        assertNull(r1.headers["X-Rate-Limited"])

        val r2 = postOnce()
        assertEquals(HttpStatusCode.OK, r2.status)
        assertNull(r2.headers["X-Rate-Limited"])

        val r3 = postOnce()
        assertEquals(HttpStatusCode.OK, r3.status)
        assertEquals("true", r3.headers["X-Rate-Limited"])
    }

    @Test
    fun `unsubscribe page renders html with placeholders replaced`() = testApplication {
        val repo = InMemorySubscriberRepository()
        val service = SubscriberService(
            repo = repo,
            emailSender = FakeEmailSender(),
            publicBaseUrl = "http://localhost:8080",
            pdfUrl = "https://example.com/file.pdf",
            unsubscribeSecret = "test-secret-123"
        )

        val fakeHtml = """
        <html>
          <body>
            token={{TOKEN}}
            action={{ACTION_URL}}
            site={{WEBSITE_URL}}
          </body>
        </html>
    """.trimIndent()

        val limiter = FixedWindowRateLimiter(limit = 5, windowSeconds = 60)
        val templateLoader = FakeTemplateLoader(fakeHtml)
        val publicBaseUrl = "http://localhost:8080"

        application {
            configureSerialization()
            configureRoutes(service, limiter, templateLoader, publicBaseUrl)
        }

        // WEBSITE_URL env might not be set in tests; your code should default to example.com or similar
        val resp = client.get("/unsubscribe?token=abc123")

        assertEquals(HttpStatusCode.OK, resp.status)
        assertTrue(resp.headers[HttpHeaders.ContentType]!!.contains("text/html"))

        val body = resp.bodyAsText()
        assertTrue(body.contains("token=abc123"))
        assertTrue(body.contains("action=http://localhost")) // derived from request
    }

}