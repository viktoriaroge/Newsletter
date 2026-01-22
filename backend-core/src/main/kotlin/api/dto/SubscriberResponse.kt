package com.viroge.newsletter.api.dto

import kotlinx.serialization.Serializable

@Serializable
data class SubscriberResponse(
    val id: String,
    val email: String,
    val status: String,
    val createdAt: String,
    val confirmedAt: String?,
)
