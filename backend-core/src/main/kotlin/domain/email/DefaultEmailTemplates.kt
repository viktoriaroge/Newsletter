package com.viroge.newsletter.domain.email

object DefaultEmailTemplates {
    const val WELCOME_FALLBACK_HTML =
        """
        <!doctype html>
        <html><body style="font-family: Arial, sans-serif;">
          <h2>Hello 👋</h2>
          <p>Thank you for subscribing. Find your download here: <a href="{{PDF_URL}}">{{PDF_URL}}</a></p>
          <p><a href="{{UNSUBSCRIBE_URL}}">Unsubscribe</a></p>
        </body></html>
        """
}
