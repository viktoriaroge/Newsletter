val kotlin_version: String by project
val ktor_version: String by project
val logback_version: String by project
val exposed_version: String by project
val flyway_version: String by project

plugins {
    kotlin("jvm") version "2.2.21"
    id("io.ktor.plugin") version "3.3.3"
    id("org.jetbrains.kotlin.plugin.serialization") version "2.2.21"
}

group = "com.viroge.newsletter"
version = "0.0.1"

application {
    mainClass = "io.ktor.server.netty.EngineMain"
}

kotlin {
    jvmToolchain(24) // Set to the lowest common denominator
}

tasks.test {
    useJUnitPlatform()
}

dependencies {
    implementation("io.ktor:ktor-server-core")
    implementation("io.ktor:ktor-server-content-negotiation")
    implementation("io.ktor:ktor-serialization-kotlinx-json")
    implementation("io.ktor:ktor-server-config-yaml")
    implementation("io.ktor:ktor-server-netty")
    implementation("io.ktor:ktor-server-status-pages")
    implementation("io.ktor:ktor-server-cors:$ktor_version")

    implementation("io.ktor:ktor-client-core:$ktor_version")
    implementation("io.ktor:ktor-client-cio:$ktor_version")
    implementation("io.ktor:ktor-client-content-negotiation:$ktor_version")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktor_version")

    implementation("ch.qos.logback:logback-classic:$logback_version")

    implementation("org.jetbrains.exposed:exposed-core:$exposed_version")
    implementation("org.jetbrains.exposed:exposed-dao:$exposed_version")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposed_version")
    implementation("org.jetbrains.exposed:exposed-java-time:$exposed_version")

    implementation("org.flywaydb:flyway-core:$flyway_version")
    implementation("org.flywaydb:flyway-database-postgresql:$flyway_version")

    implementation("org.postgresql:postgresql:42.7.7")

    testImplementation(kotlin("test")) // kotlin.test + JUnit integration
    testImplementation("io.ktor:ktor-server-test-host:$ktor_version")
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")
}
