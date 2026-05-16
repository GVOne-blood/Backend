/**
 * SQL File Generator
 * 
 * Generates 8 SQL files (one per schema) with proper dependency ordering.
 * 
 * Requirements: 6.1, 6.2, 6.3, 6.4, 6.5
 */

import * as fs from 'fs';
import * as path from 'path';
import { IDRegistry } from './utils/id-registry';
import { SQLWriter, DEFAULT_WRITE_OPTIONS, WriteOptions } from './writers/sql-writer';

// Import generators
import { generateRoles } from './generators/role-generator';
import { generateUsers, assignShopsToOwners } from './generators/user-generator';
import { generateUserHasRole } from './generators/user-has-role-generator';
import { generateShops } from './generators/shop-generator';
import { generateShopMembers } from './generators/shop-member-generator';
import { generateShopWallets } from './generators/shop-wallet-generator';
import { generateCategories } from './generators/category-generator';
import { generateProducts } from './generators/product-generator';
import { generateProductCategories } from './generators/product-categories-generator';
import { generateSales } from './generators/sales-generator';
import { generateProductSales } from './generators/product-sales-generator';
import { generateOrders } from './generators/order-generator';
import { generateOrderItems } from './generators/order-items-generator';
import { updateAllOrderTotals } from './generators/order-totals-updater';

/**
 * Schema file configuration
 */
interface SchemaFileConfig {
  filename: string;
  schema: string;
  tables: Array<{
    table: string;
    records: any[];
  }>;
}

/**
 * Generate all seed data and SQL files
 */
