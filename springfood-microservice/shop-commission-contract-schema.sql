-- =====================================================
-- SQL Script for Shop Commission & Contract Tables
-- SpringFood Microservices Database Schema
-- =====================================================

-- 1. Platform Commission Config Table (springfood_shop schema)
CREATE TABLE IF NOT EXISTS springfood_shop.platform_commission_config (
    config_id        UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name             VARCHAR(255) NOT NULL,
    description      TEXT,
    commission_type  VARCHAR(20)  NOT NULL,  -- PERCENTAGE, FLAT, HYBRID
    percent_rate     NUMERIC(5,2),           -- e.g., 5.00 (%)
    flat_amount      NUMERIC(15,2),          -- e.g., 3000 (VND/order)
    min_commission   NUMERIC(15,2),          -- minimum commission per order
    max_commission   NUMERIC(15,2),          -- commission cap (if any)
    is_active        BOOLEAN      DEFAULT false,
    effective_from   TIMESTAMPTZ  NOT NULL,
    effective_to     TIMESTAMPTZ,
    created_by       VARCHAR(50),            -- admin user_id
    created_at       TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_by       VARCHAR(50),
    updated_at       TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for platform_commission_config
CREATE INDEX IF NOT EXISTS idx_platform_commission_config_active ON springfood_shop.platform_commission_config(is_active, effective_from);
CREATE INDEX IF NOT EXISTS idx_platform_commission_config_type ON springfood_shop.platform_commission_config(commission_type);
CREATE INDEX IF NOT EXISTS idx_platform_commission_config_dates ON springfood_shop.platform_commission_config(effective_from, effective_to);

-- 2. Commission Rule Table (springfood_shop schema)
CREATE TABLE IF NOT EXISTS springfood_shop.commission_rule (
    rule_id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name             VARCHAR(255) NOT NULL,
    description      TEXT,
    
    -- Scope fields (null = applies to all)
    shop_id          UUID,                   -- references springfood_shop.shops.shop_id
    category_name    VARCHAR(255),           -- references springfood_product.categories.category_name
    product_id       UUID,                   -- references springfood_product.products.product_id
    
    commission_type  VARCHAR(20)  NOT NULL,  -- PERCENTAGE, FLAT, HYBRID
    percent_rate     NUMERIC(5,2),
    flat_amount      NUMERIC(15,2),
    min_commission   NUMERIC(15,2),
    
    priority         INTEGER      NOT NULL DEFAULT 100,  -- Lower number = higher priority
    is_active        BOOLEAN      DEFAULT true,
    effective_from   TIMESTAMPTZ  NOT NULL,
    effective_to     TIMESTAMPTZ,
    created_by       VARCHAR(50),
    created_at       TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_by       VARCHAR(50),
    updated_at       TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for commission_rule
CREATE INDEX IF NOT EXISTS idx_commission_rule_shop ON springfood_shop.commission_rule(shop_id, is_active);
CREATE INDEX IF NOT EXISTS idx_commission_rule_category ON springfood_shop.commission_rule(category_name, is_active);
CREATE INDEX IF NOT EXISTS idx_commission_rule_product ON springfood_shop.commission_rule(product_id, is_active);
CREATE INDEX IF NOT EXISTS idx_commission_rule_priority ON springfood_shop.commission_rule(priority, is_active);
CREATE INDEX IF NOT EXISTS idx_commission_rule_dates ON springfood_shop.commission_rule(effective_from, effective_to);

-- 3. Shop Contract Table (springfood_shop schema)
CREATE TABLE IF NOT EXISTS springfood_shop.shop_contract (
    contract_id      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    shop_id          UUID         NOT NULL,              -- references springfood_shop.shops.shop_id
    contract_code    VARCHAR(100) NOT NULL,
    title            VARCHAR(255) NOT NULL,
    contract_type    VARCHAR(50),                        -- STANDARD, MALL, VIP, PARTNER
    document_id      VARCHAR(255),                       -- references springfood_media.media_file.id
    
    -- Commission terms specific to this contract (overrides commission_rule)
    commission_type  VARCHAR(20),                        -- PERCENTAGE, FLAT, HYBRID
    percent_rate     NUMERIC(5,2),
    flat_amount      NUMERIC(15,2),
    
    start_date       DATE         NOT NULL,
    end_date         DATE,
    status           VARCHAR(50)  NOT NULL,              -- DRAFT, ACTIVE, EXPIRED, TERMINATED
    
    signed_at        TIMESTAMPTZ,
    signed_by_shop   VARCHAR(100),
    signed_by_admin  VARCHAR(50),                        -- references springfood_authentication.user.user_id
    
    created_at       TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    created_by       VARCHAR(50),
    updated_at       TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_by       VARCHAR(50)
);

-- Create indexes for shop_contract
CREATE INDEX IF NOT EXISTS idx_shop_contract_shop ON springfood_shop.shop_contract(shop_id, status);
CREATE INDEX IF NOT EXISTS idx_shop_contract_code ON springfood_shop.shop_contract(contract_code);
CREATE INDEX IF NOT EXISTS idx_shop_contract_dates ON springfood_shop.shop_contract(start_date, end_date);
CREATE INDEX IF NOT EXISTS idx_shop_contract_status ON springfood_shop.shop_contract(status);
CREATE INDEX IF NOT EXISTS idx_shop_contract_type ON springfood_shop.shop_contract(contract_type);

-- 4. Platform Fee Config Table (springfood_shop schema)
CREATE TABLE IF NOT EXISTS springfood_shop.platform_fee_config (
    fee_id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    fee_code         VARCHAR(100) NOT NULL UNIQUE,       -- PAYMENT_GATEWAY_FEE, WITHDRAWAL_FEE
    name             VARCHAR(255) NOT NULL,
    description      TEXT,
    fee_type         VARCHAR(20)  NOT NULL,              -- PERCENTAGE, FLAT
    percent_rate     NUMERIC(5,2),
    flat_amount      NUMERIC(15,2),
    apply_scope      VARCHAR(50),                        -- ORDER, WITHDRAWAL
    is_active        BOOLEAN      DEFAULT true,
    effective_from   TIMESTAMPTZ  NOT NULL,
    effective_to     TIMESTAMPTZ,
    created_at       TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    created_by       VARCHAR(50),
    updated_at       TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_by       VARCHAR(50)
);

-- Create indexes for platform_fee_config
CREATE INDEX IF NOT EXISTS idx_platform_fee_config_code ON springfood_shop.platform_fee_config(fee_code);
CREATE INDEX IF NOT EXISTS idx_platform_fee_config_active ON springfood_shop.platform_fee_config(is_active, effective_from);
CREATE INDEX IF NOT EXISTS idx_platform_fee_config_scope ON springfood_shop.platform_fee_config(apply_scope);
CREATE INDEX IF NOT EXISTS idx_platform_fee_config_type ON springfood_shop.platform_fee_config(fee_type);

-- =====================================================
-- Sample Data Inserts
-- =====================================================

-- Insert default platform commission config
INSERT INTO springfood_shop.platform_commission_config 
(name, description, commission_type, percent_rate, min_commission, is_active, effective_from, created_by) VALUES
('Default Platform Commission', 'Hoa hồng mặc định cho toàn sàn', 'PERCENTAGE', 5.00, 1000, true, CURRENT_TIMESTAMP, 'system')
ON CONFLICT DO NOTHING;

-- Insert sample commission rules
INSERT INTO springfood_shop.commission_rule 
(name, description, commission_type, percent_rate, priority, effective_from, created_by) VALUES
('VIP Shop Commission', 'Hoa hồng ưu đãi cho shop VIP', 'PERCENTAGE', 3.00, 10, CURRENT_TIMESTAMP, 'system'),
('Food Category Commission', 'Hoa hồng cho danh mục thực phẩm', 'PERCENTAGE', 4.00, 50, CURRENT_TIMESTAMP, 'system'),
('High Value Product Commission', 'Hoa hồng cho sản phẩm giá trị cao', 'HYBRID', 2.00, 30, CURRENT_TIMESTAMP, 'system')
ON CONFLICT DO NOTHING;

-- Insert sample platform fee configs
INSERT INTO springfood_shop.platform_fee_config 
(fee_code, name, description, fee_type, percent_rate, apply_scope, effective_from, created_by) VALUES
('PAYMENT_GATEWAY_FEE', 'Phí cổng thanh toán', 'Phí xử lý thanh toán qua cổng thanh toán', 'PERCENTAGE', 2.50, 'ORDER', CURRENT_TIMESTAMP, 'system'),
('WITHDRAWAL_FEE', 'Phí rút tiền', 'Phí rút tiền về tài khoản ngân hàng', 'FLAT', NULL, 'WITHDRAWAL', CURRENT_TIMESTAMP, 'system'),
('SERVICE_FEE', 'Phí dịch vụ', 'Phí dịch vụ nền tảng', 'PERCENTAGE', 1.00, 'ORDER', CURRENT_TIMESTAMP, 'system')
ON CONFLICT (fee_code) DO NOTHING;

-- Update withdrawal fee flat amount
UPDATE springfood_shop.platform_fee_config 
SET flat_amount = 5000 
WHERE fee_code = 'WITHDRAWAL_FEE';

-- =====================================================
-- Comments for Documentation
-- =====================================================

COMMENT ON TABLE springfood_shop.platform_commission_config IS 'Platform-wide default commission configuration for new shops';
COMMENT ON COLUMN springfood_shop.platform_commission_config.commission_type IS 'Commission calculation type: PERCENTAGE, FLAT, HYBRID';
COMMENT ON COLUMN springfood_shop.platform_commission_config.is_active IS 'Only one config should be active at a time';

COMMENT ON TABLE springfood_shop.commission_rule IS 'Commission rules that override platform default, can be scoped to shop/category/product';
COMMENT ON COLUMN springfood_shop.commission_rule.priority IS 'Lower number = higher priority, used for rule resolution';
COMMENT ON COLUMN springfood_shop.commission_rule.shop_id IS 'If set, rule applies only to this shop';
COMMENT ON COLUMN springfood_shop.commission_rule.category_name IS 'If set, rule applies only to this category';
COMMENT ON COLUMN springfood_shop.commission_rule.product_id IS 'If set, rule applies only to this product';

COMMENT ON TABLE springfood_shop.shop_contract IS 'Complete history of shop contracts, shops.contract_start_date/end_date caches active contract';
COMMENT ON COLUMN springfood_shop.shop_contract.status IS 'Contract status: DRAFT, ACTIVE, EXPIRED, TERMINATED';
COMMENT ON COLUMN springfood_shop.shop_contract.contract_type IS 'Contract tier: STANDARD, MALL, VIP, PARTNER';
COMMENT ON COLUMN springfood_shop.shop_contract.document_id IS 'Reference to signed contract document in media service';

COMMENT ON TABLE springfood_shop.platform_fee_config IS 'Configuration for various platform fees (payment gateway, withdrawal, etc.)';
COMMENT ON COLUMN springfood_shop.platform_fee_config.fee_code IS 'Unique identifier for fee type';
COMMENT ON COLUMN springfood_shop.platform_fee_config.apply_scope IS 'Where fee applies: ORDER, WITHDRAWAL';

-- =====================================================
-- Update Triggers for updated_at columns
-- =====================================================

-- Create triggers for tables with updated_at columns
CREATE TRIGGER update_platform_commission_config_updated_at 
    BEFORE UPDATE ON springfood_shop.platform_commission_config 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_commission_rule_updated_at 
    BEFORE UPDATE ON springfood_shop.commission_rule 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_shop_contract_updated_at 
    BEFORE UPDATE ON springfood_shop.shop_contract 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_platform_fee_config_updated_at 
    BEFORE UPDATE ON springfood_shop.platform_fee_config 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- =====================================================
-- Constraints and Business Rules
-- =====================================================

-- Ensure only one active platform commission config at a time
-- (This would be enforced at application level due to complexity)

-- Ensure commission rules have valid priority
ALTER TABLE springfood_shop.commission_rule 
ADD CONSTRAINT chk_commission_rule_priority 
CHECK (priority >= 1 AND priority <= 1000);

-- Ensure contract dates are logical
ALTER TABLE springfood_shop.shop_contract 
ADD CONSTRAINT chk_shop_contract_dates 
CHECK (end_date IS NULL OR end_date >= start_date);

-- Ensure fee configs have appropriate values based on type
-- (This would be enforced at application level due to complexity)

-- 5. Admin Config Audit Log Table (springfood_shop schema)
CREATE TABLE IF NOT EXISTS springfood_shop.admin_config_audit_log (
    log_id       UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    entity_type  VARCHAR(50)  NOT NULL,  -- PLATFORM_COMMISSION, COMMISSION_RULE, PLATFORM_FEE, SHOP_CONTRACT
    entity_id    UUID         NOT NULL,
    action       VARCHAR(20)  NOT NULL,  -- CREATE, UPDATE, DEACTIVATE, DELETE
    old_value    JSONB,
    new_value    JSONB,
    changed_by   VARCHAR(50)  NOT NULL,  -- references springfood_authentication.user.user_id
    changed_at   TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    note         TEXT
);

-- Create indexes for admin_config_audit_log
CREATE INDEX IF NOT EXISTS idx_admin_audit_entity ON springfood_shop.admin_config_audit_log(entity_type, entity_id);
CREATE INDEX IF NOT EXISTS idx_admin_audit_changed_by ON springfood_shop.admin_config_audit_log(changed_by, changed_at);
CREATE INDEX IF NOT EXISTS idx_admin_audit_changed_at ON springfood_shop.admin_config_audit_log(changed_at DESC);
CREATE INDEX IF NOT EXISTS idx_admin_audit_action ON springfood_shop.admin_config_audit_log(action, entity_type);

-- =====================================================
-- Additional Comments for AdminConfigAuditLog
-- =====================================================

COMMENT ON TABLE springfood_shop.admin_config_audit_log IS 'Audit log for tracking admin configuration changes';
COMMENT ON COLUMN springfood_shop.admin_config_audit_log.entity_type IS 'Type of entity being audited: PLATFORM_COMMISSION, COMMISSION_RULE, PLATFORM_FEE, SHOP_CONTRACT';
COMMENT ON COLUMN springfood_shop.admin_config_audit_log.action IS 'Action performed: CREATE, UPDATE, DEACTIVATE, DELETE';
COMMENT ON COLUMN springfood_shop.admin_config_audit_log.old_value IS 'Previous values in JSON format (null for CREATE)';
COMMENT ON COLUMN springfood_shop.admin_config_audit_log.new_value IS 'New values in JSON format (null for DELETE)';

-- =====================================================
-- Sample Audit Log Data
-- =====================================================

-- Insert sample audit log entries (these would normally be created by application)
INSERT INTO springfood_shop.admin_config_audit_log 
(entity_type, entity_id, action, new_value, changed_by, note) VALUES
('PLATFORM_COMMISSION', gen_random_uuid(), 'CREATE', 
 '{"name": "Default Platform Commission", "commission_type": "PERCENTAGE", "percent_rate": 5.00}', 
 'system', 'Initial platform commission configuration'),
('COMMISSION_RULE', gen_random_uuid(), 'CREATE', 
 '{"name": "VIP Shop Commission", "commission_type": "PERCENTAGE", "percent_rate": 3.00, "priority": 10}', 
 'system', 'VIP shop commission rule created'),
('PLATFORM_FEE', gen_random_uuid(), 'CREATE', 
 '{"fee_code": "PAYMENT_GATEWAY_FEE", "name": "Phí cổng thanh toán", "fee_type": "PERCENTAGE", "percent_rate": 2.50}', 
 'system', 'Payment gateway fee configuration created')
ON CONFLICT DO NOTHING;

-- =====================================================
-- End of Script
-- =====================================================