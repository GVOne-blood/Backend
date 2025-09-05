-- =============================================================================
-- === SCRIPT TẠO VÀ KHỞI TẠO DỮ LIỆU CHO DATABASE FOOD_APP
-- === Tác giả: Dựa trên thiết kế của bạn và được tinh chỉnh
-- === PostgreSQL Version: 12+
-- =============================================================================

-- Hướng dẫn sử dụng:
-- 1. Mở psql hoặc một công cụ quản trị PostgreSQL (như DBeaver, pgAdmin).
-- 2. Tạo một database mới, ví dụ: CREATE DATABASE food_app;
-- 3. Kết nối tới database vừa tạo: \c food_app
-- 4. Sao chép và chạy toàn bộ script này.

-- =============================================================================
-- === PHẦN 1: DỌN DẸP VÀ THIẾT LẬP MÔI TRƯỜNG
-- =============================================================================

-- Xóa các bảng theo thứ tự ngược lại của sự phụ thuộc để tránh lỗi khóa ngoại
DROP TABLE IF EXISTS "token";
DROP TABLE IF EXISTS "bank_account";
DROP TABLE IF EXISTS "wallet_transaction";
DROP TABLE IF EXISTS "shop_wallet";
DROP TABLE IF EXISTS "notification";
DROP TABLE IF EXISTS "product_category";
DROP TABLE IF EXISTS "booking_item";
DROP TABLE IF EXISTS "booking";
DROP TABLE IF EXISTS "address";
DROP TABLE IF EXISTS "payment";
DROP TABLE IF EXISTS "product_sale";
DROP TABLE IF EXISTS "sale";
DROP TABLE IF EXISTS "product_tag";
DROP TABLE IF EXISTS "tag";
DROP TABLE IF EXISTS "feedback";
DROP TABLE IF EXISTS "product";
DROP TABLE IF EXISTS "shop";
DROP TABLE IF EXISTS "post";
DROP TABLE IF EXISTS "role_has_permission";
DROP TABLE IF EXISTS "user_has_role";
DROP TABLE IF EXISTS "permission";
DROP TABLE IF EXISTS "role";
DROP TABLE IF EXISTS "category";
DROP TABLE IF EXISTS "user";

-- Xóa các kiểu ENUM nếu đã tồn tại
DROP TYPE IF EXISTS user_status;
DROP TYPE IF EXISTS post_status;
DROP TYPE IF EXISTS shop_status;
DROP TYPE IF EXISTS product_status;
DROP TYPE IF EXISTS booking_status;
DROP TYPE IF EXISTS notification_type;
DROP TYPE IF EXISTS transaction_type;
DROP TYPE IF EXISTS transaction_status;
DROP TYPE IF EXISTS token_type;

-- =============================================================================
-- === PHẦN 2: ĐỊNH NGHĨA CÁC KIỂU DỮ LIỆU TÙY CHỈNH (ENUMs)
-- =============================================================================
CREATE TYPE user_status AS ENUM ('ACTIVE', 'INACTIVE', 'BANNED');
CREATE TYPE post_status AS ENUM ('DRAFT', 'PUBLISHED', 'ARCHIVED');
CREATE TYPE shop_status AS ENUM ('ACTIVE', 'INACTIVE', 'PENDING_APPROVAL', 'BANNED');
CREATE TYPE product_status AS ENUM ('AVAILABLE', 'OUT_OF_STOCK', 'UNLISTED');
CREATE TYPE booking_status AS ENUM ('PENDING', 'PROCESSING', 'SHIPPED', 'COMPLETED', 'CANCELLED');
CREATE TYPE notification_type AS ENUM ('SYSTEM', 'PROMOTION', 'ORDER_UPDATE');
CREATE TYPE transaction_type AS ENUM ('DEPOSIT', 'WITHDRAWAL', 'ADJUSTMENT', 'PAYMENT');
CREATE TYPE transaction_status AS ENUM ('PENDING', 'COMPLETED', 'FAILED', 'CANCELLED');
CREATE TYPE token_type AS ENUM ('BEARER', 'REFRESH');


-- =============================================================================
-- === PHẦN 3: TẠO CẤU TRÚC CÁC BẢNG (CREATE TABLES)
-- =============================================================================

CREATE TABLE "user" (
  "user_id" varchar PRIMARY KEY,
  "firstName" varchar,
  "lastName" varchar,
  "email" varchar UNIQUE NOT NULL,
  "email_verified" bool DEFAULT false,
  "status" user_status,
  "phone" varchar(15) UNIQUE,
  "phone_verified" bool DEFAULT false,
  "address" varchar,
  "dob" date,
  "username" varchar UNIQUE NOT NULL,
  "password" varchar NOT NULL,
  "avatar" varchar,
  "is_deleted" bool NOT NULL DEFAULT false,
  "last_login_at" timestamp,
  "updated_at" timestamp,
  "created_at" timestamp
);

