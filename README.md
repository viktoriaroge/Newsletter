# Newsletter

![Made With Love](https://img.shields.io/badge/Made%20with-%E2%9D%A4-black)
![Open Source](https://img.shields.io/badge/Open%20Source-Yes-green)
![License](https://img.shields.io/badge/license-MIT-purple.svg)

![Maintained](https://img.shields.io/badge/Maintained-Yes-success)
![Status](https://img.shields.io/badge/status-Production%20Ready-blue.svg)

![Language](https://img.shields.io/badge/language-Kotlin-yellow.svg)
![Backend](https://img.shields.io/badge/backend-Ktor-orange.svg)
![Database](https://img.shields.io/badge/database-PostgreSQL-red.svg)

![Platform](https://img.shields.io/badge/deploy-Fly.io-purple.svg)
![Supabase](https://img.shields.io/badge/storage-Supabase-3ECF8E)
![Email](https://img.shields.io/badge/email-Resend-black.svg)

A lightweight, self-hostable newsletter and audience management backend built with Ktor, PostgreSQL, and Resend, designed for easy integration with website builders such as Squarespace.

This project provides:
- Newsletter subscriptions
- Welcome email delivery with downloadable content
- Unsubscribe flow with confirmation
- Admin management endpoints
- Rate limiting and bot protection
- Pluggable HTML templates (hosted remotely)
- Fly.io + Supabase deployment friendly

The goal is to offer a small, production-shaped system that is easy to understand, extend, and reuse.


## Features

- Public subscription endpoint
- Squarespace-friendly API
- Email delivery via Resend
- Unsubscribe links with secure tokens
- PostgreSQL persistence
- Flyway migrations
- Rate limiting
- Health checks
- Swagger / OpenAPI documentation
- Optional remote HTML templates
- Local development with Docker


## Architecture Overview
| Website (Squarespace / External Page) |
|:-------------------------------------:|
|                   v                   |
|    POST /v1/squarespace/subscribe     |
|                   v                   |
|         Ktor Backend (Fly.io)         |
|                   v                   |
|      -- Validate & Rate Limit --      |
|   -- Store subscriber (PENDING) --    |
|       -- Send welcome email --        |
|           -- Mark ACTIVE --           |
|                   v                   |
|         PostgreSQL (Supabase)         |

Squarespace never talks directly to:
- Database
- Email provider
- Templates

All logic stays inside the backend.


## Tech Stack
- Kotlin
- Ktor
- Exposed
- PostgreSQL
- Flyway
- Resend
- Supabase (Postgres + Storage)
- Fly.io
- Docker


## Local Development

1. Prerequisites:
   - Java 21+
   - Docker
   - Gradle

2. Start PostgreSQL

   ```
   docker compose up -d
   ```

3. Create `.env`

   Copy example:

   ```
   cp .env.example .env
   ```

   Fill required values.

4. Run Application

   `./gradlew :backend-core:run`

5. Run Tests

   `./gradlew test`

6. Swagger UI

   http://localhost:8080/swagger/

7. Health Endpoints

   `GET /health`

   `GET /health/db`


## Squarespace Integration

Use a custom HTML form or Code Block that sends:

```
POST https://<your-backend>/v1/squarespace/subscribe
Content-Type: application/json

{
"email": "user@example.com"
}
```

On success, backend returns:

```
{ "ok": true }
```

Squarespace never stores emails.


## API Endpoints
### Public

| Method | Path                      | Description                   |
|:-------|:--------------------------|:------------------------------|
| POST   | /v1/squarespace/subscribe | Subscribe email               |
| GET    | /unsubscribe              | Unsubscribe confirmation page |
| POST   | /v1/unsubscribe/confirm   | Confirm unsubscribe           |
| GET    | /health                   | App health                    |
| GET    | /health/db                | Database health               |


### Admin (requires header)
```
X-Admin-Token: <ADMIN_API_TOKEN>
```

| Method | Path                      | Description             |
|:-------|:--------------------------|:------------------------|
| POST   | /v1/subscriptions         | Create subscription     |
| GET    | /v1/subscriptions         | List subscribers        |
| GET    | /v1/subscriptions/{email} | Get subscriber by email |
| POST	  | /v1/admin/retry-pending	  | Retry PENDING emails    |



## Environment Variables

| Variable                         | Description                                                                          | Use                               |
|:---------------------------------|:-------------------------------------------------------------------------------------|:----------------------------------|
| JDBC_DATABASE_URL                | JDBC connection string for Postgres (Supabase or local)                              | Database                          |
| DB_USER                          | DB username                                                                          | Database                          |
| DB_PASSWORD                      | DB password                                                                          | Database                          |
| ADMIN_API_TOKEN                  | Admin token required for protected endpoints via header X-Admin-Token                | Admin / Security                  |
| UNSUBSCRIBE_SECRET               | Secret used to sign/verify unsubscribe tokens                                        | Admin / Security                  |
| PUBLIC_BASE_URL                  | Public base URL of this API (used for unsubscribe links), e.g. https://<app>.fly.dev | Public URLs                       |
| WEBSITE_URL                      | Your website URL shown on pages/emails                                               | Public URLs                       |
| PDF_URL                          | Public URL to the downloadable PDF                                                   | Public URLs                       |
| EMAIL_ENABLED                    | true / false (disable email sending for local dev/testing)                           | Email (Resend)                    |
| EMAIL_FROM                       | Verified sender, e.g. Your Name <noreply@yourdomain.com>                             | Email (Resend)                    |
| RESEND_API_KEY                   | Resend API key                                                                       | Email (Resend)                    |
| WELCOME_EMAIL_TEMPLATE_URL       | URL to hosted HTML email template                                                    | Templates (optional, recommended) |
| AUTHOR_LOGO_URL                  | URL to logo/banner image used in email                                               | Templates (optional, recommended) |
| UNSUBSCRIBE_CONFIRM_TEMPLATE_URL | URL to hosted HTML for /unsubscribe                                                  | Templates (optional, recommended) |
| UNSUBSCRIBE_RESULT_TEMPLATE_URL  | URL to hosted HTML for unsubscribe result page                                       | Templates (optional, recommended) |


## Templates

HTML templates may be hosted remotely (e.g. Supabase Storage).
If a template URL is missing or unreachable, built-in defaults are used.

Templates support placeholders:

```
{{PDF_URL}}
{{UNSUBSCRIBE_URL}}
{{WEBSITE_URL}}
{{LOGO_BLOCK}}
{{YEAR}}
```


## Storage

Supabase Storage is recommended for:

- PDF downloads
- Email logos
- HTML templates

Objects must be public.


## Testing Philosophy

- Repository and service layers tested
- HTTP routing tested
- Fake email sender used in tests
- No external network calls during tests


## Retry Strategy

If email sending fails:
- Subscriber remains PENDING
- Admin can call:
   ```
   POST /v1/admin/retry-pending
   ```


## Security Notes

- No secrets in repository
- Admin endpoints protected by header token
- Rate limiting on public endpoints
- Input validation
- Tokens are signed


## License

MIT License


## Why This Project Exists

This project was built as:
- A real-world backend learning exercise
- A reusable newsletter system
- A portfolio-quality reference implementation

Feel free to fork, modify, and reuse.