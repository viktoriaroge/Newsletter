package com.viroge.newsletter.api.dto

import kotlinx.serialization.Serializable

@Serializable
data class SubscriberRequest(
    val email: String,
)
