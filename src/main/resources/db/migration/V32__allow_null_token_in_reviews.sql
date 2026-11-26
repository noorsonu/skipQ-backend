-- =============================================================================
-- Migration: V32__allow_null_token_in_reviews.sql
-- Description: Allow direct reviews without compulsory token_id and remove unique constraint
-- =============================================================================

ALTER TABLE reviews ALTER COLUMN token_id DROP NOT NULL;
ALTER TABLE reviews DROP CONSTRAINT IF EXISTS reviews_token_id_key;