CREATE TABLE "role" (
  "role_name" varchar PRIMARY KEY NOT NULL,
  "description" varchar NOT NULL
);

CREATE TABLE "permission" (
  "permission_name" varchar PRIMARY KEY NOT NULL,
  "description" varchar NOT NULL
);

CREATE TABLE "user_has_role" (
  "user_id" varchar,
  "role_name" varchar,
  "updated_at" timestamp,
  "created_at" timestamp,
  PRIMARY KEY ("user_id", "role_name")
);

CREATE TABLE "role_has_permission" (
  "role_name" varchar,
  "permission_name" varchar,
  "updated_at" timestamp,
  "created_at" timestamp,
  PRIMARY KEY ("role_name", "permission_name")
);

CREATE TABLE "post" (
  "post_id" varchar PRIMARY KEY,
  "title" varchar,
  "body" text,
  "user_id" varchar,
  "post_status" post_status,
  "updated_at" timestamp,
  "created_at" timestamp
);

CREATE TABLE "shop" (
  "shop_id" varchar PRIMARY KEY,
  "owner_id" varchar UNIQUE, -- Một người chỉ sở hữu 1 shop
  "shop_name" varchar NOT NULL,
  "logo" varchar,
  "total_product" int DEFAULT 0,
  "total_sold" int DEFAULT 0,
  "introduction" varchar,
  "shop_status" shop_status,
  "updated_at" timestamp,
  "created_at" timestamp
);

CREATE TABLE "product" (
  "product_id" varchar PRIMARY KEY,
  "shop_id" varchar NOT NULL,
  "name" varchar NOT NULL,
  "description" varchar,
  "MSG" date, -- Sửa từ datetime thành date
  "EXP" date, -- Sửa từ datetime thành date
  "product_status" product_status,
  "price" decimal(15,2) NOT NULL,
  "wholesale_price" decimal(15,2),
  "avg_rate" decimal(3,2) DEFAULT 0,
  "quantity" int NOT NULL DEFAULT 0,
  "images" jsonb, -- Sử dụng jsonb để lưu mảng ảnh sẽ linh hoạt hơn varchar
  "updated_at" timestamp,
  "created_at" timestamp
);

CREATE TABLE "feedback" (
  "feedback_id" varchar PRIMARY KEY,
  "user_id" varchar NOT NULL,
  "product_id" varchar NOT NULL,
  "rating" int,
  "content" varchar,
  "updated_at" timestamp,
  "created_at" timestamp
);

CREATE TABLE "tag" (
  "tag_name" varchar PRIMARY KEY,
  "tag_description" varchar,
  "updated_at" timestamp,
  "created_at" timestamp
);

CREATE TABLE "product_tag" (
  "product_id" varchar,
  "tag_name" varchar,
  PRIMARY KEY ("product_id", "tag_name")
);

CREATE TABLE "sale" (
  "sale_id" varchar PRIMARY KEY,
  "title" varchar NOT NULL,
  "discount_percentage" decimal(5,2),
  "discount_amount" decimal(15,2),
  "conditions" varchar,
  "start_date" timestamp,
  "end_date" timestamp,
  "updated_at" timestamp,
  "created_at" timestamp
);

CREATE TABLE "product_sale" (
  "product_id" varchar,
  "sale_id" varchar,
  PRIMARY KEY ("product_id", "sale_id")
);

CREATE TABLE "payment" (
  "payment_name" varchar PRIMARY KEY,
  "description" varchar,
  "is_active" boolean DEFAULT true
);

CREATE TABLE "address" (
  "address_id" varchar PRIMARY KEY,
  "ward" varchar,
  "street" varchar,
  "city" varchar,
  "details" varchar,
  "user_id" varchar,
  "updated_at" timestamp,
  "created_at" timestamp
);

CREATE TABLE "booking" (
  "booking_id" varchar PRIMARY KEY,
  "user_id" varchar NOT NULL,
  "booking_status" booking_status,
  "final_price" decimal(15,2) NOT NULL,
  "payment_method_name" varchar,
  "address_id" varchar,
  "customer_notes" text,
  "created_at" timestamp,
  "updated_at" timestamp
);

CREATE TABLE "booking_item" (
  "bill_id" varchar PRIMARY KEY,
  "booking_id" varchar NOT NULL,
  "product_id" varchar NOT NULL,
  "quantity" int NOT NULL,
  "price_at_booking" decimal(15,2) NOT NULL,
  "updated_at" timestamp,
  "created_at" timestamp
);

