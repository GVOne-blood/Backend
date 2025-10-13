-- Insert sample categories
INSERT INTO categories (category_name, slug, description, is_active) VALUES
('electronics', 'electronics', 'Electronic devices and gadgets', true),
('clothing', 'clothing', 'Clothing and fashion items', true),
('books', 'books', 'Books and publications', true);

-- Insert sample products
INSERT INTO products (product_id, shop_id, name, sku, description, msg, exp, product_status, price, wholesale_price, avg_rate, quantity, images, created_at, updated_at) VALUES
('550e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440000', 'iPhone 15', 'IPH15-128', 'Latest iPhone model with advanced features', '2024-01-01', '2025-01-01', 'AVAILABLE', 999.99, 950.00, 4.5, 50, '["https://example.com/iphone1.jpg", "https://example.com/iphone2.jpg"]', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('550e8400-e29b-41d4-a716-446655440002', '550e8400-e29b-41d4-a716-446655440000', 'Samsung Galaxy S24', 'SGS24-256', 'Android flagship smartphone', '2024-02-01', '2025-02-01', 'AVAILABLE', 899.99, 850.00, 4.3, 30, '["https://example.com/galaxy1.jpg"]', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('550e8400-e29b-41d4-a716-446655440003', '550e8400-e29b-41d4-a716-446655440000', 'MacBook Pro 16"', 'MBP16-M3', 'Professional laptop for developers', '2024-03-01', '2026-03-01', 'AVAILABLE', 2499.99, 2400.00, 4.8, 20, '["https://example.com/macbook1.jpg", "https://example.com/macbook2.jpg", "https://example.com/macbook3.jpg"]', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Insert product-category relationships
INSERT INTO product_categories (product_id, category_name) VALUES
('550e8400-e29b-41d4-a716-446655440001', 'electronics'),
('550e8400-e29b-41d4-a716-446655440002', 'electronics'),
('550e8400-e29b-41d4-a716-446655440003', 'electronics');