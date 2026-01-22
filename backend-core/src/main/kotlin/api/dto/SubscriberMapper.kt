package com.viroge.newsletter.api.dto

import com.viroge.newsletter.domain.Subscriber

fun Subscriber.toResponse(): SubscriberResponse =
    SubscriberResponse(
        id = id,
        email = email,
        status = status.name,
        createdAt = createdAt,
        confirmedAt = confirmedAt
    )