CREATE TABLE "category" (
  "category_name" varchar PRIMARY KEY,
  "description" varchar,
  "is_active" bool DEFAULT true,
  "created_at" timestamp,
  "updated_at" timestamp
);

CREATE TABLE "product_category" (
  "product_id" varchar,
  "category_name" varchar,
  PRIMARY KEY ("product_id", "category_name")
);

CREATE TABLE "notification" (
  "notification_id" varchar PRIMARY KEY,
  "user_id" varchar,
  "notify_type" notification_type,
  "title" varchar,
  "content" varchar,
  "link" varchar,
  "is_read" bool DEFAULT false,
  "created_at" timestamp
);

CREATE TABLE "shop_wallet" (
  "wallet_id" varchar PRIMARY KEY,
  "shop_id" varchar UNIQUE NOT NULL,
  "balance" decimal(15,2) DEFAULT 0,
  "pending_amount" decimal(15,2) DEFAULT 0,
  "locked_amount" decimal(15,2) DEFAULT 0,
  "created_at" timestamp,
  "updated_at" timestamp
);

CREATE TABLE "wallet_transaction" (
  "transaction_id" varchar PRIMARY KEY,
  "wallet_id" varchar NOT NULL,
  "transaction_code" varchar UNIQUE,
  "transaction_type" transaction_type NOT NULL,
  "amount" decimal(15,2) NOT NULL,
  "balance_before" decimal(15,2),
  "balance_after" decimal(15,2),
  "fee" decimal(15,2) DEFAULT 0,
  "status" transaction_status DEFAULT 'PENDING',
  "reference_type" varchar,
  "reference_id" varchar,
  "description" varchar,
  "created_at" timestamp,
  "updated_at" timestamp
);

CREATE TABLE "bank_account" (
  "account_id" varchar PRIMARY KEY,
  "user_id" varchar,
  "shop_id" varchar,
  "bank_name" varchar NOT NULL,
  "account_number" varchar NOT NULL,
  "account_holder_name" varchar NOT NULL,
  "is_default" bool DEFAULT false,
  "is_verified" bool DEFAULT false,
  "created_at" timestamp,
  "updated_at" timestamp,
  CONSTRAINT user_or_shop_check CHECK (("user_id" IS NOT NULL AND "shop_id" IS NULL) OR ("user_id" IS NULL AND "shop_id" IS NOT NULL))
);

CREATE TABLE "token" (
  "token" varchar PRIMARY KEY,
  "user_id" varchar NOT NULL,
  "token_type" token_type,
  "expires_at" timestamp NOT NULL, -- Thêm trường thời gian hết hạn
  "created_at" timestamp
);


-- =============================================================================
-- === PHẦN 4: THÊM KHÓA NGOẠI (FOREIGN KEYS) VÀ INDEX
-- =============================================================================

-- Cải tiến: Thêm ON DELETE CASCADE cho các bảng nối để tự động dọn dẹp
-- Thêm ON DELETE SET NULL cho các mối quan hệ không bắt buộc để giữ lại dữ liệu

ALTER TABLE "user_has_role" ADD FOREIGN KEY ("user_id") REFERENCES "user" ("user_id") ON DELETE CASCADE;
ALTER TABLE "user_has_role" ADD FOREIGN KEY ("role_name") REFERENCES "role" ("role_name") ON DELETE CASCADE;

ALTER TABLE "role_has_permission" ADD FOREIGN KEY ("role_name") REFERENCES "role" ("role_name") ON DELETE CASCADE;
ALTER TABLE "role_has_permission" ADD FOREIGN KEY ("permission_name") REFERENCES "permission" ("permission_name") ON DELETE CASCADE;

ALTER TABLE "post" ADD FOREIGN KEY ("user_id") REFERENCES "user" ("user_id") ON DELETE SET NULL;

ALTER TABLE "shop" ADD FOREIGN KEY ("owner_id") REFERENCES "user" ("user_id") ON DELETE SET NULL;

ALTER TABLE "product" ADD FOREIGN KEY ("shop_id") REFERENCES "shop" ("shop_id") ON DELETE CASCADE;

ALTER TABLE "feedback" ADD FOREIGN KEY ("user_id") REFERENCES "user" ("user_id") ON DELETE CASCADE;
ALTER TABLE "feedback" ADD FOREIGN KEY ("product_id") REFERENCES "product" ("product_id") ON DELETE CASCADE;

