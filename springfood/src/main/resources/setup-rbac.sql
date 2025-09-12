-- Script setup RBAC cho Spring Food Application
-- Chạy script này trong PostgreSQL database: food_app

-- ==========================================
-- 1. KIỂM TRA VÀ TẠO ROLES
-- ==========================================
-- Xem roles hiện có
SELECT * FROM role;

-- Tạo roles cơ bản nếu chưa có
INSERT INTO role (role_name, description, created_at, updated_at) 
VALUES 
    ('ADMIN', 'Administrator với toàn quyền hệ thống', NOW(), NOW()),
    ('CUSTOMER', 'Khách hàng thông thường', NOW(), NOW()),
    ('SHOP_OWNER', 'Chủ cửa hàng', NOW(), NOW()),
    ('STAFF', 'Nhân viên', NOW(), NOW())
ON CONFLICT (role_name) DO UPDATE 
SET description = EXCLUDED.description,
    updated_at = NOW();

-- ==========================================
-- 2. KIỂM TRA VÀ TẠO PERMISSIONS
-- ==========================================
-- Xem permissions hiện có
SELECT * FROM permission;

-- Tạo permissions chi tiết
INSERT INTO permission (permission_name, description, created_at, updated_at)
VALUES 
    -- Product permissions
    ('VIEW_PRODUCT', 'Xem thông tin sản phẩm', NOW(), NOW()),
    ('CREATE_PRODUCT', 'Tạo sản phẩm mới', NOW(), NOW()),
    ('UPDATE_PRODUCT', 'Cập nhật thông tin sản phẩm', NOW(), NOW()),
    ('DELETE_PRODUCT', 'Xóa sản phẩm', NOW(), NOW()),
    
    -- User management permissions
    ('VIEW_USERS', 'Xem danh sách người dùng', NOW(), NOW()),
    ('MANAGE_USERS', 'Quản lý toàn bộ người dùng', NOW(), NOW()),
    ('UPDATE_USER', 'Cập nhật thông tin người dùng', NOW(), NOW()),
    ('DELETE_USER', 'Xóa người dùng', NOW(), NOW()),
    
    -- Order permissions
    ('VIEW_ORDERS', 'Xem đơn hàng', NOW(), NOW()),
    ('CREATE_ORDER', 'Tạo đơn hàng mới', NOW(), NOW()),
    ('UPDATE_ORDER', 'Cập nhật đơn hàng', NOW(), NOW()),
    ('MANAGE_ORDERS', 'Quản lý toàn bộ đơn hàng', NOW(), NOW()),
    ('CANCEL_ORDER', 'Hủy đơn hàng', NOW(), NOW()),
    
    -- Shop permissions
    ('VIEW_SHOP', 'Xem thông tin cửa hàng', NOW(), NOW()),
    ('CREATE_SHOP', 'Tạo cửa hàng mới', NOW(), NOW()),
    ('UPDATE_SHOP', 'Cập nhật thông tin cửa hàng', NOW(), NOW()),
    ('DELETE_SHOP', 'Xóa cửa hàng', NOW(), NOW()),
    ('MANAGE_SHOP', 'Quản lý toàn bộ cửa hàng', NOW(), NOW()),
    
    -- Category permissions
    ('VIEW_CATEGORY', 'Xem danh mục', NOW(), NOW()),
    ('CREATE_CATEGORY', 'Tạo danh mục mới', NOW(), NOW()),
    ('UPDATE_CATEGORY', 'Cập nhật danh mục', NOW(), NOW()),
    ('DELETE_CATEGORY', 'Xóa danh mục', NOW(), NOW()),
    
    -- Payment permissions
    ('VIEW_PAYMENT', 'Xem thông tin thanh toán', NOW(), NOW()),
    ('CREATE_PAYMENT', 'Tạo thanh toán', NOW(), NOW()),
    ('MANAGE_PAYMENTS', 'Quản lý toàn bộ thanh toán', NOW(), NOW()),
    
    -- Feedback permissions
    ('VIEW_FEEDBACK', 'Xem phản hồi', NOW(), NOW()),
    ('CREATE_FEEDBACK', 'Tạo phản hồi', NOW(), NOW()),
    ('DELETE_FEEDBACK', 'Xóa phản hồi', NOW(), NOW()),
    
    -- Report permissions
    ('VIEW_REPORTS', 'Xem báo cáo', NOW(), NOW()),
    ('GENERATE_REPORTS', 'Tạo báo cáo', NOW(), NOW())
