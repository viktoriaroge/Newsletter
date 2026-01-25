package com.viroge.newsletter.api.util

private val EMAIL_REGEX =
    Regex("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$", RegexOption.IGNORE_CASE)

fun requireValidEmail(value: String?, fieldName: String = "email"): String {
    val email = value?.trim()
    require(!email.isNullOrEmpty()) { "$fieldName is required" }
    require(EMAIL_REGEX.matches(email)) { "$fieldName is invalid" }
    return email.lowercase()
}
