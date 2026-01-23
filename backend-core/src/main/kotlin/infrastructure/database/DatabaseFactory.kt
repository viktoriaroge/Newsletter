package com.viroge.newsletter.infrastructure.database

import org.jetbrains.exposed.sql.Database

object DatabaseFactory {
    fun init() {
        val jdbcUrl = System.getenv("JDBC_DATABASE_URL")
            ?: error("JDBC_DATABASE_URL not set")
        val user = System.getenv("DB_USER")
            ?: error("DB_USER not set")
        val pass = System.getenv("DB_PASSWORD")
            ?: error("DB_PASSWORD not set")

        Database.connect(
            url = jdbcUrl,
            driver = "org.postgresql.Driver",
            user = user,
            password = pass
        )
    }
}
