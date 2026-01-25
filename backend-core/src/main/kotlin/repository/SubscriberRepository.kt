package com.viroge.newsletter.repository

import com.viroge.newsletter.domain.Subscriber
import com.viroge.newsletter.domain.SubscriptionStatus
import java.time.Instant
import java.util.UUID

interface SubscriberRepository {
    fun ping()
    fun save(subscriber: Subscriber): Subscriber
    fun findByEmail(email: String): Subscriber?
    fun findById(id: UUID): Subscriber?
    fun findAll(): List<Subscriber>
    fun updateStatus(id: UUID, status: SubscriptionStatus, unsubscribedAt: Instant?): Boolean
    fun updateLastWelcomeSentAt(id: UUID, at: Instant?): Boolean
}
