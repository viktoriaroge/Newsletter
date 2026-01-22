package com.viroge.newsletter.infrastructure.database

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseFactory {
    fun init() {
        Database.connect(
            url = "jdbc:postgresql://localhost:5432/newsletter",
            driver = "org.postgresql.Driver",
            user = "newsletter",
            password = "newsletter",
        )

        transaction {
            SchemaUtils.create(SubscribersTable)
        }
    }
}
