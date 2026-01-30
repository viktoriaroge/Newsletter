package com.viroge.newsletter.api.templates

class FakeTemplateLoader(private val html: String) : TemplateLoader {
    override suspend fun loadOrDefault(url: String?, defaultHtml: String): String = html
}
