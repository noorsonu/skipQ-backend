-- V27 Add email and password to users for Google Sign-In and Direct/Password authentication
ALTER TABLE users ADD COLUMN IF NOT EXISTS email VARCHAR(150);
ALTER TABLE users ADD COLUMN IF NOT EXISTS password VARCHAR(255);

CREATE INDEX IF NOT EXISTS idx_users_email ON users (email);