ON CONFLICT (permission_name) DO UPDATE 
SET description = EXCLUDED.description,
    updated_at = NOW();

-- ==========================================
-- 3. GÁN PERMISSIONS CHO ROLES
-- ==========================================

-- Xóa permissions cũ (optional - chỉ dùng nếu muốn reset)
-- DELETE FROM role_has_permission;

-- ADMIN - có tất cả permissions
INSERT INTO role_has_permission (role_name, permission_name, created_at, updated_at)
SELECT 'ADMIN', permission_name, NOW(), NOW()
FROM permission
ON CONFLICT DO NOTHING;

-- CUSTOMER - permissions cơ bản
DELETE FROM role_has_permission WHERE role_name = 'CUSTOMER';
INSERT INTO role_has_permission (role_name, permission_name, created_at, updated_at)
VALUES 
    ('CUSTOMER', 'VIEW_PRODUCT', NOW(), NOW()),
    ('CUSTOMER', 'VIEW_CATEGORY', NOW(), NOW()),
    ('CUSTOMER', 'VIEW_SHOP', NOW(), NOW()),
    ('CUSTOMER', 'CREATE_ORDER', NOW(), NOW()),
    ('CUSTOMER', 'VIEW_ORDERS', NOW(), NOW()),
    ('CUSTOMER', 'CANCEL_ORDER', NOW(), NOW()),
    ('CUSTOMER', 'CREATE_PAYMENT', NOW(), NOW()),
    ('CUSTOMER', 'VIEW_PAYMENT', NOW(), NOW()),
    ('CUSTOMER', 'CREATE_FEEDBACK', NOW(), NOW()),
    ('CUSTOMER', 'VIEW_FEEDBACK', NOW(), NOW())
ON CONFLICT DO NOTHING;

-- SHOP_OWNER - quản lý shop và sản phẩm
DELETE FROM role_has_permission WHERE role_name = 'SHOP_OWNER';
INSERT INTO role_has_permission (role_name, permission_name, created_at, updated_at)
VALUES 
    ('SHOP_OWNER', 'VIEW_PRODUCT', NOW(), NOW()),
    ('SHOP_OWNER', 'CREATE_PRODUCT', NOW(), NOW()),
    ('SHOP_OWNER', 'UPDATE_PRODUCT', NOW(), NOW()),
    ('SHOP_OWNER', 'DELETE_PRODUCT', NOW(), NOW()),
    ('SHOP_OWNER', 'VIEW_CATEGORY', NOW(), NOW()),
    ('SHOP_OWNER', 'VIEW_SHOP', NOW(), NOW()),
    ('SHOP_OWNER', 'UPDATE_SHOP', NOW(), NOW()),
    ('SHOP_OWNER', 'VIEW_ORDERS', NOW(), NOW()),
    ('SHOP_OWNER', 'UPDATE_ORDER', NOW(), NOW()),
    ('SHOP_OWNER', 'VIEW_PAYMENT', NOW(), NOW()),
    ('SHOP_OWNER', 'VIEW_FEEDBACK', NOW(), NOW()),
    ('SHOP_OWNER', 'VIEW_REPORTS', NOW(), NOW())
ON CONFLICT DO NOTHING;

-- STAFF - nhân viên với quyền hạn chế
DELETE FROM role_has_permission WHERE role_name = 'STAFF';
INSERT INTO role_has_permission (role_name, permission_name, created_at, updated_at)
VALUES 
    ('STAFF', 'VIEW_PRODUCT', NOW(), NOW()),
    ('STAFF', 'UPDATE_PRODUCT', NOW(), NOW()),
    ('STAFF', 'VIEW_CATEGORY', NOW(), NOW()),
    ('STAFF', 'VIEW_ORDERS', NOW(), NOW()),
    ('STAFF', 'UPDATE_ORDER', NOW(), NOW()),
    ('STAFF', 'VIEW_PAYMENT', NOW(), NOW()),
    ('STAFF', 'VIEW_FEEDBACK', NOW(), NOW())