ALTER TABLE "product_tag" ADD FOREIGN KEY ("product_id") REFERENCES "product" ("product_id") ON DELETE CASCADE;
ALTER TABLE "product_tag" ADD FOREIGN KEY ("tag_name") REFERENCES "tag" ("tag_name") ON DELETE CASCADE;

ALTER TABLE "product_sale" ADD FOREIGN KEY ("product_id") REFERENCES "product" ("product_id") ON DELETE CASCADE;
ALTER TABLE "product_sale" ADD FOREIGN KEY ("sale_id") REFERENCES "sale" ("sale_id") ON DELETE CASCADE;

ALTER TABLE "address" ADD FOREIGN KEY ("user_id") REFERENCES "user" ("user_id") ON DELETE CASCADE;

ALTER TABLE "booking" ADD FOREIGN KEY ("user_id") REFERENCES "user" ("user_id") ON DELETE CASCADE;
ALTER TABLE "booking" ADD FOREIGN KEY ("payment_method_name") REFERENCES "payment" ("payment_name") ON DELETE SET NULL;
ALTER TABLE "booking" ADD FOREIGN KEY ("address_id") REFERENCES "address" ("address_id") ON DELETE SET NULL;

ALTER TABLE "booking_item" ADD FOREIGN KEY ("booking_id") REFERENCES "booking" ("booking_id") ON DELETE CASCADE;
ALTER TABLE "booking_item" ADD FOREIGN KEY ("product_id") REFERENCES "product" ("product_id") ON DELETE RESTRICT; -- Không cho xóa sản phẩm nếu đã có trong đơn hàng

ALTER TABLE "product_category" ADD FOREIGN KEY ("product_id") REFERENCES "product" ("product_id") ON DELETE CASCADE;
ALTER TABLE "product_category" ADD FOREIGN KEY ("category_name") REFERENCES "category" ("category_name") ON DELETE CASCADE;

ALTER TABLE "notification" ADD FOREIGN KEY ("user_id") REFERENCES "user" ("user_id") ON DELETE CASCADE;

ALTER TABLE "shop_wallet" ADD FOREIGN KEY ("shop_id") REFERENCES "shop" ("shop_id") ON DELETE CASCADE;

ALTER TABLE "wallet_transaction" ADD FOREIGN KEY ("wallet_id") REFERENCES "shop_wallet" ("wallet_id") ON DELETE CASCADE;

ALTER TABLE "bank_account" ADD FOREIGN KEY ("user_id") REFERENCES "user" ("user_id") ON DELETE CASCADE;
ALTER TABLE "bank_account" ADD FOREIGN KEY ("shop_id") REFERENCES "shop" ("shop_id") ON DELETE CASCADE;

ALTER TABLE "token" ADD FOREIGN KEY ("user_id") REFERENCES "user" ("user_id") ON DELETE CASCADE;

-- Tạo Index cho các cột khóa ngoại để tăng tốc độ truy vấn
CREATE INDEX ON "shop" ("owner_id");
CREATE INDEX ON "product" ("shop_id");
CREATE INDEX ON "booking" ("user_id");
CREATE INDEX ON "address" ("user_id");
CREATE INDEX ON "wallet_transaction" ("wallet_id");


-- =============================================================================
-- === PHẦN 5: THÊM CHÚ THÍCH CHO CÁC BẢNG (COMMENTS)
-- =============================================================================
COMMENT ON TABLE "post" IS 'Post for adv food, news, etc.';
COMMENT ON COLUMN "post"."body" IS 'Content of the post';
COMMENT ON TABLE "shop" IS 'Manage shops selling products';
COMMENT ON COLUMN "shop"."shop_status" IS 'e.g., Active, Inactive, Pending Approval';
COMMENT ON TABLE "product" IS 'Products sold by shops';
COMMENT ON COLUMN "product"."images" IS 'Store image URLs as a JSONB array';
COMMENT ON TABLE "feedback" IS 'Manage comments & feedback from user for a product';
COMMENT ON COLUMN "feedback"."rating" IS 'Rating from 1 to 5';
COMMENT ON TABLE "tag" IS 'Manage types/categories of all products';
COMMENT ON TABLE "product_tag" IS 'Many-to-many relationship between products and tags';
COMMENT ON TABLE "sale" IS 'Manage sale campaigns';
COMMENT ON TABLE "product_sale" IS 'Apply a sale campaign to specific products';
COMMENT ON TABLE "payment" IS 'Manage payment methods';
COMMENT ON TABLE "booking" IS 'The Invoice or Order Header';
COMMENT ON COLUMN "booking"."booking_status" IS 'pending, processing, shipped, completed, cancelled';
COMMENT ON TABLE "address" IS 'Manage shipping addresses';
COMMENT ON TABLE "booking_item" IS 'The Invoice Line Items or Order Details';
COMMENT ON TABLE "category" IS 'Manage types of products';
COMMENT ON TABLE "notification" IS 'Manage notifications for each user';
COMMENT ON TABLE "shop_wallet" IS 'Manage revenue of shops';
COMMENT ON TABLE "wallet_transaction" IS 'Manage transactions between shop & users';
COMMENT ON TABLE "bank_account" IS 'Manage bank accounts';
COMMENT ON TABLE "token" IS 'Manage authentication tokens';
COMMENT ON COLUMN "token"."token_type" IS 'BEARER, REFRESH';


