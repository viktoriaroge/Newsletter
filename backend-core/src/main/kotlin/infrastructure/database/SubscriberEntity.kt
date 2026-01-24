package com.viroge.newsletter.infrastructure.database

data class SubscriberEntity(
    val id: String,
    val email: String,
    val status: String,
    val createdAt: String,
    val unsubscribedAt: String?,
)
