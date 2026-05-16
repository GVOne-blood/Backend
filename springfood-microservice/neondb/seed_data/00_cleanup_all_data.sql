-- ============================================
-- CLEANUP SCRIPT - XÓA TẤT CẢ DỮ LIỆU SEED
-- ============================================
-- Chạy script này TRƯỚC KHI chạy lại các file seed data
-- Script này xóa dữ liệu theo thứ tự ngược lại (Level 7 → Level 1)
-- để tránh lỗi foreign key constraint

BEGIN;

-- ============================================
-- LEVEL 7: Chat, Notification, Media, Payment
-- ============================================

-- Chat schema
DELETE FROM springfood_chat.message WHERE 1=1;
DELETE FROM springfood_chat.conversation_participant WHERE 1=1;
DELETE FROM springfood_chat.conversation WHERE 1=1;

-- Notification schema
DELETE FROM springfood_notification.notifications WHERE 1=1;

-- Media schema
DELETE FROM springfood_media.media_file WHERE 1=1;

-- Payment schema
DELETE FROM springfood_payment.payment_transactions WHERE 1=1;

-- ============================================
-- LEVEL 6: Orders, Order Items, Product-Sales
-- ============================================

-- Order schema
DELETE FROM springfood_order.order_items WHERE 1=1;
DELETE FROM springfood_order.orders WHERE 1=1;

-- ============================================
-- LEVEL 5: Product-Sales, Product-Categories, Sales
-- ============================================

-- Product schema (junction tables và sales)
DELETE FROM springfood_product.product_sales WHERE 1=1;
DELETE FROM springfood_product.sales WHERE 1=1;
DELETE FROM springfood_product.product_categories WHERE 1=1;

-- ============================================
-- LEVEL 4: Products, Categories
-- ============================================

DELETE FROM springfood_product.products WHERE 1=1;
DELETE FROM springfood_product.categories WHERE 1=1;

-- ============================================
-- LEVEL 3: Shops
-- ============================================

-- Shop schema (delete in dependency order)
DELETE FROM springfood_shop.shop_wallets WHERE 1=1;
DELETE FROM springfood_shop.shop_members WHERE 1=1;
DELETE FROM springfood_shop.shops WHERE 1=1;

-- ============================================
-- LEVEL 2-1: Users, User-Role, Roles
-- ============================================

-- Authentication schema
DELETE FROM springfood_authentication.user_has_role WHERE 1=1;
DELETE FROM springfood_authentication.user WHERE 1=1;
DELETE FROM springfood_authentication.role WHERE 1=1;

COMMIT;

-- ============================================
-- VERIFICATION QUERIES
-- ============================================
-- Uncomment để kiểm tra sau khi xóa

-- SELECT 'roles' as table_name, COUNT(*) as count FROM springfood_authentication.role
-- UNION ALL
-- SELECT 'users', COUNT(*) FROM springfood_authentication.user
-- UNION ALL
-- SELECT 'user_has_role', COUNT(*) FROM springfood_authentication.user_has_role
-- UNION ALL
-- SELECT 'shops', COUNT(*) FROM springfood_shop.shops
-- UNION ALL
-- SELECT 'shop_members', COUNT(*) FROM springfood_shop.shop_members
-- UNION ALL
-- SELECT 'shop_wallets', COUNT(*) FROM springfood_shop.shop_wallets
-- UNION ALL
-- SELECT 'categories', COUNT(*) FROM springfood_product.categories
-- UNION ALL
-- SELECT 'products', COUNT(*) FROM springfood_product.products
-- UNION ALL
-- SELECT 'product_categories', COUNT(*) FROM springfood_product.product_categories
-- UNION ALL
-- SELECT 'sales', COUNT(*) FROM springfood_product.sales
-- UNION ALL
-- SELECT 'product_sales', COUNT(*) FROM springfood_product.product_sales
-- UNION ALL
-- SELECT 'orders', COUNT(*) FROM springfood_order.orders
-- UNION ALL
-- SELECT 'order_items', COUNT(*) FROM springfood_order.order_items;
