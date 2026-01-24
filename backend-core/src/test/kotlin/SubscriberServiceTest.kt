import com.viroge.newsletter.domain.SubscriptionStatus
import com.viroge.newsletter.domain.UnsubscribeTokens
import com.viroge.newsletter.domain.email.FakeEmailSender
import com.viroge.newsletter.repository.InMemorySubscriberRepository
import com.viroge.newsletter.repository.SubscriberRepository
import com.viroge.newsletter.service.SubscriberService
import kotlinx.coroutines.test.runTest
import kotlin.test.*
import java.time.Instant

class SubscriberServiceTest {

    private fun newService(
        repo: SubscriberRepository = InMemorySubscriberRepository(),
        emailSender: FakeEmailSender = FakeEmailSender()
    ): Pair<SubscriberService, FakeEmailSender> {
        val service = SubscriberService(
            repo = repo,
            emailSender = emailSender,
            publicBaseUrl = "http://localhost:8080",
            pdfUrl = "https://example.com/file.pdf",
            unsubscribeSecret = "test-secret-1234567890"
        )
        return service to emailSender
    }

    @Test
    fun `subscribe creates subscriber and activates when email succeeds`() = runTest {
        val (service, email) = newService()

        val s = service.subscribe("Test@Example.com")
        assertEquals("test@example.com", s.email)
        assertEquals(SubscriptionStatus.ACTIVE, s.status)
        assertTrue(email.sent.isNotEmpty())
    }

    @Test
    fun `subscribe keeps PENDING when email fails`() = runTest {
        val repo = InMemorySubscriberRepository()
        val email = FakeEmailSender().apply { shouldFail = true }
        val service = SubscriberService(
            repo = repo,
            emailSender = email,
            publicBaseUrl = "http://localhost:8080",
            pdfUrl = "https://example.com/file.pdf",
            unsubscribeSecret = "test-secret-1234567890"
        )

        val s = service.subscribe("fail@example.com")
        assertEquals(SubscriptionStatus.PENDING, s.status)
        assertEquals(0, email.sent.size)
    }

    @Test
    fun `confirmUnsubscribe marks subscriber as UNSUBSCRIBED`() = runTest {
        val (service, _) = newService()

        val s = service.subscribe("u@example.com")
        assertEquals(SubscriptionStatus.ACTIVE, s.status)

        // Create token the same way service does (uses UnsubscribeTokens)
        val token = UnsubscribeTokens.create(
            subscriberId = s.id.toString(),
            issuedAtEpochSec = Instant.now().epochSecond,
            secret = "test-secret-1234567890"
        )

        service.confirmUnsubscribe(token)

        val after = service.getByEmail("u@example.com")!!
        assertEquals(SubscriptionStatus.UNSUBSCRIBED, after.status)
        assertNotNull(after.unsubscribedAt)
    }
}
