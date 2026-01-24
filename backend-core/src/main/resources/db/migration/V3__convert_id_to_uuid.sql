-- Convert id column from text/varchar to uuid
ALTER TABLE subscribers
ALTER COLUMN id TYPE uuid
  USING id::uuid;

-- Convert created_at from varchar/text to timestamptz
ALTER TABLE subscribers
ALTER COLUMN created_at TYPE timestamptz
  USING created_at::timestamptz;

-- Drop obsolete column
ALTER TABLE subscribers
DROP COLUMN IF EXISTS confirmed_at;