package com.viroge.newsletter.infrastructure.database

import org.flywaydb.core.Flyway

object FlywayFactory {

    fun migrate() {
        Flyway.configure()
            .dataSource(
                "jdbc:postgresql://localhost:5432/newsletter",
                "newsletter",
                "newsletter"
            )
            .load()
            .migrate()
    }
}