-- =============================================================================
-- === PHẦN 6: CHÈN DỮ LIỆU MẪU (DATA SEEDING)
-- =============================================================================

-- Bảng "role"
INSERT INTO "role" ("role_name", "description") VALUES
('ADMIN', 'Quản trị viên hệ thống, có toàn quyền.'),
('SHOP_OWNER', 'Chủ cửa hàng, quản lý sản phẩm và đơn hàng của shop mình.'),
('CUSTOMER', 'Khách hàng, có thể mua sắm và quản lý đơn hàng cá nhân.');

-- Bảng "permission"
INSERT INTO "permission" ("permission_name", "description") VALUES
('user:read', 'Xem thông tin người dùng'),
('user:manage', 'Quản lý tất cả người dùng (khóa, mở khóa)'),
('shop:create', 'Tạo cửa hàng mới'),
('shop:read_own', 'Xem thông tin cửa hàng của mình'),
('shop:update_own', 'Cập nhật thông tin cửa hàng của mình'),
('shop:manage_all', 'Quản lý tất cả cửa hàng (phê duyệt, từ chối)'),
('product:create', 'Tạo sản phẩm mới cho cửa hàng của mình'),
('product:update_own', 'Cập nhật sản phẩm của cửa hàng mình'),
('product:delete_own', 'Xóa sản phẩm của cửa hàng mình'),
('order:create', 'Tạo đơn hàng mới (mua hàng)'),
('order:read_own', 'Xem đơn hàng của mình'),
('order:manage_shop', 'Quản lý đơn hàng của cửa hàng mình'),
('order:read_all', 'Xem tất cả đơn hàng trong hệ thống');

-- Bảng "payment"
INSERT INTO "payment" ("payment_name", "description", "is_active") VALUES
('COD', 'Thanh toán khi nhận hàng (Cash On Delivery)', true),
('MOMO', 'Thanh toán qua ví điện tử MoMo', true),
('VNPAY', 'Thanh toán qua cổng VNPAY (Thẻ ATM, QR Code)', true),
('ZALOPAY', 'Thanh toán qua ví điện tử ZaloPay', false);

-- Bảng "category"
INSERT INTO "category" ("category_name", "description", "is_active", "created_at", "updated_at") VALUES
('Thịt Tươi Sống', 'Các loại thịt heo, bò, gà tươi', true, NOW(), NOW()),
('Hải Sản', 'Các loại cá, tôm, mực tươi và đông lạnh', true, NOW(), NOW()),
('Rau Củ Quả', 'Rau củ quả sạch, hữu cơ', true, NOW(), NOW()),
('Đồ Ăn Vặt', 'Các loại bánh kẹo, snack', true, NOW(), NOW());

-- Bảng "tag"
INSERT INTO "tag" ("tag_name", "tag_description", "created_at", "updated_at") VALUES
('Hữu cơ', 'Sản phẩm đạt chuẩn hữu cơ', NOW(), NOW()),
('VietGAP', 'Sản phẩm đạt chuẩn VietGAP', NOW(), NOW()),
('Tươi sống', 'Sản phẩm tươi giao trong ngày', NOW(), NOW()),
('Khuyến mãi', 'Sản phẩm đang có chương trình giảm giá', NOW(), NOW());

-- Bảng "role_has_permission"
INSERT INTO "role_has_permission" ("role_name", "permission_name", "created_at", "updated_at") VALUES
('ADMIN', 'user:read', NOW(), NOW()), ('ADMIN', 'user:manage', NOW(), NOW()), ('ADMIN', 'shop:manage_all', NOW(), NOW()), ('ADMIN', 'order:read_all', NOW(), NOW()),
('SHOP_OWNER', 'shop:read_own', NOW(), NOW()), ('SHOP_OWNER', 'shop:update_own', NOW(), NOW()), ('SHOP_OWNER', 'product:create', NOW(), NOW()), ('SHOP_OWNER', 'product:update_own', NOW(), NOW()), ('SHOP_OWNER', 'product:delete_own', NOW(), NOW()), ('SHOP_OWNER', 'order:manage_shop', NOW(), NOW()),
('CUSTOMER', 'shop:create', NOW(), NOW()), ('CUSTOMER', 'order:create', NOW(), NOW()), ('CUSTOMER', 'order:read_own', NOW(), NOW());

