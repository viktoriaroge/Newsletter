package com.viroge.newsletter.api.dto

import kotlinx.serialization.Serializable

@Serializable
data class ApiError(
    val code: String,
    val message: String,
    val requestId: String? = null
)
