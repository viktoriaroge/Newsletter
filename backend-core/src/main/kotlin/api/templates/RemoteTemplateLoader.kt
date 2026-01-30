package com.viroge.newsletter.api.templates

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.Clock
import java.time.Duration
import java.time.Instant

class RemoteTemplateLoader(
    private val clock: Clock = Clock.systemUTC(),
    private val ttl: Duration = Duration.ofMinutes(5),
    private val httpClient: HttpClient = HttpClient(CIO) {
        expectSuccess = false
    }
): TemplateLoader {
    private data class CacheEntry(
        val fetchedAt: Instant,
        val body: String
    )

    private val mutex = Mutex()
    private val cache = mutableMapOf<String, CacheEntry>()      // url -> cached body
    private val lastGood = mutableMapOf<String, String>()       // url -> last known good body

    override suspend fun loadOrDefault(url: String?, defaultHtml: String): String {
        if (url.isNullOrBlank()) return defaultHtml

        val now = Instant.now(clock)

        // Return fresh cache if still valid
        cache[url]?.let { entry ->
            if (Duration.between(entry.fetchedAt, now) <= ttl) {
                return entry.body
            }
        }

        // Only one fetch at a time per process (simple + safe)
        return mutex.withLock {
            // re-check in case another coroutine refreshed it
            cache[url]?.let { entry ->
                if (Duration.between(entry.fetchedAt, now) <= ttl) {
                    return@withLock entry.body
                }
            }

            try {
                val resp: HttpResponse = httpClient.get(url) {
                    header(HttpHeaders.Accept, "text/html, text/plain;q=0.9, */*;q=0.8")
                }

                if (!resp.status.isSuccess()) {
                    return@withLock lastGood[url] ?: defaultHtml
                }

                val body = resp.bodyAsText()
                if (body.isBlank()) return@withLock lastGood[url] ?: defaultHtml

                cache[url] = CacheEntry(fetchedAt = now, body = body)
                lastGood[url] = body
                body
            } catch (_: Exception) {
                lastGood[url] ?: defaultHtml
            }
        }
    }
}
