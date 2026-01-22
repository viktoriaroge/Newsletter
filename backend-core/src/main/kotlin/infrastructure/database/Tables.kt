package com.viroge.newsletter.infrastructure.database

import org.jetbrains.exposed.sql.Table

object SubscribersTable : Table("subscribers") {

    val id = varchar("id", 36)
    val email = varchar("email", 255).uniqueIndex()
    val status = varchar("status", 50)
    val createdAt = varchar("created_at", 50)
    val confirmedAt = varchar("confirmed_at", 50).nullable()

    override val primaryKey = PrimaryKey(id)
}
