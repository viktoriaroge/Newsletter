package com.viroge.newsletter.infrastructure.database

import org.flywaydb.core.Flyway

object FlywayFactory {

    fun migrate() {
        val jdbcUrl = System.getenv("JDBC_DATABASE_URL")
            ?: error("JDBC_DATABASE_URL not set")
        val user = System.getenv("DB_USER")
            ?: error("DB_USER not set")
        val pass = System.getenv("DB_PASSWORD")
            ?: error("DB_PASSWORD not set")

        Flyway.configure()
            .dataSource(jdbcUrl, user, pass)
            .load()
            .migrate()
    }
}
