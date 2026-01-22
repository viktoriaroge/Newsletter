package com.viroge.newsletter.domain

import kotlinx.serialization.Serializable

enum class SubscriptionStatus {
    PENDING,
    ACTIVE,
    UNSUBSCRIBED
}

@Serializable
data class Subscriber(
    val id: String,
    val email: String,
    val status: SubscriptionStatus,
    val createdAt: String,
    val confirmedAt: String?
)