export async function generateAllSQLFiles(): Promise<void> {
  console.log('Starting SQL file generation...');
  console.log('');

  // Initialize ID registry
  const registry = new IDRegistry();
  const writer = new SQLWriter();

  // Output directory
  const outputDir = path.join(__dirname, '../../seed_data');
  
  // Create output directory if it doesn't exist
  if (!fs.existsSync(outputDir)) {
    fs.mkdirSync(outputDir, { recursive: true });
  }

  console.log('Step 1: Generating Level 1-2 data (roles, users)...');
  
  // Level 1: Roles
  const roles = generateRoles(registry);
  console.log(`  ✓ Generated ${roles.length} roles`);

  // Level 2: Users
  const users = generateUsers(registry);
  console.log(`  ✓ Generated ${users.length} users`);

  // Level 3: User-Role junction
  const userHasRole = generateUserHasRole(users, registry);
  console.log(`  ✓ Generated ${userHasRole.length} user-role assignments`);

  console.log('');
  console.log('Step 2: Generating Level 3 data (shops)...');
  
  // Level 3: Shops
  const shops = generateShops(registry);
  console.log(`  ✓ Generated ${shops.length} shops`);

  // Update SHOP_OWNER users with shop_id
  assignShopsToOwners(users, registry);
  console.log(`  ✓ Assigned shops to shop owners`);

  // Generate shop members (creates new employee users)
  const { shopMembers, employeeUsers } = generateShopMembers(registry);
  console.log(`  ✓ Generated ${shopMembers.length} shop members`);
  console.log(`  ✓ Created ${employeeUsers.length} employee user accounts`);

  // Generate shop wallets (one per shop)
  const shopWallets = generateShopWallets(registry);
  console.log(`  ✓ Generated ${shopWallets.length} shop wallets`);

  console.log('');
  console.log('Step 3: Generating Level 4-5 data (categories, products, sales)...');
  
  // Level 4: Categories
  const categories = generateCategories(registry);
  console.log(`  ✓ Generated ${categories.length} categories`);

  // Level 4: Products
  const products = generateProducts(registry);
  console.log(`  ✓ Generated ${products.length} products`);

  // Level 5: Product-Category junction
  const productCategories = generateProductCategories(products, registry);
  console.log(`  ✓ Generated ${productCategories.length} product-category assignments`);

  // Level 5: Sales
  const sales = generateSales(registry);
  console.log(`  ✓ Generated ${sales.length} sales campaigns`);

  console.log('');
  console.log('Step 4: Generating Level 6 data (product-sales, orders, order items)...');
  
  // Level 6: Product-Sales junction
  const productSales = generateProductSales(registry);
  console.log(`  ✓ Generated ${productSales.length} product-sale assignments`);

  // Level 6: Orders
  const orders = generateOrders(registry);
  console.log(`  ✓ Generated ${orders.length} orders`);

  // Level 6: Order Items
  const orderItems = generateOrderItems(registry);
  console.log(`  ✓ Generated ${orderItems.length} order items`);

  // Update order totals
  updateAllOrderTotals(orders, orderItems);
  console.log(`  ✓ Updated order totals`);

  console.log('');
  console.log('Step 5: Writing SQL files...');

  // Write options
  const writeOptions: WriteOptions = {
    ...DEFAULT_WRITE_OPTIONS,
    includeTransaction: true,
    includeComments: true,
    onConflict: 'DO NOTHING'
  };

  // Schema file configurations
  const schemaFiles: SchemaFileConfig[] = [
    {
      filename: '01_springfood_authentication_seed_data.sql',
      schema: 'springfood_authentication',
      tables: [
        { table: 'role', records: roles },
        { table: 'user', records: [
          // Original 10 users
          ...users.map(u => {
            const { role, ...userWithoutRole } = u;
            return userWithoutRole;
          }),
          // Employee users (shop members)
          ...employeeUsers.map(u => {
            const { role, ...userWithoutRole } = u;
            return userWithoutRole;
          })
        ]},
        { table: 'user_has_role', records: [
          // Original user-role assignments
          ...userHasRole,
          // Employee user-role assignments (all STAFF role)
          ...employeeUsers.map(emp => ({
            user_id: emp.user_id,
            role_id: registry.getAllIds('roles').find(roleId => {
              const role = registry.getData('roles', roleId);
              return role && role.role_name === 'STAFF';
            })
          }))
        ]}
      ]
    },
    {
      filename: '02_springfood_shop_seed_data.sql',
      schema: 'springfood_shop',
      tables: [
        { table: 'shops', records: shops },
        { table: 'shop_members', records: shopMembers },
        { table: 'shop_wallets', records: shopWallets }
      ]
    },
    {
      filename: '03_springfood_product_seed_data.sql',
      schema: 'springfood_product',
      tables: [
        { table: 'categories', records: categories },
        { table: 'products', records: products.map(p => {
          // Remove temporary 'category' field before writing to SQL
          const { category, ...productWithoutCategory } = p;
          return productWithoutCategory;
        })},
        { table: 'product_categories', records: productCategories },
        { table: 'sales', records: sales.map(s => {
          // Remove temporary 'category' field before writing to SQL
          const { category, ...saleWithoutCategory } = s;
          return saleWithoutCategory;
        })},
        { table: 'product_sales', records: productSales }
      ]
    },
    {
      filename: '04_springfood_order_seed_data.sql',
      schema: 'springfood_order',
      tables: [
        { table: 'orders', records: orders },
        { table: 'order_items', records: orderItems }
      ]
    },
    {
      filename: '05_springfood_payment_seed_data.sql',
      schema: 'springfood_payment',
      tables: [
        // TODO: Implement payment_transactions generator (Task 9.1)
        // { table: 'payment_transactions', records: paymentTransactions }
      ]
    },
    {
      filename: '06_springfood_media_seed_data.sql',
      schema: 'springfood_media',
      tables: [
        // TODO: Implement media_file generator (Task 9.1)
        // { table: 'media_file', records: mediaFiles }
      ]
    },
    {
      filename: '07_springfood_notification_seed_data.sql',
      schema: 'springfood_notification',
      tables: [
        // TODO: Implement notifications generator (Task 9.1)
        // { table: 'notifications', records: notifications }
      ]
    },
    {
      filename: '08_springfood_chat_seed_data.sql',
      schema: 'springfood_chat',
      tables: [
        // TODO: Implement chat generators (Task 9.1)
        // { table: 'conversation', records: conversations },
        // { table: 'conversation_participant', records: conversationParticipants },
        // { table: 'message', records: messages }
      ]
    }
  ];

  // Generate SQL files
  for (const config of schemaFiles) {
    // Skip files with no tables
    if (config.tables.length === 0 || config.tables.every(t => t.records.length === 0)) {
      console.log(`  ⊘ Skipping ${config.filename} (no data)`);
      continue;
    }

    const sqlContent = writer.generateSQLFile(
      config.schema,
      config.tables,
      writeOptions
    );

    const filePath = path.join(outputDir, config.filename);
    fs.writeFileSync(filePath, sqlContent, 'utf-8');
    
    const totalRecords = config.tables.reduce((sum, t) => sum + t.records.length, 0);
    console.log(`  ✓ Generated ${config.filename} (${totalRecords} records)`);
  }

  console.log('');
  console.log('✓ SQL file generation complete!');
  console.log(`  Output directory: ${outputDir}`);
  console.log('');
  
  // Print summary
  console.log('Summary:');
  console.log(`  Roles: ${roles.length}`);
  console.log(`  Users: ${users.length + employeeUsers.length} (${users.length} original + ${employeeUsers.length} employees)`);
  console.log(`  User-Role assignments: ${userHasRole.length + employeeUsers.length}`);
  console.log(`  Shops: ${shops.length}`);
  console.log(`  Shop members: ${shopMembers.length}`);
  console.log(`  Shop wallets: ${shopWallets.length}`);
  console.log(`  Categories: ${categories.length}`);
  console.log(`  Products: ${products.length}`);
  console.log(`  Product-Category assignments: ${productCategories.length}`);
  console.log(`  Sales campaigns: ${sales.length}`);
  console.log(`  Product-Sale assignments: ${productSales.length}`);
  console.log(`  Orders: ${orders.length}`);
  console.log(`  Order items: ${orderItems.length}`);
  console.log('');
}

// Run if executed directly
if (require.main === module) {
  generateAllSQLFiles()
    .then(() => {
      console.log('Done!');
      process.exit(0);
    })
    .catch((error) => {
      console.error('Error generating SQL files:', error);
      process.exit(1);
    });
}
