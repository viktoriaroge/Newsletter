package com.viroge.newsletter.repository

import com.viroge.newsletter.domain.Subscriber
import com.viroge.newsletter.domain.SubscriptionStatus
import java.time.Instant
import java.util.UUID

class InMemorySubscriberRepository : SubscriberRepository {

    private val store = mutableMapOf<UUID, Subscriber>()

    override fun ping() = Unit

    override fun save(subscriber: Subscriber): Subscriber {
        store[subscriber.id] = subscriber
        return subscriber
    }

    override fun findByEmail(email: String): Subscriber? =
        store.values.firstOrNull { it.email == email }

    override fun findById(id: UUID): Subscriber? =
        store[id]

    override fun updateStatus(
        id: UUID,
        status: SubscriptionStatus,
        unsubscribedAt: Instant?
    ): Boolean {
        val existing = store[id] ?: return false
        store[id] = existing.copy(
            status = status,
            unsubscribedAt = unsubscribedAt
        )
        return true
    }

    override fun updateLastWelcomeSentAt(id: UUID, at: Instant?): Boolean {
        val existing = store[id] ?: return false
        store[id] = existing.copy(lastWelcomeSentAt = at)
        return true
    }

    override fun findAll(): List<Subscriber> =
        store.values.toList()
}