-- Bảng "user"
INSERT INTO "user" ("user_id", "firstName", "lastName", "email", "email_verified", "status", "phone", "phone_verified", "address", "dob", "username", "password", "avatar", "is_deleted", "last_login_at", "updated_at", "created_at") VALUES
('user_admin_01', 'Admin', 'Hệ Thống', 'admin@foodapp.com', true, 'ACTIVE', '0909090909', true, '123 Admin Street, District 1, HCMC', '1990-01-01', 'admin', 'plain_password_for_dev', 'avatars/admin.png', false, NOW(), NOW(), NOW()),
('user_shop_owner_01', 'Bình', 'Trần Văn', 'binh.tran@shop.com', true, 'ACTIVE', '0987654321', true, '789 Hùng Vương, Hải Châu, Đà Nẵng', '1988-05-20', 'binh_shop', 'plain_password_for_dev', 'avatars/binh.png', false, NOW(), NOW(), NOW()),
('user_shop_owner_02', 'Dũng', 'Hoàng Trí', 'dung.hoang@shop.com', true, 'ACTIVE', '0978123456', true, '333 Võ Văn Tần, District 3, HCMC', '1992-11-15', 'dung_shop', 'plain_password_for_dev', 'avatars/dung.png', false, NOW(), NOW(), NOW()),
('user_customer_01', 'An', 'Nguyễn Văn', 'an.nguyen@customer.com', true, 'ACTIVE', '0397130501', true, '456 Lê Lợi, District 1, HCMC', '1995-08-10', 'nguyenvana', 'plain_password_for_dev', 'avatars/an.png', false, NOW(), NOW(), NOW()),
('user_customer_02', 'Lan', 'Lê Thị', 'lan.le@customer.com', true, 'ACTIVE', '0912345678', false, '101 Nguyễn Trãi, Thanh Xuân, Hà Nội', '1998-02-25', 'lethilan', 'plain_password_for_dev', 'avatars/lan.png', false, NOW(), NOW(), NOW()),
('user_customer_03', 'Cường', 'Phạm Văn', 'cuong.pham@customer.com', false, 'INACTIVE', '0905112233', false, '212 Trần Phú, District 5, HCMC', '2000-12-30', 'phamvancuong', 'plain_password_for_dev', NULL, false, NULL, NOW(), NOW()),
('user_customer_04', 'Linh', 'Vũ Thị', 'linh.vu@customer.com', true, 'BANNED', '0966778899', true, '555 Cầu Giấy, Cầu Giấy, Hà Nội', '1999-07-07', 'vuthilinh', 'plain_password_for_dev', 'avatars/linh.png', false, NOW(), NOW(), NOW());

-- Bảng "user_has_role"
INSERT INTO "user_has_role" ("user_id", "role_name", "created_at", "updated_at") VALUES
('user_admin_01', 'ADMIN', NOW(), NOW()),
('user_shop_owner_01', 'SHOP_OWNER', NOW(), NOW()), ('user_shop_owner_01', 'CUSTOMER', NOW(), NOW()),
('user_shop_owner_02', 'SHOP_OWNER', NOW(), NOW()), ('user_shop_owner_02', 'CUSTOMER', NOW(), NOW()),
('user_customer_01', 'CUSTOMER', NOW(), NOW()), ('user_customer_02', 'CUSTOMER', NOW(), NOW()), ('user_customer_03', 'CUSTOMER', NOW(), NOW()), ('user_customer_04', 'CUSTOMER', NOW(), NOW());

-- Bảng "shop"
INSERT INTO "shop" ("shop_id", "owner_id", "shop_name", "logo", "introduction", "shop_status", "created_at", "updated_at") VALUES
('shop_binhan_01', 'user_shop_owner_01', 'Bếp Nhà Bình An', 'logos/binhan.png', 'Cung cấp thực phẩm sạch, an toàn cho mọi gia đình.', 'ACTIVE', NOW(), NOW()),
('shop_dunghoang_02', 'user_shop_owner_02', 'Vườn Rau Sạch Dũng Hoàng', 'logos/dunghoang.png', 'Rau củ quả hữu cơ trồng tại Đà Lạt.', 'ACTIVE', NOW(), NOW()),
('shop_haisan_03', 'user_customer_01', 'Hải Sản Tươi Sống Phan Thiết', 'logos/haisan.png', 'Giao hàng tận nơi trong ngày.', 'PENDING_APPROVAL', NOW(), NOW());

