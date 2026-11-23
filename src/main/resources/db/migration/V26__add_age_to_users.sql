-- =============================================================================
-- Migration: V26__add_age_to_users.sql
-- Description: Add age column to users table
-- =============================================================================

ALTER TABLE users
    ADD COLUMN IF NOT EXISTS age INTEGER;
