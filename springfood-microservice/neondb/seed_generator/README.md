# SpringFood Seed Data Generator

TypeScript-based seed data generation system for SpringFood PostgreSQL database with 8 schemas.

## Overview

This system generates realistic, referentially-intact seed data for the SpringFood e-commerce platform. It creates:
- 150+ Vietnamese food products
- 50+ food categories with hierarchy
- 25+ food shops
- 15-20 sales/promotion campaigns
- 10+ orders with order items
- Predefined user accounts with roles
- Supporting data for chat, media, notification, and payment schemas

## Project Structure

```
seed_generator/
├── src/
│   ├── parsers/         # DDL file parsers
│   ├── analyzers/       # Dependency analyzers
│   ├── generators/      # Data generators
│   ├── writers/         # SQL file writers
│   ├── templates/       # Vietnamese food data templates
│   ├── utils/           # Utility functions (BCrypt, UUID, etc.)
│   └── index.ts         # Main entry point
├── tests/               # Unit and integration tests
├── package.json         # Node.js dependencies
├── tsconfig.json        # TypeScript configuration
└── README.md            # This file
```

## Installation

```bash
cd neondb/seed_generator
npm install
```

## Usage

### Generate Seed Data

```bash
npm run generate
```

This will:
1. Parse all DDL files from `neondb/` folder
2. Analyze table dependencies
3. Generate realistic Vietnamese food data
4. Create 8 SQL files (one per schema) in `neondb/seed_data/`
5. Create master execution script `run_all_seeds.sql`

### Build TypeScript

```bash
npm run build
```

### Run Tests

```bash
npm test
```

## Features

### Referential Integrity
- All foreign keys reference existing primary keys
- Proper dependency ordering (parent tables before child tables)
- Cross-schema consistency (user_id, shop_id, product_id)

### Realistic Vietnamese Food Data
- Product names: Phở, Bún, Cơm, Trà Sữa, Cà Phê, etc.
- Shop names: Phở Hà Nội 24h, Trà Sữa Gong Cha, Cà Phê Cộng, etc.
- Realistic prices: 10,000 - 500,000 VND
- Vietnamese descriptions and addresses

### Sales/Promotion Campaigns
- Flash Sales (40% discount, 2-3 hours)
- Seasonal Sales (20-25% discount, 7-30 days)
- Category-Specific Sales (Tuần Lễ Trà Sữa, Ngày Phở Việt Nam)
- Time-Based Sales (Ưu Đãi Bữa Sáng, Happy Hour)
- New Customer, Combo, Weekend, Holiday sales

### Predefined User Accounts
- 1 ADMIN user
- 2 SHOP_OWNER users
- 3 STAFF users
- 4 CUSTOMER users
- Password: `Password123!` (BCrypt hashed)

## Output Files

The generator creates the following SQL files in `neondb/seed_data/`:

1. `01_springfood_authentication_seed_data.sql` - Users, roles, addresses
2. `02_springfood_shop_seed_data.sql` - Shops, shop members, wallets
3. `03_springfood_product_seed_data.sql` - Categories, products, sales, product-sales
4. `04_springfood_order_seed_data.sql` - Orders, order items
5. `05_springfood_payment_seed_data.sql` - Payment transactions
6. `06_springfood_media_seed_data.sql` - Media files
7. `07_springfood_notification_seed_data.sql` - Notifications
8. `08_springfood_chat_seed_data.sql` - Conversations, messages

Plus:
- `run_all_seeds.sql` - Master execution script
- `run_seeds.bat` - Windows batch script
- `run_seeds.sh` - Linux/Mac shell script
- `README.md` - Documentation with user credentials

## Technology Stack

- **Language**: TypeScript
- **Runtime**: Node.js
- **Parser**: node-sql-parser
- **Password Hashing**: bcryptjs
- **UUID Generation**: uuid
- **Testing**: Jest

## Requirements Mapping

This generator implements all 14 requirements from the specification:
- Requirement 1: Referential Integrity
- Requirement 2: Realistic Food E-commerce Data
- Requirement 3: Minimum Record Counts
- Requirement 4: Data Quality and Completeness
- Requirement 5: Image Handling
- Requirement 6: SQL File Organization
- Requirement 7: Execution Script
- Requirement 8: NeonDB Compatibility
- Requirement 9: Cross-Schema Data Consistency
- Requirement 10: Predefined User Accounts
- Requirement 11: Category Hierarchy
- Requirement 12: Shop-Product Relationship
- Requirement 13: Order-Product Relationship
- Requirement 14: Documentation and README

## Development Status

- [x] Task 1: Project structure and dependencies setup
- [ ] Task 2: DDL parser and dependency analyzer
- [ ] Task 3: ID registry and core utilities
- [ ] Task 4: Vietnamese food data templates
- [ ] Task 5: Checkpoint - Verify templates and utilities
- [ ] Task 6: Data generators for Level 1-3 tables
- [ ] Task 7: Data generators for Level 4-5 tables
- [ ] Task 8: Data generators for Level 6 tables
- [ ] Task 9: Data generators for Level 7 tables
- [ ] Task 10: Checkpoint - Verify data generation
- [ ] Task 11: SQL writer module
- [ ] Task 12: Execution scripts and documentation
- [ ] Task 13: Integration tests
- [ ] Task 14: Final checkpoint and validation

## License

MIT