-- Bảng "shop_wallet"
INSERT INTO "shop_wallet" ("wallet_id", "shop_id", "balance", "created_at", "updated_at") VALUES
('wallet_shop01', 'shop_binhan_01', 500000.00, NOW(), NOW()),
('wallet_shop02', 'shop_dunghoang_02', 120000.00, NOW(), NOW());

-- Bảng "bank_account"
INSERT INTO "bank_account" ("account_id", "shop_id", "user_id", "bank_name", "account_number", "account_holder_name", "is_default", "is_verified", "created_at", "updated_at") VALUES
('bank_shop01', 'shop_binhan_01', NULL, 'Vietcombank', '0011001234567', 'TRAN VAN BINH', true, true, NOW(), NOW());

-- Bảng "product"
INSERT INTO "product" ("product_id", "shop_id", "name", "description", "MSG", "EXP", "product_status", "price", "wholesale_price", "quantity", "images", "created_at", "updated_at") VALUES
('prod_01', 'shop_binhan_01', 'Thịt Ba Rọi Heo VietGAP', 'Thịt heo sạch, không chất tăng trọng. Gói 500g.', '2024-09-01', '2024-09-08', 'AVAILABLE', 85000.00, 75000.00, 50, '["images/prod01_1.jpg", "images/prod01_2.jpg"]'::jsonb, NOW(), NOW()),
('prod_02', 'shop_binhan_01', 'Ức Gà Phi Lê Không Xương', 'Thích hợp cho người tập gym, ăn kiêng. Gói 500g.', '2024-09-01', '2024-09-07', 'AVAILABLE', 55000.00, 48000.00, 100, '["images/prod02_1.jpg"]'::jsonb, NOW(), NOW()),
('prod_03', 'shop_binhan_01', 'Cá Diêu Hồng Tươi', 'Cá tươi sống mỗi ngày, làm sạch sẵn. Con khoảng 800g.', '2024-09-04', '2024-09-05', 'AVAILABLE', 70000.00, 60000.00, 30, '["images/prod03_1.jpg"]'::jsonb, NOW(), NOW()),
('prod_04', 'shop_binhan_01', 'Trứng Gà Ta Thả Vườn', 'Vỉ 10 trứng gà ta thả vườn.', '2024-08-20', '2024-09-20', 'OUT_OF_STOCK', 35000.00, 30000.00, 0, '["images/prod04_1.jpg"]'::jsonb, NOW(), NOW()),
('prod_05', 'shop_dunghoang_02', 'Cải Bó Xôi Hữu Cơ', 'Trồng theo tiêu chuẩn hữu cơ, không thuốc trừ sâu. Bó 300g.', '2024-09-03', '2024-09-10', 'AVAILABLE', 25000.00, 20000.00, 80, '["images/prod05_1.jpg"]'::jsonb, NOW(), NOW()),
('prod_06', 'shop_dunghoang_02', 'Xà Lách Romana Đà Lạt', 'Giòn ngọt, thích hợp làm salad. Cây 500g.', '2024-09-03', '2024-09-09', 'AVAILABLE', 30000.00, 25000.00, 120, '["images/prod06_1.jpg"]'::jsonb, NOW(), NOW()),
('prod_07', 'shop_dunghoang_02', 'Cà Chua Bi Hữu Cơ', 'Cà chua bi hữu cơ, vị ngọt thanh. Hộp 500g.', '2024-09-02', '2024-09-12', 'UNLISTED', 40000.00, 35000.00, 60, '["images/prod07_1.jpg"]'::jsonb, NOW(), NOW());

-- Bảng "product_category" & "product_tag"
INSERT INTO "product_category" ("product_id", "category_name") VALUES ('prod_01', 'Thịt Tươi Sống'), ('prod_02', 'Thịt Tươi Sống'), ('prod_03', 'Hải Sản'), ('prod_04', 'Thịt Tươi Sống'), ('prod_05', 'Rau Củ Quả'), ('prod_06', 'Rau Củ Quả'), ('prod_07', 'Rau Củ Quả');
INSERT INTO "product_tag" ("product_id", "tag_name") VALUES ('prod_01', 'VietGAP'), ('prod_01', 'Tươi sống'), ('prod_02', 'Tươi sống'), ('prod_03', 'Tươi sống'), ('prod_05', 'Hữu cơ'), ('prod_06', 'Hữu cơ');

