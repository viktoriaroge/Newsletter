package com.viroge.newsletter.domain

import com.viroge.newsletter.api.dto.SubscriberResponse
import com.viroge.newsletter.infrastructure.database.SubscriberEntity
import com.viroge.newsletter.infrastructure.database.SubscribersTable
import org.jetbrains.exposed.sql.ResultRow
import java.time.Instant
import java.util.UUID

fun Subscriber.toResponse(): SubscriberResponse =
    SubscriberResponse(
        id = id.toString(),
        email = email,
        status = status.name,
        createdAt = createdAt.toString(), // ISO-8601
        unsubscribedAt = unsubscribedAt.toString(),
    )

fun SubscriberEntity.toDomain(): Subscriber =
    Subscriber(
        id = UUID.fromString(id),
        email = email,
        status = SubscriptionStatus.valueOf(status),
        createdAt = Instant.parse(createdAt),
        unsubscribedAt = unsubscribedAt?.let { Instant.parse(it) },
        lastWelcomeSentAt = lastWelcomeSentAt?.let { Instant.parse(it) },
    )

fun Subscriber.toEntity(): SubscriberEntity =
    SubscriberEntity(
        id = id.toString(),
        email = email,
        status = status.name,
        createdAt = createdAt.toString(), // ISO-8601
        unsubscribedAt = unsubscribedAt?.toString(),
        lastWelcomeSentAt = lastWelcomeSentAt?.toString(),
    )

fun ResultRow.toSubscriber(): Subscriber =
    Subscriber(
        id = this[SubscribersTable.id],
        email = this[SubscribersTable.email],
        status = SubscriptionStatus.valueOf(this[SubscribersTable.status]),
        createdAt = this[SubscribersTable.createdAt],
        unsubscribedAt = this[SubscribersTable.unsubscribedAt],
        lastWelcomeSentAt = this[SubscribersTable.lastWelcomeSentAt],
    )
