-- ============================================
-- MASTER SCRIPT - CHẠY TẤT CẢ SEED DATA
-- ============================================
-- Script này chạy tất cả các bước cần thiết:
-- 1. Fix schema issues
-- 2. Cleanup old data
-- 3. Import new seed data

\echo '============================================'
\echo 'BƯỚC 1: FIX SCHEMA ISSUES'
\echo '============================================'

-- Fix avg_star field in shops table (run outside transaction)
-- Check if column needs fixing
DO $$
BEGIN
  -- Check current precision
  IF EXISTS (
    SELECT 1 
    FROM information_schema.columns 
    WHERE table_schema = 'springfood_shop' 
      AND table_name = 'shops' 
      AND column_name = 'avg_star'
      AND numeric_precision = 2
  ) THEN
    -- Fix the column type
    ALTER TABLE springfood_shop.shops 
    ALTER COLUMN avg_star TYPE numeric(3, 2);
    
    RAISE NOTICE '✓ Fixed shops.avg_star schema (numeric(2,2) -> numeric(3,2))';
  ELSE
    RAISE NOTICE '✓ shops.avg_star schema already correct';
  END IF;
END $$;

\echo ''

\echo '============================================'
\echo 'BƯỚC 2: CLEANUP OLD DATA'
\echo '============================================'

\i 00_cleanup_all_data.sql

\echo ''
\echo '============================================'
\echo 'BƯỚC 3: IMPORT SEED DATA'
\echo '============================================'

\echo 'Importing authentication data (roles, users, user_has_role)...'
\i 01_springfood_authentication_seed_data.sql

\echo 'Importing shop data (shops, shop_members, shop_wallets)...'
\i 02_springfood_shop_seed_data.sql

\echo 'Importing product data (categories, products, sales)...'
\i 03_springfood_product_seed_data.sql

\echo 'Importing order data (orders, order_items)...'
\i 04_springfood_order_seed_data.sql

\echo ''
\echo '============================================'
\echo 'BƯỚC 4: VERIFICATION'
\echo '============================================'

SELECT 
  'roles' as table_name, 
  COUNT(*) as record_count 
FROM springfood_authentication.role
UNION ALL
SELECT 'users', COUNT(*) FROM springfood_authentication.user
UNION ALL
SELECT 'user_has_role', COUNT(*) FROM springfood_authentication.user_has_role
UNION ALL
SELECT 'shops', COUNT(*) FROM springfood_shop.shops
UNION ALL
SELECT 'shop_members', COUNT(*) FROM springfood_shop.shop_members
UNION ALL
SELECT 'shop_wallets', COUNT(*) FROM springfood_shop.shop_wallets
UNION ALL
SELECT 'categories', COUNT(*) FROM springfood_product.categories
UNION ALL
SELECT 'products', COUNT(*) FROM springfood_product.products
UNION ALL
SELECT 'product_categories', COUNT(*) FROM springfood_product.product_categories
UNION ALL
SELECT 'sales', COUNT(*) FROM springfood_product.sales
UNION ALL
SELECT 'product_sales', COUNT(*) FROM springfood_product.product_sales
UNION ALL
SELECT 'orders', COUNT(*) FROM springfood_order.orders
UNION ALL
SELECT 'order_items', COUNT(*) FROM springfood_order.order_items
ORDER BY table_name;

\echo ''
\echo '============================================'
\echo '✓ HOÀN THÀNH!'
\echo '============================================'
\echo 'Tổng số records đã import:'
\echo '  - Authentication: 207 records (5 roles + 101 users + 101 user_has_role)'
\echo '  - Shop: 145 records (27 shops + 91 shop_members + 27 shop_wallets)'
\echo '  - Product: 755 records'
\echo '  - Order: 54 records'
\echo ''
\echo 'Thông tin đăng nhập:'
\echo '  - Admin: admin1@springfood.vn / Password123!'
\echo '  - Shop Owner: shop_owner1@springfood.vn / Password123!'
\echo '  - Staff: staff1@springfood.vn / Password123!'
\echo '  - Customer: customer1@springfood.vn / Password123!'
\echo '  - Employees: employee1@springfood.vn / Employee123!'
\echo ''
\echo 'Xem chi tiết:'
\echo '  - EMPLOYEE-CREDENTIALS.md'
\echo '  - SHOP-SCHEMA-SUMMARY.md'
\echo '============================================'
