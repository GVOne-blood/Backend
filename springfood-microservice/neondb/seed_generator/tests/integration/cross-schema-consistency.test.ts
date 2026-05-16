/**
 * Cross-Schema Consistency Test
 * 
 * Verifies data consistency across different schemas to ensure
 * cross-service functionality works correctly.
 * 
 * Requirements: 9.1, 9.2, 9.3, 9.4, 9.5, 9.6, 9.7, 9.8
 */

import * as fs from 'fs';
import * as path from 'path';

/**
 * Parse INSERT statements from SQL content
 */
function parseInsertStatements(content: string, tableName: string): Array<Record<string, string>> {
  const records: Array<Record<string, string>> = [];
  
  // Find INSERT INTO statements for this table
  const insertRegex = new RegExp(`INSERT INTO ${tableName.replace('.', '\\.')}\\s*\\(([^)]+)\\)\\s*VALUES\\s*([^;]+);`, 'gis');
  const matches = content.matchAll(insertRegex);
  
  for (const match of matches) {
    const columns = match[1].split(',').map(c => c.trim());
    const valuesSection = match[2];
    
    // Parse each row of values
    const rowRegex = /\(([^)]+)\)/g;
    const rowMatches = valuesSection.matchAll(rowRegex);
    
    for (const rowMatch of rowMatches) {
      const values = parseValues(rowMatch[1]);
      
      if (values.length === columns.length) {
        const record: Record<string, string> = {};
        for (let i = 0; i < columns.length; i++) {
          record[columns[i]] = values[i];
        }
        records.push(record);
      }
    }
  }
  
  return records;
}

/**
 * Parse comma-separated values, handling quoted strings and function calls
 */
function parseValues(valuesStr: string): string[] {
  const values: string[] = [];
  let current = '';
  let inQuotes = false;
  let quoteChar = '';
  let depth = 0;
  
  for (let i = 0; i < valuesStr.length; i++) {
    const char = valuesStr[i];
    const nextChar = valuesStr[i + 1];
    
    if ((char === "'" || char === '"') && (i === 0 || valuesStr[i - 1] !== '\\')) {
      if (!inQuotes) {
        inQuotes = true;
        quoteChar = char;
        current += char;
      } else if (char === quoteChar) {
        // Check for escaped quote (doubled quote)
        if (nextChar === quoteChar) {
          current += char + nextChar;
          i++; // Skip next char
        } else {
          inQuotes = false;
          current += char;
        }
      } else {
        current += char;
      }
    } else if (char === '(' && !inQuotes) {
      depth++;
      current += char;
    } else if (char === ')' && !inQuotes) {
      depth--;
      current += char;
    } else if (char === ',' && !inQuotes && depth === 0) {
      values.push(current.trim());
      current = '';
    } else {
      current += char;
    }
  }
  
  if (current.trim()) {
    values.push(current.trim());
  }
  
  return values;
}

/**
 * Extract actual value from SQL value (remove quotes, handle NULL, etc.)
 */
