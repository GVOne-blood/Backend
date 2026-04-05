-- =====================================================
-- SQL Script for New Tables
-- SpringFood Microservices Database Schema
-- =====================================================

-- 1. Category Group Table (springfood_product schema)
CREATE TABLE IF NOT EXISTS springfood_product.category_group (
    group_id      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    group_code    VARCHAR(100) UNIQUE NOT NULL,  -- matches category_group_code in categories
    group_name    VARCHAR(255) NOT NULL,
    description   TEXT,
    icon_url      VARCHAR(500),
    display_order INTEGER DEFAULT 0,
    is_active     BOOLEAN DEFAULT true,
    created_at    TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

-- Create index for category_group
CREATE INDEX IF NOT EXISTS idx_category_group_code ON springfood_product.category_group(group_code);
CREATE INDEX IF NOT EXISTS idx_category_group_active ON springfood_product.category_group(is_active, display_order);

-- 2. Product View History Table (springfood_product schema)
CREATE TABLE IF NOT EXISTS springfood_product.product_view_history (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID NOT NULL,
    product_id  UUID NOT NULL,
    viewed_at   TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    session_id  VARCHAR(100),
    source      VARCHAR(50)  -- SEARCH, CATEGORY, RECOMMENDATION, DIRECT
);

-- Create indexes for product_view_history
CREATE INDEX IF NOT EXISTS idx_product_view_history_user_viewed ON springfood_product.product_view_history(user_id, viewed_at DESC);
CREATE INDEX IF NOT EXISTS idx_product_view_history_product ON springfood_product.product_view_history(product_id);
CREATE INDEX IF NOT EXISTS idx_product_view_history_viewed_at ON springfood_product.product_view_history(viewed_at);

-- 3. User Wishlist Table (springfood_product schema)
CREATE TABLE IF NOT EXISTS springfood_product.user_wishlist (
    wishlist_id  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id      UUID NOT NULL,
    product_id   UUID NOT NULL,
    variant_id   UUID,            -- null = wish any variant
    note         VARCHAR(500),
    created_at   TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (user_id, product_id)
);

-- Create indexes for user_wishlist
CREATE INDEX IF NOT EXISTS idx_user_wishlist_user ON springfood_product.user_wishlist(user_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_user_wishlist_product ON springfood_product.user_wishlist(product_id);

-- 4. Order Status History Table (springfood_order schema)
CREATE TABLE IF NOT EXISTS springfood_order.order_status_history (
    history_id   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id     UUID NOT NULL,
    from_status  VARCHAR(100),
    to_status    VARCHAR(100) NOT NULL,
    changed_by   VARCHAR(50),     -- user_id or "system"
    changed_role VARCHAR(50),     -- CUSTOMER, SHOP, SHIPPER, SYSTEM
    note         TEXT,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for order_status_history
CREATE INDEX IF NOT EXISTS idx_order_status_history_order_created ON springfood_order.order_status_history(order_id, created_at);
CREATE INDEX IF NOT EXISTS idx_order_status_history_status ON springfood_order.order_status_history(to_status);
CREATE INDEX IF NOT EXISTS idx_order_status_history_role ON springfood_order.order_status_history(changed_role);

-- 5. Shop Follow Table (springfood_shop schema)
CREATE TABLE IF NOT EXISTS springfood_shop.shop_follow (
    follow_id    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id      UUID NOT NULL,
    shop_id      UUID NOT NULL,
    followed_at  TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (user_id, shop_id)
);

-- Create indexes for shop_follow
CREATE INDEX IF NOT EXISTS idx_shop_follow_user ON springfood_shop.shop_follow(user_id, followed_at DESC);
CREATE INDEX IF NOT EXISTS idx_shop_follow_shop ON springfood_shop.shop_follow(shop_id, followed_at DESC);

-- 6. User Device Table (springfood_authentication schema)
CREATE TABLE IF NOT EXISTS springfood_authentication.user_device (
    device_id    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id      UUID NOT NULL,
    push_token   VARCHAR(500),     -- FCM/APNs token
    platform     VARCHAR(20),      -- IOS, ANDROID, WEB
    device_name  VARCHAR(255),
    is_active    BOOLEAN DEFAULT true,
    last_used_at TIMESTAMPTZ,
    created_at   TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for user_device
CREATE INDEX IF NOT EXISTS idx_user_device_user ON springfood_authentication.user_device(user_id);
CREATE INDEX IF NOT EXISTS idx_user_device_push_token ON springfood_authentication.user_device(push_token);
CREATE INDEX IF NOT EXISTS idx_user_device_active ON springfood_authentication.user_device(is_active, last_used_at);
CREATE INDEX IF NOT EXISTS idx_user_device_platform ON springfood_authentication.user_device(platform);

-- =====================================================
-- Sample Data Inserts (Optional)
-- =====================================================

-- Insert sample category groups
INSERT INTO springfood_product.category_group (group_code, group_name, description, display_order) VALUES
('FOOD', 'Thực phẩm', 'Nhóm các loại thực phẩm', 1),
('DRINK', 'Đồ uống', 'Nhóm các loại đồ uống', 2),
('SNACK', 'Đồ ăn vặt', 'Nhóm các loại đồ ăn vặt', 3),
('DESSERT', 'Tráng miệng', 'Nhóm các loại tráng miệng', 4),
('COMBO', 'Combo', 'Nhóm các combo ưu đãi', 5)
ON CONFLICT (group_code) DO NOTHING;

-- =====================================================
-- Comments for Documentation
-- =====================================================

COMMENT ON TABLE springfood_product.category_group IS 'Category groups for organizing product categories';
COMMENT ON COLUMN springfood_product.category_group.group_code IS 'Unique code matching category_group_code in categories table';

COMMENT ON TABLE springfood_product.product_view_history IS 'Track user product views for "Recently Viewed" feature and analytics';
COMMENT ON COLUMN springfood_product.product_view_history.source IS 'Source of view: SEARCH, CATEGORY, RECOMMENDATION, DIRECT';

COMMENT ON TABLE springfood_product.user_wishlist IS 'User favorite products wishlist';
COMMENT ON COLUMN springfood_product.user_wishlist.variant_id IS 'Specific variant wished, null means any variant';

COMMENT ON TABLE springfood_order.order_status_history IS 'Complete timeline of order status changes';
COMMENT ON COLUMN springfood_order.order_status_history.changed_role IS 'Role of who changed status: CUSTOMER, SHOP, SHIPPER, SYSTEM';

COMMENT ON TABLE springfood_shop.shop_follow IS 'User-shop follow relationships';

COMMENT ON TABLE springfood_authentication.user_device IS 'User devices for push notifications';
COMMENT ON COLUMN springfood_authentication.user_device.push_token IS 'FCM/APNs push notification token';
COMMENT ON COLUMN springfood_authentication.user_device.platform IS 'Device platform: IOS, ANDROID, WEB';

-- =====================================================
-- Update Triggers for updated_at columns
-- =====================================================

-- Function to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Create triggers for tables with updated_at columns
CREATE TRIGGER update_category_group_updated_at 
    BEFORE UPDATE ON springfood_product.category_group 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_user_device_updated_at 
    BEFORE UPDATE ON springfood_authentication.user_device 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- =====================================================
-- End of Script
-- =====================================================