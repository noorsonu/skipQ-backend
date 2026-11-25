-- =============================================================================
-- Migration: V30__purge_all_existing_data_and_reset_sequences.sql
-- Description: Complete purge of all application data, shops, users, tokens, and reset all sequences
-- =============================================================================

TRUNCATE TABLE 
    tokens,
    queue_sessions,
    shop_status_events,
    shop_hours,
    shop_claims,
    reviews,
    featured_slots,
    invoices,
    subscriptions,
    dispute_messages,
    disputes,
    notifications,
    device_tokens,
    daily_shop_metrics,
    analytics_events,
    audit_logs,
    shops,
    users
RESTART IDENTITY CASCADE;

-- Reset all auto-increment sequences back to 1
DO $$ 
DECLARE 
    seq RECORD;
BEGIN 
    FOR seq IN (SELECT sequence_name FROM information_schema.sequences WHERE sequence_schema = 'public') LOOP 
        EXECUTE 'ALTER SEQUENCE ' || quote_ident(seq.sequence_name) || ' RESTART WITH 1'; 
    END LOOP; 
END $$;
