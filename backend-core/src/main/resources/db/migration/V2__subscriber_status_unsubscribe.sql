ALTER TABLE subscribers
    ADD COLUMN IF NOT EXISTS status TEXT NOT NULL DEFAULT 'PENDING',
    ADD COLUMN IF NOT EXISTS unsubscribed_at TIMESTAMPTZ NULL;

CREATE INDEX IF NOT EXISTS idx_subscribers_status ON subscribers(status);