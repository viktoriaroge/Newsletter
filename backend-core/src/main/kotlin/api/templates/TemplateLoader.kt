package com.viroge.newsletter.api.templates

interface TemplateLoader {
    suspend fun loadOrDefault(url: String?, defaultHtml: String): String
}
