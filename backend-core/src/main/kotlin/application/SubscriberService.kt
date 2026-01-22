package com.viroge.newsletter.application

import com.viroge.newsletter.domain.Subscriber
import com.viroge.newsletter.domain.SubscriberRepository
import com.viroge.newsletter.domain.SubscriptionStatus
import java.time.Instant
import java.util.UUID

class SubscriberService(
    private val repository: SubscriberRepository
) {

    fun subscribe(email: String): Subscriber {
        val normalizedEmail = email.trim().lowercase()

        require(normalizedEmail.isNotBlank()) {
            "Email must not be blank"
        }

        require("@" in normalizedEmail) {
            "Invalid email address"
        }

        val existing = repository.findByEmail(normalizedEmail)
        if (existing != null) {
            return existing
        }

        val subscriber = Subscriber(
            id = UUID.randomUUID().toString(),
            email = normalizedEmail,
            status = SubscriptionStatus.PENDING,
            createdAt = Instant.now().toString(),
            confirmedAt = null
        )

        return repository.save(subscriber)
    }
}
