-- =============================================================================
-- Migration: V29__purge_all_data_for_clean_testing.sql
-- Description: Complete database purge and clean slate for user testing
-- =============================================================================

TRUNCATE TABLE analytics_events, disputes, featured_slots, invoices, notifications, 
               queue_sessions, reviews, shop_claims, shop_hours, shop_status_events, 
               subscriptions, tokens, shops, users CASCADE;
