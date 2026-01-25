ALTER TABLE subscribers
    ADD COLUMN IF NOT EXISTS last_welcome_sent_at timestamptz NULL;