-- ============================================================================
-- SpringFood Database Seed Data - Master Execution Script
-- ============================================================================
-- This script executes all seed data SQL files in the correct dependency order
-- to ensure referential integrity across all schemas.
--
-- Execution Order:
--   1. Authentication Schema (Level 1-2): roles, users, user_has_role, address
--   2. Shop Schema (Level 3): shops, shop_members, shop_wallets
--   3. Product Schema (Level 4-6): categories, products, product_categories, 
--      product_images, feedbacks, sales, product_sales
--   4. Order Schema (Level 6): orders, order_items
--   5. Payment Schema (Level 7): payment_transactions
--   6. Media Schema (Level 7): media_file
--   7. Notification Schema (Level 7): notifications
--   8. Chat Schema (Level 7): conversation, conversation_participant, message
--
-- Prerequisites:
--   - All schemas must exist (authentication, shop, product, order, payment, 
--     media, notification, chat)
--   - PostgreSQL client with NeonDB connection
--   - Sufficient permissions to INSERT data
--
-- Usage:
--   psql -h <neondb-host> -U <username> -d <database> -f run_all_seeds.sql
--
-- ============================================================================

\set ON_ERROR_STOP on
\timing on

-- ============================================================================
-- LEVEL 1-2: Authentication Schema
-- ============================================================================
\echo ''
\echo '============================================================================'
\echo 'LEVEL 1-2: Executing Authentication Schema Seed Data'
\echo '============================================================================'
\echo 'Tables: role, user, user_has_role, address'
\echo 'Expected Records: 5 roles, 10 users, 10 user_has_role, 0+ addresses'
\echo ''

\i 01_springfood_authentication_seed_data.sql

\echo ''
\echo '✓ Authentication schema seed data completed'
\echo ''

-- ============================================================================
-- LEVEL 3: Shop Schema
-- ============================================================================
\echo ''
\echo '============================================================================'
\echo 'LEVEL 3: Executing Shop Schema Seed Data'
\echo '============================================================================'
\echo 'Tables: shops, shop_members, shop_wallets'
\echo 'Expected Records: 25+ shops, 0+ shop_members, 0+ shop_wallets'
\echo ''

\i 02_springfood_shop_seed_data.sql

\echo ''
\echo '✓ Shop schema seed data completed'
\echo ''

-- ============================================================================
-- LEVEL 4-6: Product Schema
-- ============================================================================
\echo ''
\echo '============================================================================'
\echo 'LEVEL 4-6: Executing Product Schema Seed Data'
\echo '============================================================================'
\echo 'Tables: categories, products, product_categories, product_images,'
\echo '        feedbacks, sales, product_sales'
\echo 'Expected Records: 50+ categories, 150+ products, 150+ product_categories,'
\echo '                  0+ product_images, 0+ feedbacks, 15-20 sales,'
\echo '                  100+ product_sales'
\echo ''

\i 03_springfood_product_seed_data.sql

\echo ''
\echo '✓ Product schema seed data completed'
\echo ''

-- ============================================================================
-- LEVEL 6: Order Schema
-- ============================================================================
\echo ''
\echo '============================================================================'
\echo 'LEVEL 6: Executing Order Schema Seed Data'
\echo '============================================================================'
\echo 'Tables: orders, order_items'
\echo 'Expected Records: 10+ orders, 20+ order_items'
\echo ''

\i 04_springfood_order_seed_data.sql

\echo ''
\echo '✓ Order schema seed data completed'
\echo ''

-- ============================================================================
-- LEVEL 7: Payment Schema
-- ============================================================================
\echo ''
\echo '============================================================================'
\echo 'LEVEL 7: Executing Payment Schema Seed Data'
\echo '============================================================================'
\echo 'Tables: payment_transactions'
\echo 'Expected Records: 10+ payment_transactions'
\echo ''

-- Check if payment seed file exists
\set payment_file '05_springfood_payment_seed_data.sql'
\if :{?payment_file}
    \i 05_springfood_payment_seed_data.sql
    \echo ''
    \echo '✓ Payment schema seed data completed'
\else
    \echo '⚠ Payment seed file not found - skipping'
\endif
\echo ''

-- ============================================================================
-- LEVEL 7: Media Schema
-- ============================================================================
\echo ''
\echo '============================================================================'
\echo 'LEVEL 7: Executing Media Schema Seed Data'
\echo '============================================================================'
\echo 'Tables: media_file'
\echo 'Expected Records: 10+ media_file'
\echo ''

-- Check if media seed file exists
\set media_file '06_springfood_media_seed_data.sql'
\if :{?media_file}
    \i 06_springfood_media_seed_data.sql
    \echo ''
    \echo '✓ Media schema seed data completed'
\else
    \echo '⚠ Media seed file not found - skipping'
\endif
\echo ''

-- ============================================================================
-- LEVEL 7: Notification Schema
-- ============================================================================
\echo ''
\echo '============================================================================'
\echo 'LEVEL 7: Executing Notification Schema Seed Data'
\echo '============================================================================'
\echo 'Tables: notifications'
\echo 'Expected Records: 10+ notifications'
\echo ''

-- Check if notification seed file exists
\set notification_file '07_springfood_notification_seed_data.sql'
\if :{?notification_file}
    \i 07_springfood_notification_seed_data.sql
    \echo ''
    \echo '✓ Notification schema seed data completed'
\else
    \echo '⚠ Notification seed file not found - skipping'
\endif
\echo ''

-- ============================================================================
-- LEVEL 7: Chat Schema
-- ============================================================================
\echo ''
\echo '============================================================================'
\echo 'LEVEL 7: Executing Chat Schema Seed Data'
\echo '============================================================================'
\echo 'Tables: conversation, conversation_participant, message'
\echo 'Expected Records: 10+ conversations, 20+ participants, 50+ messages'
\echo ''

-- Check if chat seed file exists
\set chat_file '08_springfood_chat_seed_data.sql'
\if :{?chat_file}
    \i 08_springfood_chat_seed_data.sql
    \echo ''
    \echo '✓ Chat schema seed data completed'
\else
    \echo '⚠ Chat seed file not found - skipping'
\endif
\echo ''

-- ============================================================================
-- COMPLETION SUMMARY
-- ============================================================================
\echo ''
\echo '============================================================================'
\echo '✓✓✓ ALL SEED DATA EXECUTION COMPLETED SUCCESSFULLY ✓✓✓'
\echo '============================================================================'
\echo ''
\echo 'Schemas populated:'
\echo '  ✓ Authentication (roles, users, user_has_role, address)'
\echo '  ✓ Shop (shops, shop_members, shop_wallets)'
\echo '  ✓ Product (categories, products, product_categories, product_images,'
\echo '            feedbacks, sales, product_sales)'
\echo '  ✓ Order (orders, order_items)'
\echo '  ✓ Payment (payment_transactions) - if file exists'
\echo '  ✓ Media (media_file) - if file exists'
\echo '  ✓ Notification (notifications) - if file exists'
\echo '  ✓ Chat (conversation, conversation_participant, message) - if file exists'
\echo ''
\echo 'Next steps:'
\echo '  1. Verify record counts: SELECT COUNT(*) FROM <schema>.<table>;'
\echo '  2. Test predefined user accounts (see README.md for credentials)'
\echo '  3. Verify referential integrity: Check foreign key constraints'
\echo '  4. Test application with seed data'
\echo ''
\echo '============================================================================'
\echo ''

\timing off
