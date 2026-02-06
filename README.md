# Newsletter
Custom newsletter and audience management platform.

## Required Environment variables for Deploy

### Database

JDBC_DATABASE_URL — JDBC connection string for Postgres (Supabase or local)

DB_USER — DB username

DB_PASSWORD — DB password

### Admin

ADMIN_API_TOKEN — Admin token required for protected endpoints via header X-Admin-Token

UNSUBSCRIBE_SECRET — Secret used to sign/verify unsubscribe tokens

### Public URLs

PUBLIC_BASE_URL — Public base URL of this API (used for unsubscribe links), e.g. https://<app>.fly.dev

WEBSITE_URL — Your website URL shown on pages/emails

PDF_URL — Public URL to the downloadable PDF

### Email (Resend)

EMAIL_ENABLED — true|false (disable email sending for local dev/testing)

EMAIL_FROM — Verified sender, e.g. Your Name <noreply@yourdomain.com>

RESEND_API_KEY — Resend API key

### Remote templates (optional, recommended)

WELCOME_EMAIL_TEMPLATE_URL — URL to hosted HTML email template

AUTHOR_LOGO_URL — URL to logo/banner image used in email

UNSUBSCRIBE_CONFIRM_TEMPLATE_URL — URL to hosted HTML for /unsubscribe

UNSUBSCRIBE_RESULT_TEMPLATE_URL — URL to hosted HTML for unsubscribe result page