ON CONFLICT DO NOTHING;

-- ==========================================
-- 4. KIỂM TRA KẾT QUẢ
-- ==========================================

-- Xem tổng quan roles và số lượng permissions
SELECT 
    r.role_name,
    r.description,
    COUNT(rhp.permission_name) as permission_count
FROM role r
LEFT JOIN role_has_permission rhp ON r.role_name = rhp.role_name
GROUP BY r.role_name, r.description
ORDER BY r.role_name;

-- Xem chi tiết permissions của từng role
SELECT 
    r.role_name,
    STRING_AGG(rhp.permission_name, ', ' ORDER BY rhp.permission_name) as permissions
FROM role r
LEFT JOIN role_has_permission rhp ON r.role_name = rhp.role_name
GROUP BY r.role_name
ORDER BY r.role_name;

-- ==========================================
-- 5. TẠO USER TEST (OPTIONAL)
-- ==========================================

-- Tạo user admin test (password: admin123)
INSERT INTO "user" (
    user_id, 
    username, 
    email, 
    password,  -- Cần mã hóa BCrypt, đây chỉ là placeholder
    "firstName", 
    "lastName", 
    status, 
    is_deleted,
    phone_verified,
    email_verified,
    created_at, 
    updated_at
)
VALUES (
    gen_random_uuid()::text,
    'admin',
    'admin@springfood.com',
    '$2a$10$DowJonesIndustrialAverage',  -- Thay bằng password đã mã hóa
    'Admin',
    'System',
    'ACTIVE',
    false,
    true,
    true,
    NOW(),
    NOW()
)
ON CONFLICT (username) DO NOTHING;

-- Gán role ADMIN cho user admin
INSERT INTO user_has_role (user_id, role_name, created_at, updated_at)
SELECT user_id, 'ADMIN', NOW(), NOW()
FROM "user"
WHERE username = 'admin'
ON CONFLICT DO NOTHING;

-- Tạo user customer test (password: customer123)
INSERT INTO "user" (
    user_id, 
    username, 
    email, 
    password,  -- Cần mã hóa BCrypt
    "firstName", 
    "lastName", 
    status, 
    is_deleted,
    phone_verified,
    email_verified,
    created_at, 
    updated_at
)
VALUES (
    gen_random_uuid()::text,
    'customer1',
    'customer1@springfood.com',
    '$2a$10$DowJonesIndustrialAverage',  -- Thay bằng password đã mã hóa
    'Customer',
    'One',
    'ACTIVE',
    false,
    false,
    false,
    NOW(),
    NOW()
)
ON CONFLICT (username) DO NOTHING;

-- Gán role CUSTOMER cho user customer1
INSERT INTO user_has_role (user_id, role_name, created_at, updated_at)
SELECT user_id, 'CUSTOMER', NOW(), NOW()
FROM "user"
WHERE username = 'customer1'
ON CONFLICT DO NOTHING;

-- ==========================================
-- 6. KIỂM TRA USERS VÀ ROLES
-- ==========================================

SELECT 
    u.username,
    u.email,
    u.status,
    STRING_AGG(uhr.role_name, ', ') as roles
FROM "user" u
LEFT JOIN user_has_role uhr ON u.user_id = uhr.user_id
GROUP BY u.user_id, u.username, u.email, u.status
ORDER BY u.created_at DESC
LIMIT 10;

-- Kiểm tra quyền của một user cụ thể
SELECT DISTINCT
    u.username,
    uhr.role_name,
    rhp.permission_name
FROM "user" u
JOIN user_has_role uhr ON u.user_id = uhr.user_id
JOIN role_has_permission rhp ON uhr.role_name = rhp.role_name
WHERE u.username IN ('admin', 'customer1')
ORDER BY u.username, uhr.role_name, rhp.permission_name;