function extractValue(sqlValue: string): string | null {
  const trimmed = sqlValue.trim();
  
  // Handle NULL
  if (trimmed.toUpperCase() === 'NULL') {
    return null;
  }
  
  // Handle function calls (gen_random_uuid(), NOW(), etc.)
  if (trimmed.includes('(') && trimmed.includes(')')) {
    // For gen_random_uuid(), we can't extract the actual value
    // Return a placeholder that won't match anything
    return `__FUNCTION_${trimmed}__`;
  }
  
  // Handle quoted strings
  if ((trimmed.startsWith("'") && trimmed.endsWith("'")) ||
      (trimmed.startsWith('"') && trimmed.endsWith('"'))) {
    // Remove quotes and unescape
    return trimmed.slice(1, -1).replace(/''/g, "'").replace(/\\'/g, "'");
  }
  
  // Handle numbers and other literals
  return trimmed;
}

describe('Cross-Schema Consistency Test', () => {
  const seedDataDir = path.join(__dirname, '../../../seed_data');
  
  let authContent: string;
  let shopContent: string;
  let productContent: string;
  let orderContent: string;
  
  beforeAll(() => {
    // Read all SQL files
    authContent = fs.readFileSync(
      path.join(seedDataDir, '01_springfood_authentication_seed_data.sql'),
      'utf-8'
    );
    shopContent = fs.readFileSync(
      path.join(seedDataDir, '02_springfood_shop_seed_data.sql'),
      'utf-8'
    );
    productContent = fs.readFileSync(
      path.join(seedDataDir, '03_springfood_product_seed_data.sql'),
      'utf-8'
    );
    orderContent = fs.readFileSync(
      path.join(seedDataDir, '04_springfood_order_seed_data.sql'),
      'utf-8'
    );
  });

  describe('User Consistency Across Schemas', () => {
    it('should verify user_id in orders matches user_id in authentication.user (Requirement 9.1)', () => {
      // Parse users from authentication schema
      const users = parseInsertStatements(authContent, 'springfood_authentication.user');
      const userIds = new Set(
        users.map(u => extractValue(u['user_id'])).filter(id => id && !id.startsWith('__FUNCTION_'))
      );
      
      // Parse orders from order schema
      const orders = parseInsertStatements(orderContent, 'springfood_order.orders');
      
      // Verify all order user_ids exist in authentication.user
      for (const order of orders) {
        const userId = extractValue(order['user_id']);
        
        if (userId && !userId.startsWith('__FUNCTION_')) {
          expect(userIds.has(userId)).toBe(true);
        }
      }
      
      expect(orders.length).toBeGreaterThan(0);
    });

    it('should verify all orders belong to users with CUSTOMER role (Requirement 13.8)', () => {
      // Parse users and their roles
      const users = parseInsertStatements(authContent, 'springfood_authentication.user');
      const userHasRole = parseInsertStatements(authContent, 'springfood_authentication.user_has_role');
      
      // Build map of user_id -> roles
      const userRoles = new Map<string, Set<string>>();
      for (const uhr of userHasRole) {
        const userId = extractValue(uhr['user_id']);
        const roleName = extractValue(uhr['role_name']);
        
        if (userId && roleName && !userId.startsWith('__FUNCTION_')) {
          if (!userRoles.has(userId)) {
            userRoles.set(userId, new Set());
          }
          userRoles.get(userId)!.add(roleName);
        }
      }
      
      // Parse orders
      const orders = parseInsertStatements(orderContent, 'springfood_order.orders');
      
      // Verify all orders belong to CUSTOMER users
      for (const order of orders) {
        const userId = extractValue(order['user_id']);
        
        if (userId && !userId.startsWith('__FUNCTION_')) {
          const roles = userRoles.get(userId);
          expect(roles).toBeDefined();
          expect(roles!.has('CUSTOMER')).toBe(true);
        }
      }
      
      expect(orders.length).toBeGreaterThan(0);
    });

    it('should verify all SHOP_OWNER users have valid shop_id (Requirement 9.6, 12.7, 12.8)', () => {
      // Parse users
      const users = parseInsertStatements(authContent, 'springfood_authentication.user');
      const userHasRole = parseInsertStatements(authContent, 'springfood_authentication.user_has_role');
      
      // Parse shops
      const shops = parseInsertStatements(shopContent, 'springfood_shop.shops');
      const shopIds = new Set(
        shops.map(s => extractValue(s['shop_id'])).filter(id => id && !id.startsWith('__FUNCTION_'))
      );
      
      // Build map of user_id -> roles
      const userRoles = new Map<string, Set<string>>();
      for (const uhr of userHasRole) {
        const userId = extractValue(uhr['user_id']);
        const roleName = extractValue(uhr['role_name']);
        
        if (userId && roleName && !userId.startsWith('__FUNCTION_')) {
          if (!userRoles.has(userId)) {
            userRoles.set(userId, new Set());
          }
          userRoles.get(userId)!.add(roleName);
        }
      }
      
      // Verify SHOP_OWNER users have valid shop_id
      let shopOwnerCount = 0;
      for (const user of users) {
        const userId = extractValue(user['user_id']);
        const shopId = extractValue(user['shop_id']);
        
        if (userId && !userId.startsWith('__FUNCTION_')) {
          const roles = userRoles.get(userId);
          
          if (roles && roles.has('SHOP_OWNER')) {
            shopOwnerCount++;
            
            // SHOP_OWNER must have a shop_id
            expect(shopId).not.toBeNull();
            
            if (shopId && !shopId.startsWith('__FUNCTION_')) {
              // shop_id must reference existing shop
              expect(shopIds.has(shopId)).toBe(true);
            }
          }
        }
      }
      
      // Should have at least 2 shop owners (per requirements)
      expect(shopOwnerCount).toBeGreaterThanOrEqual(2);
    });
  });

  describe('Shop Consistency Across Schemas', () => {
    it('should verify shop_id in products matches shop_id in shop.shops (Requirement 9.2)', () => {
      // Parse shops
      const shops = parseInsertStatements(shopContent, 'springfood_shop.shops');
      const shopIds = new Set(
        shops.map(s => extractValue(s['shop_id'])).filter(id => id && !id.startsWith('__FUNCTION_'))
      );
      
      // Parse products
      const products = parseInsertStatements(productContent, 'springfood_product.products');
      
      // Verify all product shop_ids exist in shop.shops
      for (const product of products) {
        const shopId = extractValue(product['shop_id']);
        
        if (shopId && !shopId.startsWith('__FUNCTION_')) {
          expect(shopIds.has(shopId)).toBe(true);
        }
      }
      
      expect(products.length).toBeGreaterThan(0);
    });

    it('should verify shop_id in orders matches shop_id in shop.shops (Requirement 9.5)', () => {
      // Parse shops
      const shops = parseInsertStatements(shopContent, 'springfood_shop.shops');
      const shopIds = new Set(
        shops.map(s => extractValue(s['shop_id'])).filter(id => id && !id.startsWith('__FUNCTION_'))
      );
      
      // Parse orders
      const orders = parseInsertStatements(orderContent, 'springfood_order.orders');
      
      // Verify all order shop_ids exist in shop.shops
      for (const order of orders) {
        const shopId = extractValue(order['shop_id']);
        
        if (shopId && !shopId.startsWith('__FUNCTION_')) {
          expect(shopIds.has(shopId)).toBe(true);
        }
      }
      
      expect(orders.length).toBeGreaterThan(0);
    });

    it('should verify each shop has at least 5 products (Requirement 12.3)', () => {
      // Parse shops
      const shops = parseInsertStatements(shopContent, 'springfood_shop.shops');
      const shopIds = shops
        .map(s => extractValue(s['shop_id']))
        .filter(id => id && !id.startsWith('__FUNCTION_'));
      
      // Parse products
      const products = parseInsertStatements(productContent, 'springfood_product.products');
      
      // Count products per shop
      const productCountPerShop = new Map<string, number>();
      for (const product of products) {
        const shopId = extractValue(product['shop_id']);
        
        if (shopId && !shopId.startsWith('__FUNCTION_')) {
          productCountPerShop.set(shopId, (productCountPerShop.get(shopId) || 0) + 1);
        }
      }
      
      // Verify each shop has at least 5 products
      for (const shopId of shopIds) {
        const productCount = productCountPerShop.get(shopId) || 0;
        expect(productCount).toBeGreaterThanOrEqual(5);
      }
    });
  });

  describe('Product Consistency Across Schemas', () => {
    it('should verify product_id in order_items matches product_id in products (Requirement 9.3)', () => {
      // Parse products
      const products = parseInsertStatements(productContent, 'springfood_product.products');
      const productIds = new Set(
        products.map(p => extractValue(p['product_id'])).filter(id => id && !id.startsWith('__FUNCTION_'))
      );
      
      // Parse order items
      const orderItems = parseInsertStatements(orderContent, 'springfood_order.order_items');
      
      // Verify all order_item product_ids exist in products
      for (const item of orderItems) {
        const productId = extractValue(item['product_id']);
        
        if (productId && !productId.startsWith('__FUNCTION_')) {
          expect(productIds.has(productId)).toBe(true);
        }
      }
      
      expect(orderItems.length).toBeGreaterThan(0);
    });

    it('should verify order items reference products from same shop as order (Requirement 13.2)', () => {
      // Parse products with shop_id
      const products = parseInsertStatements(productContent, 'springfood_product.products');
      const productShopMap = new Map<string, string>();
      
      for (const product of products) {
        const productId = extractValue(product['product_id']);
        const shopId = extractValue(product['shop_id']);
        
        if (productId && shopId && !productId.startsWith('__FUNCTION_') && !shopId.startsWith('__FUNCTION_')) {
          productShopMap.set(productId, shopId);
        }
      }
      
      // Parse orders with shop_id
      const orders = parseInsertStatements(orderContent, 'springfood_order.orders');
      const orderShopMap = new Map<string, string>();
      
      for (const order of orders) {
        const orderId = extractValue(order['order_id']);
        const shopId = extractValue(order['shop_id']);
        
        if (orderId && shopId && !orderId.startsWith('__FUNCTION_') && !shopId.startsWith('__FUNCTION_')) {
          orderShopMap.set(orderId, shopId);
        }
      }
      
      // Parse order items
      const orderItems = parseInsertStatements(orderContent, 'springfood_order.order_items');
      
      // Verify each order item's product belongs to the same shop as the order
      for (const item of orderItems) {
        const orderId = extractValue(item['order_id']);
        const productId = extractValue(item['product_id']);
        
        if (orderId && productId && 
            !orderId.startsWith('__FUNCTION_') && 
            !productId.startsWith('__FUNCTION_')) {
          
          const orderShopId = orderShopMap.get(orderId);
          const productShopId = productShopMap.get(productId);
          
          expect(orderShopId).toBeDefined();
          expect(productShopId).toBeDefined();
          expect(orderShopId).toBe(productShopId);
        }
      }
      
      expect(orderItems.length).toBeGreaterThan(0);
    });
  });

  describe('Data Consistency Within Schema', () => {
    it('should verify same user_id represents same user across all references (Requirement 9.6)', () => {
      // Parse users
      const users = parseInsertStatements(authContent, 'springfood_authentication.user');
      const userEmails = new Map<string, string>();
      
      for (const user of users) {
        const userId = extractValue(user['user_id']);
        const email = extractValue(user['email']);
        
        if (userId && email && !userId.startsWith('__FUNCTION_')) {
          userEmails.set(userId, email);
        }
      }
      
      // Parse user_has_role
      const userHasRole = parseInsertStatements(authContent, 'springfood_authentication.user_has_role');
      
      // Verify all user_ids in user_has_role exist in users
      for (const uhr of userHasRole) {
        const userId = extractValue(uhr['user_id']);
        
        if (userId && !userId.startsWith('__FUNCTION_')) {
          expect(userEmails.has(userId)).toBe(true);
        }
      }
      
      // Parse orders
      const orders = parseInsertStatements(orderContent, 'springfood_order.orders');
      
      // Verify all user_ids in orders exist in users
      for (const order of orders) {
        const userId = extractValue(order['user_id']);
        
        if (userId && !userId.startsWith('__FUNCTION_')) {
          expect(userEmails.has(userId)).toBe(true);
        }
      }
    });

    it('should verify same shop_id represents same shop across all references (Requirement 9.7)', () => {
      // Parse shops
      const shops = parseInsertStatements(shopContent, 'springfood_shop.shops');
      const shopNames = new Map<string, string>();
      
      for (const shop of shops) {
        const shopId = extractValue(shop['shop_id']);
        const shopName = extractValue(shop['shop_name']);
        
        if (shopId && shopName && !shopId.startsWith('__FUNCTION_')) {
          shopNames.set(shopId, shopName);
        }
      }
      
      // Parse products
      const products = parseInsertStatements(productContent, 'springfood_product.products');
      
      // Verify all shop_ids in products exist in shops
      for (const product of products) {
        const shopId = extractValue(product['shop_id']);
        
        if (shopId && !shopId.startsWith('__FUNCTION_')) {
          expect(shopNames.has(shopId)).toBe(true);
        }
      }
      
      // Parse orders
      const orders = parseInsertStatements(orderContent, 'springfood_order.orders');
      
      // Verify all shop_ids in orders exist in shops
      for (const order of orders) {
        const shopId = extractValue(order['shop_id']);
        
        if (shopId && !shopId.startsWith('__FUNCTION_')) {
          expect(shopNames.has(shopId)).toBe(true);
        }
      }
    });

    it('should verify same product_id represents same product across all references (Requirement 9.8)', () => {
      // Parse products
      const products = parseInsertStatements(productContent, 'springfood_product.products');
      const productNames = new Map<string, string>();
      
      for (const product of products) {
        const productId = extractValue(product['product_id']);
        const productName = extractValue(product['name']);
        
        if (productId && productName && !productId.startsWith('__FUNCTION_')) {
          productNames.set(productId, productName);
        }
      }
      
      // Parse product_categories
      const productCategories = parseInsertStatements(productContent, 'springfood_product.product_categories');
      
      // Verify all product_ids in product_categories exist in products
      for (const pc of productCategories) {
        const productId = extractValue(pc['product_id']);
        
        if (productId && !productId.startsWith('__FUNCTION_')) {
          expect(productNames.has(productId)).toBe(true);
        }
      }
      
      // Parse product_sales
      const productSales = parseInsertStatements(productContent, 'springfood_product.product_sales');
      
      // Verify all product_ids in product_sales exist in products
      for (const ps of productSales) {
        const productId = extractValue(ps['product_id']);
        
        if (productId && !productId.startsWith('__FUNCTION_')) {
          expect(productNames.has(productId)).toBe(true);
        }
      }
      
      // Parse order_items
      const orderItems = parseInsertStatements(orderContent, 'springfood_order.order_items');
      
      // Verify all product_ids in order_items exist in products
      for (const item of orderItems) {
        const productId = extractValue(item['product_id']);
        
        if (productId && !productId.startsWith('__FUNCTION_')) {
          expect(productNames.has(productId)).toBe(true);
        }
      }
    });
  });

  describe('Order Totals Consistency', () => {
    it('should verify order totals are calculated correctly (Requirement 13.5)', () => {
      // Parse orders
      const orders = parseInsertStatements(orderContent, 'springfood_order.orders');
      const orderTotals = new Map<string, {
        subtotal: number;
        shipping: number;
        discount: number;
        final: number;
      }>();
      
      for (const order of orders) {
        const orderId = extractValue(order['order_id']);
        const subtotal = parseFloat(extractValue(order['subtotal_amount']) || '0');
        const shipping = parseFloat(extractValue(order['shipping_fee']) || '0');
        const discount = parseFloat(extractValue(order['discount_amount']) || '0');
        const final = parseFloat(extractValue(order['final_price']) || '0');
        
        if (orderId && !orderId.startsWith('__FUNCTION_')) {
          orderTotals.set(orderId, { subtotal, shipping, discount, final });
        }
      }
      
      // Parse order items
      const orderItems = parseInsertStatements(orderContent, 'springfood_order.order_items');
      const orderItemTotals = new Map<string, number>();
      
      for (const item of orderItems) {
        const orderId = extractValue(item['order_id']);
        const quantity = parseInt(extractValue(item['quantity']) || '0');
        const price = parseFloat(extractValue(item['price_at_booking']) || '0');
        const itemTotal = quantity * price;
        
        if (orderId && !orderId.startsWith('__FUNCTION_')) {
          orderItemTotals.set(orderId, (orderItemTotals.get(orderId) || 0) + itemTotal);
        }
      }
      
      // Verify order totals match calculated values
      for (const [orderId, totals] of orderTotals.entries()) {
        const calculatedSubtotal = orderItemTotals.get(orderId) || 0;
        const calculatedFinal = calculatedSubtotal + totals.shipping - totals.discount;
        
        // Allow small floating point differences (0.01)
        expect(Math.abs(totals.subtotal - calculatedSubtotal)).toBeLessThan(0.01);
        expect(Math.abs(totals.final - calculatedFinal)).toBeLessThan(0.01);
      }
    });
  });
});
