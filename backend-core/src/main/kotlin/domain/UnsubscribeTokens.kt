package com.viroge.newsletter.domain

import java.util.Base64
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

object UnsubscribeTokens {
    private const val HMAC_ALG = "HmacSHA256"

    fun create(subscriberId: String, issuedAtEpochSec: Long, secret: String): String {
        val payload = "$subscriberId:$issuedAtEpochSec"
        val payloadB64 = b64Url(payload.toByteArray())
        val sigB64 = b64Url(hmacSha256(payload.toByteArray(), secret))
        return "$payloadB64.$sigB64"
    }

    fun verify(token: String, secret: String, maxAgeSeconds: Long): String {
        val parts = token.split(".")
        require(parts.size == 2) { "Invalid token" }

        val payloadBytes = b64UrlDecode(parts[0])
        val sigBytes = b64UrlDecode(parts[1])

        val expectedSig = hmacSha256(payloadBytes, secret)
        require(expectedSig.contentEquals(sigBytes)) { "Invalid token signature" }

        val payload = payloadBytes.toString(Charsets.UTF_8)
        val (id, issuedAtStr) = payload.split(":", limit = 2)
        val issuedAt = issuedAtStr.toLong()

        val now = System.currentTimeMillis() / 1000
        require(now - issuedAt <= maxAgeSeconds) { "Token expired" }

        return id // subscriberId
    }

    private fun hmacSha256(data: ByteArray, secret: String): ByteArray {
        val mac = Mac.getInstance(HMAC_ALG)
        mac.init(SecretKeySpec(secret.toByteArray(Charsets.UTF_8), HMAC_ALG))
        return mac.doFinal(data)
    }

    private fun b64Url(bytes: ByteArray): String =
        Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)

    private fun b64UrlDecode(s: String): ByteArray =
        Base64.getUrlDecoder().decode(s)
}