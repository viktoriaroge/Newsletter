package com.viroge.newsletter.service

import com.viroge.newsletter.domain.email.EmailSender
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
    private val WELCOME_COOLDOWN_SECONDS = 24L * 3600

    suspend fun subscribe(rawEmail: String): Subscriber {
        val email = normalizeEmail(rawEmail)
        val now = Instant.now()

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
                        unsubscribedAt = null,
                        lastWelcomeSentAt = null,
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

        val shouldSendWelcome = when (subscriber.status) {
            SubscriptionStatus.PENDING -> true
            SubscriptionStatus.UNSUBSCRIBED -> true // should not happen here, but safe
            SubscriptionStatus.ACTIVE -> {
                val last = subscriber.lastWelcomeSentAt
                last == null || (now.epochSecond - last.epochSecond) >= WELCOME_COOLDOWN_SECONDS
            }
        }

        if (shouldSendWelcome) {
            try {
                emailSender.sendWelcomeEmail(
                    to = email,
                    pdfUrl = pdfUrl,
                    unsubscribeUrl = unsubscribeUrl
                )
                repo.updateLastWelcomeSentAt(subscriber.id, now)
                repo.updateStatus(subscriber.id, SubscriptionStatus.ACTIVE, unsubscribedAt = null)
            } catch (e: Exception) {
                // Keep PENDING (or keep ACTIVE if it already was), but do not crash caller
                println("Welcome email send failed for $email: ${e.message}")
            }
        } else {
            // No resend, but ensure ACTIVE (it already is in this branch)
            if (subscriber.status != SubscriptionStatus.ACTIVE) {
                repo.updateStatus(subscriber.id, SubscriptionStatus.ACTIVE, unsubscribedAt = null)
            }
        }

        return repo.findById(subscriber.id) ?: subscriber
    }

    fun confirmUnsubscribe(token: String) {
        val subscriberIdStr = UnsubscribeTokens.verify(
            token = token,
            secret = unsubscribeSecret,
            maxAgeSeconds = 30L * 24 * 3600
        )

        val id = UUID.fromString(subscriberIdStr)
        val ok = repo.updateStatus(id, SubscriptionStatus.UNSUBSCRIBED, unsubscribedAt = Instant.now())
        if (!ok) throw NoSuchElementException("Subscriber not found")
    }

    private fun normalizeEmail(raw: String): String =
        raw.trim().lowercase()

    fun getByEmail(email: String): Subscriber? =
        repo.findByEmail(email.lowercase())

    fun getAll(): List<Subscriber> =
        repo.findAll()
}
