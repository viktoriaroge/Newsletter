package com.viroge.newsletter.service

import com.viroge.newsletter.domain.EmailSender
import com.viroge.newsletter.domain.Subscriber
import com.viroge.newsletter.domain.SubscriptionStatus
import com.viroge.newsletter.domain.UnsubscribeTokens
import com.viroge.newsletter.repository.SubscriberRepository
import java.time.Instant
import java.util.UUID

class SubscriberService(
    private val repo: SubscriberRepository,
    private val emailSender: EmailSender,
    private val publicBaseUrl: String,
    private val pdfUrl: String,
    private val unsubscribeSecret: String
) {

    suspend fun subscribe(rawEmail: String): Subscriber {
        val email = normalizeEmail(rawEmail)

        val existing = repo.findByEmail(email)
        val subscriber = when {
            existing == null -> {
                val now = Instant.now()
                repo.save(
                    Subscriber(
                        id = UUID.randomUUID(),
                        email = email,
                        status = SubscriptionStatus.PENDING,
                        createdAt = now,
                        unsubscribedAt = null
                    )
                )
            }

            existing.status == SubscriptionStatus.UNSUBSCRIBED -> {
                repo.updateStatus(existing.id, SubscriptionStatus.PENDING, unsubscribedAt = null)
                existing.copy(status = SubscriptionStatus.PENDING, unsubscribedAt = null)
            }

            else -> existing
        }

        val token = UnsubscribeTokens.create(
            subscriberId = subscriber.id.toString(),
            issuedAtEpochSec = Instant.now().epochSecond,
            secret = unsubscribeSecret
        )
        val unsubscribeUrl = "$publicBaseUrl/unsubscribe?token=$token"

        try {
            emailSender.sendWelcomeEmail(subscriber.email, pdfUrl, unsubscribeUrl)
            repo.updateStatus(subscriber.id, SubscriptionStatus.ACTIVE, unsubscribedAt = null)
        } catch (e: Exception) {
            println("Welcome email send failed for ${subscriber.email}: ${e.message}")
        }

        // Return latest state (optional: re-read if you want exact ACTIVE/PENDING)
        return repo.findById(subscriber.id) ?: subscriber
    }

    fun confirmUnsubscribe(token: String) {
        val subscriberIdStr = UnsubscribeTokens.verify(
            token = token,
            secret = unsubscribeSecret,
            maxAgeSeconds = 30L * 24 * 3600 // 30 days
        )

        val subscriberId = UUID.fromString(subscriberIdStr)

        val ok = repo.updateStatus(
            id = subscriberId,
            status = SubscriptionStatus.UNSUBSCRIBED,
            unsubscribedAt = Instant.now()
        )

        if (!ok) {
            throw NoSuchElementException("Subscriber not found")
        }
    }

    private fun normalizeEmail(raw: String): String =
        raw.trim().lowercase()

    fun getByEmail(email: String): Subscriber? =
        repo.findByEmail(email.lowercase())

    fun getAll(): List<Subscriber> =
        repo.findAll()
}
