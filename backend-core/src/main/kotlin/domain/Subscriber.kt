package com.viroge.newsletter.domain

import java.time.Instant
import java.util.UUID

data class Subscriber(
    val id: UUID,
    val email: String,
    val status: SubscriptionStatus,
    val createdAt: Instant,
    val unsubscribedAt: Instant?,
)
