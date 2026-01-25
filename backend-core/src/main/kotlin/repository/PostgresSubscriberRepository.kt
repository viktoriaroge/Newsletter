package com.viroge.newsletter.repository

import com.viroge.newsletter.domain.Subscriber
import com.viroge.newsletter.domain.SubscriptionStatus
import com.viroge.newsletter.domain.toSubscriber
import com.viroge.newsletter.infrastructure.database.SubscribersTable
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.time.Instant
import java.util.UUID

class PostgresSubscriberRepository : SubscriberRepository {

    override fun ping(): Unit = transaction {
        exec("SELECT 1") { /* ignore */ }
    }

    override fun findByEmail(email: String): Subscriber? = transaction {
        SubscribersTable
            .select { SubscribersTable.email eq email }
            .limit(1)
            .firstOrNull()
            ?.toSubscriber()
    }

    override fun findById(id: UUID): Subscriber? = transaction {
        SubscribersTable
            .select { SubscribersTable.id eq id }
            .limit(1)
            .firstOrNull()
            ?.toSubscriber()
    }

    override fun save(subscriber: Subscriber): Subscriber = transaction {
        SubscribersTable.insert {
            it[id] = subscriber.id
            it[email] = subscriber.email
            it[status] = subscriber.status.name
            it[createdAt] = subscriber.createdAt
            it[unsubscribedAt] = subscriber.unsubscribedAt
        }
        subscriber
    }

    override fun updateStatus(id: UUID, status: SubscriptionStatus, unsubscribedAt: Instant?): Boolean = transaction {
        val updated = SubscribersTable.update({ SubscribersTable.id eq id }) {
            it[SubscribersTable.status] = status.name
            it[SubscribersTable.unsubscribedAt] = unsubscribedAt
        }
        updated > 0
    }

    override fun updateLastWelcomeSentAt(id: UUID, at: Instant?): Boolean = transaction {
        SubscribersTable.update({ SubscribersTable.id eq id }) {
            it[lastWelcomeSentAt] = at
        } > 0
    }

    override fun findAll(): List<Subscriber> =
        transaction {
            SubscribersTable
                .selectAll()
                .map { it.toSubscriber() }
        }
}
