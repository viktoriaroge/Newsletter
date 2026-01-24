import com.viroge.newsletter.api.configureSerialization
import com.viroge.newsletter.api.routes.configureRoutes
import com.viroge.newsletter.domain.email.FakeEmailSender
import com.viroge.newsletter.repository.InMemorySubscriberRepository
import com.viroge.newsletter.service.SubscriberService
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.config.MapApplicationConfig
import io.ktor.server.testing.testApplication
import kotlin.test.Test
import kotlin.test.assertEquals
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

        application {
            configureSerialization()
            configureRoutes(service)
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

        application {
            configureSerialization()
            configureRoutes(service)
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

        application {
            configureSerialization()
            configureRoutes(service)
        }

        val resp = client.post("/v1/squarespace/subscribe") {
            contentType(ContentType.Application.Json)
            setBody("""{"email":"test@example.com"}""")
        }

        assertEquals(HttpStatusCode.OK, resp.status)
        val body = resp.bodyAsText()
        assertTrue(body.contains("\"ok\""))
    }
}
