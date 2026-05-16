# SpringFood Database Seed Data

This directory contains seed data for the SpringFood e-commerce platform database. The seed data includes realistic Vietnamese food products, shops, categories, users, orders, and sales campaigns designed for testing and development purposes.

## Table of Contents

- [Overview](#overview)
- [Prerequisites](#prerequisites)
- [Quick Start](#quick-start)
- [Execution Order](#execution-order)
- [Predefined User Accounts](#predefined-user-accounts)
- [Record Counts](#record-counts)
- [Execution Methods](#execution-methods)
- [Troubleshooting](#troubleshooting)
- [Data Quality](#data-quality)

## Overview

The seed data system generates realistic Vietnamese food e-commerce data across 8 PostgreSQL schemas:

- **authentication**: Users, roles, and authentication data
- **shop**: Food shops and shop management
- **product**: Products, categories, and sales campaigns
- **order**: Customer orders and order items
- **payment**: Payment transactions
- **media**: Media files and images
- **notification**: User notifications
- **chat**: Conversations and messages

All data maintains **referential integrity** with proper foreign key relationships across schemas.

## Prerequisites

Before executing the seed data scripts, ensure you have:

1. **PostgreSQL Client (psql)**
   - Windows: Download from [PostgreSQL Downloads](https://www.postgresql.org/download/windows/)
   - macOS: `brew install postgresql`
   - Linux: `sudo apt-get install postgresql-client` (Ubuntu/Debian)

2. **NeonDB Connection Credentials**
   - Host (e.g., `ep-xxx-xxx.us-east-2.aws.neon.tech`)
   - Username (default: `neondb_owner`)
   - Password
   - Database name (default: `neondb`)

3. **Database Schemas**
   - All 8 schemas must exist before running seed data
   - Run DDL scripts first if schemas don't exist

4. **Permissions**
   - User must have INSERT permissions on all tables
   - User must have SELECT permissions for foreign key validation

## Quick Start

### Windows

```batch
cd neondb\seed_data
run_seeds.bat
```

### Linux/Mac

```bash
cd neondb/seed_data
chmod +x run_seeds.sh
./run_seeds.sh
```

### Manual Execution

```bash
psql -h <neondb-host> -U <username> -d <database> -f run_all_seeds.sql
```

## Execution Order

The seed data files are executed in dependency order to maintain referential integrity:

### Level 1-2: Authentication Schema
**File**: `01_springfood_authentication_seed_data.sql`

Tables populated:
- `role` - 5 fixed roles
- `user` - 10 predefined users
- `user_has_role` - User-role assignments
- `address` - User addresses (if any)

### Level 3: Shop Schema
**File**: `02_springfood_shop_seed_data.sql`

Tables populated:
- `shops` - 25+ Vietnamese food shops
- `shop_members` - Shop staff members (if any)
- `shop_wallets` - Shop wallet information (if any)

### Level 4-6: Product Schema
**File**: `03_springfood_product_seed_data.sql`

Tables populated:
- `categories` - 50+ food categories (hierarchical)
- `products` - 150+ Vietnamese food products
- `product_categories` - Product-category relationships
- `product_images` - Product images (if any)
- `feedbacks` - Product reviews (if any)
- `sales` - 15-20 sales campaigns
- `product_sales` - Product-sale relationships

### Level 6: Order Schema
**File**: `04_springfood_order_seed_data.sql`

Tables populated:
- `orders` - 10+ customer orders
- `order_items` - 20+ order line items

### Level 7: Payment Schema
**File**: `05_springfood_payment_seed_data.sql` (if exists)

Tables populated:
- `payment_transactions` - 10+ payment records

### Level 7: Media Schema
**File**: `06_springfood_media_seed_data.sql` (if exists)

Tables populated:
- `media_file` - 10+ media files

### Level 7: Notification Schema
**File**: `07_springfood_notification_seed_data.sql` (if exists)

Tables populated:
- `notifications` - 10+ user notifications

### Level 7: Chat Schema
**File**: `08_springfood_chat_seed_data.sql` (if exists)

Tables populated:
- `conversation` - 10+ conversations
- `conversation_participant` - 20+ participants
- `message` - 50+ messages

## Predefined User Accounts

The seed data includes 10 predefined user accounts for testing different roles:

### Admin Account (1 user)
- **Email**: `admin1@springfood.vn`
- **Password**: `Password123!`
- **Role**: ADMIN
- **Name**: Nguyễn Văn Admin

### Shop Owner Accounts (2 users)
- **Email**: `shopowner1@springfood.vn`, `shopowner2@springfood.vn`
- **Password**: `Password123!`
- **Role**: SHOP_OWNER
- **Names**: Trần Thị Owner, Lê Văn Owner

### Staff Accounts (3 users)
- **Email**: `staff1@springfood.vn`, `staff2@springfood.vn`, `staff3@springfood.vn`
- **Password**: `Password123!`
- **Role**: STAFF
- **Names**: Phạm Văn Staff, Hoàng Thị Staff, Vũ Văn Staff

### Customer Accounts (4 users)
- **Email**: `customer1@springfood.vn`, `customer2@springfood.vn`, `customer3@springfood.vn`, `customer4@springfood.vn`
- **Password**: `Password123!`
- **Role**: CUSTOMER
- **Names**: Nguyễn Thị Customer, Trần Văn Customer, Lê Thị Customer, Phạm Văn Customer

**Note**: All passwords are BCrypt hashed in the database. The plain text password `Password123!` is provided here for testing purposes only.

## Record Counts

Expected minimum record counts after successful execution:

| Schema | Table | Minimum Records |
|--------|-------|-----------------|
| authentication | role | 5 |
| authentication | user | 10 |
| authentication | user_has_role | 10 |
| shop | shops | 25 |
| product | categories | 50 |
| product | products | 150 |
| product | product_categories | 150 |
| product | sales | 15 |
| product | product_sales | 100 |
| order | orders | 10 |
| order | order_items | 20 |
| payment | payment_transactions | 10 |
| media | media_file | 10 |
| notification | notifications | 10 |
| chat | conversation | 10 |
| chat | conversation_participant | 20 |
| chat | message | 50 |

### Verify Record Counts

```sql
-- Authentication schema
SELECT COUNT(*) FROM springfood_authentication.role;
SELECT COUNT(*) FROM springfood_authentication.user;
SELECT COUNT(*) FROM springfood_authentication.user_has_role;

-- Shop schema
SELECT COUNT(*) FROM springfood_shop.shops;

-- Product schema
SELECT COUNT(*) FROM springfood_product.categories;
SELECT COUNT(*) FROM springfood_product.products;
SELECT COUNT(*) FROM springfood_product.sales;
SELECT COUNT(*) FROM springfood_product.product_sales;

-- Order schema
SELECT COUNT(*) FROM springfood_order.orders;
SELECT COUNT(*) FROM springfood_order.order_items;
```

## Execution Methods

### Method 1: Wrapper Scripts (Recommended)

The wrapper scripts provide interactive prompts and error handling:

**Windows**:
```batch
run_seeds.bat
```

**Linux/Mac**:
```bash
chmod +x run_seeds.sh
./run_seeds.sh
```

### Method 2: Environment Variables

Set connection parameters as environment variables to skip prompts:

**Windows**:
```batch
set NEON_HOST=ep-xxx-xxx.us-east-2.aws.neon.tech
set NEON_USER=neondb_owner
set NEON_DB=neondb
set NEON_PASSWORD=your_password
run_seeds.bat
```

**Linux/Mac**:
```bash
export NEON_HOST=ep-xxx-xxx.us-east-2.aws.neon.tech
export NEON_USER=neondb_owner
export NEON_DB=neondb
export PGPASSWORD=your_password
./run_seeds.sh
```

### Method 3: Direct psql Execution

Execute the master SQL script directly:

```bash
psql -h ep-xxx-xxx.us-east-2.aws.neon.tech \
     -U neondb_owner \
     -d neondb \
     -p 5432 \
     -f run_all_seeds.sql
```

### Method 4: Individual File Execution

Execute individual SQL files for specific schemas:

```bash
# Authentication only
psql -h <host> -U <user> -d <db> -f 01_springfood_authentication_seed_data.sql

# Shop only
psql -h <host> -U <user> -d <db> -f 02_springfood_shop_seed_data.sql

# Product only
psql -h <host> -U <user> -d <db> -f 03_springfood_product_seed_data.sql
```

**Warning**: When executing individual files, ensure dependency order is maintained.

## Troubleshooting

### Connection Issues

**Problem**: `psql: error: connection to server at "..." failed`

**Solutions**:
1. Verify NeonDB host is correct
2. Check network connectivity
3. Ensure NeonDB instance is running
4. Verify firewall allows PostgreSQL port (5432)

### Authentication Issues

**Problem**: `psql: error: FATAL: password authentication failed`

**Solutions**:
1. Verify username is correct (default: `neondb_owner`)
2. Check password is correct
3. Ensure user has database access permissions
4. Try resetting password in NeonDB console

### Foreign Key Violations

**Problem**: `ERROR: insert or update on table "..." violates foreign key constraint`

**Solutions**:
1. Ensure all schemas exist before running seed data
2. Verify tables are empty (or use ON CONFLICT clauses)
3. Execute files in correct dependency order
4. Check if DDL scripts were run successfully

**Clean database before re-running**:
```sql
-- Delete all data in reverse dependency order
TRUNCATE springfood_chat.message CASCADE;
TRUNCATE springfood_chat.conversation_participant CASCADE;
TRUNCATE springfood_chat.conversation CASCADE;
TRUNCATE springfood_notification.notifications CASCADE;
TRUNCATE springfood_media.media_file CASCADE;
TRUNCATE springfood_payment.payment_transactions CASCADE;
TRUNCATE springfood_order.order_items CASCADE;
TRUNCATE springfood_order.orders CASCADE;
TRUNCATE springfood_product.product_sales CASCADE;
TRUNCATE springfood_product.sales CASCADE;
TRUNCATE springfood_product.feedbacks CASCADE;
TRUNCATE springfood_product.product_images CASCADE;
TRUNCATE springfood_product.product_categories CASCADE;
TRUNCATE springfood_product.products CASCADE;
TRUNCATE springfood_product.categories CASCADE;
TRUNCATE springfood_shop.shop_wallets CASCADE;
TRUNCATE springfood_shop.shop_members CASCADE;
TRUNCATE springfood_shop.shops CASCADE;
TRUNCATE springfood_authentication.address CASCADE;
TRUNCATE springfood_authentication.user_has_role CASCADE;
TRUNCATE springfood_authentication.user CASCADE;
TRUNCATE springfood_authentication.role CASCADE;
```

### Permission Issues

**Problem**: `ERROR: permission denied for table "..."`

**Solutions**:
1. Verify user has INSERT permissions on all tables
2. Grant necessary permissions:
   ```sql
   GRANT INSERT, SELECT ON ALL TABLES IN SCHEMA springfood_authentication TO neondb_owner;
   GRANT INSERT, SELECT ON ALL TABLES IN SCHEMA springfood_shop TO neondb_owner;
   GRANT INSERT, SELECT ON ALL TABLES IN SCHEMA springfood_product TO neondb_owner;
   GRANT INSERT, SELECT ON ALL TABLES IN SCHEMA springfood_order TO neondb_owner;
   GRANT INSERT, SELECT ON ALL TABLES IN SCHEMA springfood_payment TO neondb_owner;
   GRANT INSERT, SELECT ON ALL TABLES IN SCHEMA springfood_media TO neondb_owner;
   GRANT INSERT, SELECT ON ALL TABLES IN SCHEMA springfood_notification TO neondb_owner;
   GRANT INSERT, SELECT ON ALL TABLES IN SCHEMA springfood_chat TO neondb_owner;
   ```

### Schema Not Found

**Problem**: `ERROR: schema "springfood_authentication" does not exist`

**Solutions**:
1. Run DDL scripts to create schemas first
2. Verify schema names match exactly (case-sensitive)
3. Check if you're connected to the correct database

### Duplicate Key Violations

**Problem**: `ERROR: duplicate key value violates unique constraint`

**Solutions**:
1. Database already contains seed data - clean it first
2. ON CONFLICT clauses should handle this - check SQL files
3. Verify primary key sequences are correct

### Timeout Issues

**Problem**: Script execution times out or hangs

**Solutions**:
1. Increase psql timeout: `psql --set=statement_timeout=300000`
2. Execute files individually instead of master script
3. Check NeonDB instance performance/limits
4. Reduce batch sizes in SQL files if needed

## Data Quality

### Vietnamese Food Products

The seed data includes realistic Vietnamese food items:

- **Phở**: Phở Bò Tái, Phở Gà, Phở Đặc Biệt, etc.
- **Bún**: Bún Chả, Bún Bò Huế, Bún Riêu, etc.
- **Cơm**: Cơm Tấm Sườn, Cơm Gà Xối Mỡ, Cơm Chiên, etc.
- **Đồ Uống**: Cà Phê, Trà Sữa, Sinh Tố, Nước Ép, etc.
- **Tráng Miệng**: Chè, Kem, Bánh Ngọt, etc.

### Price Ranges (VND)

Realistic Vietnamese food delivery prices:

- Cà Phê: 15,000 - 45,000 VND
- Trà Sữa: 25,000 - 65,000 VND
- Phở: 35,000 - 75,000 VND
- Cơm: 35,000 - 85,000 VND
- Bún: 30,000 - 70,000 VND

### Shop Names

Realistic Vietnamese food businesses:

- Phở Hà Nội 24h
- Bún Chả Hà Nội
- Cơm Tấm Sài Gòn
- The Coffee House
- Highlands Coffee
- Gong Cha
- Phúc Long Coffee & Tea

### Sales Campaigns

15-20 realistic promotion campaigns:

- Flash Sale Cuối Tuần (40% discount, 3 hours)
- Khuyến Mãi Mùa Hè (20% discount, 30 days)
- Tuần Lễ Trà Sữa (15% discount, 7 days)
- Ưu Đãi Bữa Sáng (25% discount, 6h-9h)
- Happy Hour (30% discount, 14h-16h)

### Category Hierarchy

50+ categories organized in 2-level hierarchy:

**Root Categories** (15):
- Món Việt Truyền Thống
- Đồ Uống
- Tráng Miệng
- Món Chay
- Món Hải Sản
- Món Nướng & BBQ
- Món Lẩu
- Món Ăn Sáng
- Món Ăn Vặt
- Đồ Ăn Nhanh

**Child Categories** (40+):
- Phở, Bún, Cơm, Nem & Chả
- Cà Phê, Trà Sữa, Trà Trái Cây, Nước Ép, Sinh Tố
- Chè, Kem, Bánh Ngọt
- And many more...

## Support

For issues or questions:

1. Check the [Troubleshooting](#troubleshooting) section
2. Verify all prerequisites are met
3. Review error messages carefully
4. Check NeonDB console for database status
5. Contact the development team

## License

This seed data is for development and testing purposes only. Do not use in production environments.

---

**Last Updated**: 2025-01-XX  
**Version**: 1.0.0  
**Maintainer**: SpringFood Development Team
