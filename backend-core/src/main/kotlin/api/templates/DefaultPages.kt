package com.viroge.newsletter.api.templates

object DefaultPages {

    fun unsubscribeConfirmDefault(token: String, actionUrl: String, websiteUrl: String): String =
        """
        <!doctype html>
        <html>
        <head><meta charset="utf-8"><meta name="viewport" content="width=device-width, initial-scale=1">
        <title>Unsubscribe</title></head>
        <body style="font-family: Arial, sans-serif; padding: 24px;">
          <h2>Unsubscribe?</h2>
          <p>Click confirm to stop receiving future emails.</p>
          <form method="post" action="$actionUrl">
            <input type="hidden" name="token" value="$token"/>
            <button type="submit">Confirm unsubscribe</button>
          </form>
          <p style="margin-top:16px;"><a href="$websiteUrl">Back to website</a></p>
        </body></html>
        """.trimIndent()

    fun unsubscribeResultDefault(title: String, message: String, websiteUrl: String): String =
        """
        <!doctype html>
        <html>
        <head><meta charset="utf-8"><meta name="viewport" content="width=device-width, initial-scale=1">
        <title>Unsubscribe</title></head>
        <body style="font-family: Arial, sans-serif; padding: 24px;">
          <h2>$title</h2>
          <p>$message</p>
          <p style="margin-top:16px;"><a href="$websiteUrl">Back to website</a></p>
        </body></html>
        """.trimIndent()
}
