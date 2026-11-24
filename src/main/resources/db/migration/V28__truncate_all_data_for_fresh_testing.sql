-- =============================================================================
-- Migration: V28__truncate_all_data_for_fresh_testing.sql
-- Description: Complete clean slate truncate for fresh user testing
-- =============================================================================

TRUNCATE TABLE analytics_events, disputes, featured_slots, invoices, notifications, 
               queue_sessions, reviews, shop_claims, shop_hours, shop_status_events, 
               subscriptions, tokens, shops, users CASCADE;