-- Bảng "address"
INSERT INTO "address" ("address_id", "ward", "street", "city", "details", "user_id", "created_at", "updated_at") VALUES
('addr_cust01_01', 'Phường Bến Nghé', '456 Lê Lợi', 'Thành phố Hồ Chí Minh', 'Tòa nhà Bitexco, Lầu 10', 'user_customer_01', NOW(), NOW()),
('addr_cust01_02', 'Phường Thảo Điền', '12 Quốc Hương', 'Thành phố Hồ Chí Minh', 'Chung cư Masteri', 'user_customer_01', NOW(), NOW()),
('addr_cust02_01', 'Phường Thượng Đình', '101 Nguyễn Trãi', 'Hà Nội', 'Gần Royal City', 'user_customer_02', NOW(), NOW());

-- Bảng "booking"
INSERT INTO "booking" ("booking_id", "user_id", "booking_status", "final_price", "payment_method_name", "address_id", "customer_notes", "created_at", "updated_at") VALUES
('book_01', 'user_customer_01', 'COMPLETED', 140000.00, 'MOMO', 'addr_cust01_01', 'Giao hàng trong giờ hành chính.', '2024-08-10 09:30:00', '2024-08-11 14:00:00'),
('book_02', 'user_customer_02', 'PROCESSING', 55000.00, 'COD', 'addr_cust02_01', 'Vui lòng gọi trước khi giao.', '2024-08-11 11:00:00', '2024-08-11 11:05:00'),
('book_03', 'user_customer_01', 'CANCELLED', 70000.00, 'VNPAY', 'addr_cust01_02', 'Khách đổi ý.', '2024-08-12 15:00:00', '2024-08-12 18:00:00');

-- Bảng "booking_item"
INSERT INTO "booking_item" ("bill_id", "booking_id", "product_id", "quantity", "price_at_booking", "created_at", "updated_at") VALUES
('bitem_01', 'book_01', 'prod_01', 1, 85000.00, NOW(), NOW()), ('bitem_02', 'book_01', 'prod_02', 1, 55000.00, NOW(), NOW()),
('bitem_03', 'book_02', 'prod_05', 1, 25000.00, NOW(), NOW()), ('bitem_04', 'book_02', 'prod_06', 1, 30000.00, NOW(), NOW()),
('bitem_05', 'book_03', 'prod_03', 1, 70000.00, NOW(), NOW());

-- Bảng "feedback"
INSERT INTO "feedback" ("feedback_id", "user_id", "product_id", "rating", "content", "created_at", "updated_at") VALUES
('feed_01', 'user_customer_01', 'prod_01', 5, 'Thịt rất tươi và ngon, đóng gói cẩn thận. Sẽ ủng hộ shop tiếp!', NOW(), NOW()),
('feed_02', 'user_customer_01', 'prod_02', 4, 'Ức gà chắc thịt, nhưng giao hàng hơi chậm một chút.', NOW(), NOW());

-- Bảng "wallet_transaction"
INSERT INTO "wallet_transaction" ("transaction_id", "wallet_id", "transaction_code", "transaction_type", "amount", "balance_before", "balance_after", "fee", "status", "reference_type", "reference_id", "description", "created_at", "updated_at") VALUES
('trans_book01_p1', 'wallet_shop01', 'TRN001', 'DEPOSIT', 85000.00, 500000.00, 580750.00, 4250.00, 'COMPLETED', 'booking_item', 'bitem_01', 'Thanh toán cho đơn hàng book_01', '2024-08-11 14:01:00', '2024-08-11 14:01:00'),
('trans_book01_p2', 'wallet_shop01', 'TRN002', 'DEPOSIT', 55000.00, 580750.00, 633000.00, 2750.00, 'COMPLETED', 'booking_item', 'bitem_02', 'Thanh toán cho đơn hàng book_01', '2024-08-11 14:01:00', '2024-08-11 14:01:00');

-- Bảng "notification"
INSERT INTO "notification" ("notification_id", "user_id", "notify_type", "title", "content", "link", "is_read", "created_at") VALUES
('noti_01', 'user_customer_01', 'ORDER_UPDATE', 'Đơn hàng #book_01 đã hoàn thành', 'Cảm ơn bạn đã mua sắm. Vui lòng để lại đánh giá cho sản phẩm.', '/orders/book_01', false, '2024-08-11 14:02:00'),
('noti_02', 'user_shop_owner_02', 'ORDER_UPDATE', 'Bạn có đơn hàng mới #book_02', 'Khách hàng Lê Thị Lan vừa đặt một đơn hàng mới.', '/shop/orders/book_02', true, '2024-08-11 11:01:00');

-- =============================================================================
-- === KẾT THÚC SCRIPT ===
-- =============================================================================