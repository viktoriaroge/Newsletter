package com.viroge.newsletter.api.templates

fun renderTemplate(html: String, values: Map<String, String>): String {
    var out = html
    values.forEach { (k, v) ->
        out = out.replace("{{${k}}}", v, ignoreCase = false)
    }
    return out
}
