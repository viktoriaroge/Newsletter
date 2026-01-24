package com.viroge.newsletter.infrastructure.database

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp

object SubscribersTable : Table("subscribers") {

    val id = uuid("id").uniqueIndex()
    val email = varchar("email", 320).uniqueIndex()

    val status = varchar("status", 32) // "PENDING", "ACTIVE", "UNSUBSCRIBED"
    val createdAt = timestamp("created_at")
    val unsubscribedAt = timestamp("unsubscribed_at").nullable()

    override val primaryKey = PrimaryKey(id)
}
