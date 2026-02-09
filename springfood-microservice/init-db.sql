-- =====================================================
-- PostgreSQL Initialization Script for SpringFood
-- =====================================================
-- Using SCHEMAS instead of separate databases for better 
-- resource management and cross-service queries when needed
-- =====================================================

-- Create main database (if not exists - handled by Docker)
-- CREATE DATABASE springfood;

-- Create schemas for each microservice
CREATE SCHEMA IF NOT EXISTS identity;
CREATE SCHEMA IF NOT EXISTS product;
CREATE SCHEMA IF NOT EXISTS cart;
CREATE SCHEMA IF NOT EXISTS orders;
CREATE SCHEMA IF NOT EXISTS payment;
CREATE SCHEMA IF NOT EXISTS shop;
CREATE SCHEMA IF NOT EXISTS notification;
CREATE SCHEMA IF NOT EXISTS chat;
CREATE SCHEMA IF NOT EXISTS media;
CREATE SCHEMA IF NOT EXISTS action_log;
CREATE SCHEMA IF NOT EXISTS statistical;

-- Grant privileges on schemas to postgres user
GRANT ALL PRIVILEGES ON SCHEMA identity TO postgres;
GRANT ALL PRIVILEGES ON SCHEMA product TO postgres;
GRANT ALL PRIVILEGES ON SCHEMA cart TO postgres;
GRANT ALL PRIVILEGES ON SCHEMA orders TO postgres;
GRANT ALL PRIVILEGES ON SCHEMA payment TO postgres;
GRANT ALL PRIVILEGES ON SCHEMA shop TO postgres;
GRANT ALL PRIVILEGES ON SCHEMA notification TO postgres;
GRANT ALL PRIVILEGES ON SCHEMA chat TO postgres;
GRANT ALL PRIVILEGES ON SCHEMA media TO postgres;
GRANT ALL PRIVILEGES ON SCHEMA action_log TO postgres;
GRANT ALL PRIVILEGES ON SCHEMA statistical TO postgres;

-- Set default search path (optional)
-- ALTER DATABASE springfood SET search_path TO public, identity, product, cart, orders, payment, shop, notification, chat, media;

-- Note: Each service should connect with ?currentSchema=<schema_name>
-- Example: jdbc:postgresql://localhost:5432/springfood?currentSchema=chat
