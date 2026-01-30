package com.viroge.newsletter.api.rate

import java.time.Clock
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

class FixedWindowRateLimiter(
    private val limit: Int,
    private val windowSeconds: Long,
    private val clock: Clock = Clock.systemUTC()
) {
    private data class Counter(var windowStartSec: Long, var count: Int)

    private val counters = ConcurrentHashMap<String, Counter>()

    /**
     * @return true if request is allowed, false if rate-limited
     */
    fun allow(key: String): Boolean {
        val nowSec = Instant.now(clock).epochSecond
        val windowStart = nowSec - (nowSec % windowSeconds)

        val counter = counters.compute(key) { _, existing ->
            when {
                existing == null -> Counter(windowStart, 1)
                existing.windowStartSec != windowStart -> Counter(windowStart, 1)
                else -> {
                    existing.count += 1
                    existing
                }
            }
        }!!

        // Basic cleanup (best-effort) to prevent unbounded growth
        if (counters.size > 10_000) {
            cleanupOldWindows(currentWindowStart = windowStart)
        }

        return counter.count <= limit
    }

    private fun cleanupOldWindows(currentWindowStart: Long) {
        // remove entries older than 2 windows ago
        val minAllowed = currentWindowStart - (2 * windowSeconds)
        counters.entries.removeIf { it.value.windowStartSec < minAllowed }
    }
}
