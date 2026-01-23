package com.viroge.newsletter.repository

import com.viroge.newsletter.domain.Subscriber
import com.viroge.newsletter.domain.SubscriptionStatus
import com.viroge.newsletter.infrastructure.database.SubscribersTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

class PostgresSubscriberRepository : SubscriberRepository {

    override fun findById(id: String): Subscriber? =
        transaction {
            SubscribersTable
                .select { SubscribersTable.id eq id }
                .map { rowToSubscriber(it) }
                .singleOrNull()
        }

    override fun findByEmail(email: String): Subscriber? =
        transaction {
            SubscribersTable
                .select { SubscribersTable.email eq email }
                .map { rowToSubscriber(it) }
                .singleOrNull()
        }

    override fun save(subscriber: Subscriber): Subscriber =
        transaction {
            SubscribersTable.insert {
                it[id] = subscriber.id
                it[email] = subscriber.email
                it[status] = subscriber.status.name
                it[createdAt] = subscriber.createdAt
                it[confirmedAt] = subscriber.confirmedAt
            }
            subscriber
        }

    private fun rowToSubscriber(row: ResultRow): Subscriber =
        Subscriber(
            id = row[SubscribersTable.id],
            email = row[SubscribersTable.email],
            status = SubscriptionStatus.valueOf(row[SubscribersTable.status]),
            createdAt = row[SubscribersTable.createdAt],
            confirmedAt = row[SubscribersTable.confirmedAt]
        )

    override fun findAll(): List<Subscriber> =
        transaction {
            SubscribersTable
                .selectAll()
                .map { rowToSubscriber(it) }
        }
}
