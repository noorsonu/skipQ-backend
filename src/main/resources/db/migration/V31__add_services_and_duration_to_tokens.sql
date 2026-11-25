-- =============================================================================
-- Migration: V31__add_services_and_duration_to_tokens.sql
-- Description: Add selected_services and service_duration_min columns to tokens table
-- =============================================================================

ALTER TABLE tokens ADD COLUMN IF NOT EXISTS selected_services TEXT;
ALTER TABLE tokens ADD COLUMN IF NOT EXISTS service_duration_min INTEGER NOT NULL DEFAULT 15;
