/**
 * Referential Integrity Test
 * 
 * Parses all generated SQL files and verifies that all foreign key values
 * reference existing primary keys.
 * 
 * Requirements: 1.1, 1.2, 1.3, 1.4, 1.5, 1.6, 9.1, 9.2, 9.3, 9.4, 9.5, 9.6, 9.7, 9.8
 */

import * as fs from 'fs';
import * as path from 'path';

/**
 * Represents a table's primary key registry
 */
interface PKRegistry {
  [table: string]: Set<string>;
}

/**
 * Represents a foreign key violation
 */
interface FKViolation {
  table: string;
  column: string;
  value: string;
  referencedTable: string;
  referencedColumn: string;
}

/**
 * Foreign key definitions for all tables
 */
const FOREIGN_KEYS: Record<string, Array<{
  column: string;
  referencedTable: string;
  referencedColumn: string;
}>> = {
  'springfood_authentication.user_has_role': [
    { column: 'user_id', referencedTable: 'springfood_authentication.user', referencedColumn: 'user_id' },
    { column: 'role_name', referencedTable: 'springfood_authentication.role', referencedColumn: 'role_name' }
  ],
  'springfood_authentication.address': [
    { column: 'user_id', referencedTable: 'springfood_authentication.user', referencedColumn: 'user_id' }
  ],
  'springfood_shop.shop_members': [
    { column: 'shop_id', referencedTable: 'springfood_shop.shops', referencedColumn: 'shop_id' },
    { column: 'user_id', referencedTable: 'springfood_authentication.user', referencedColumn: 'user_id' }
  ],
  'springfood_shop.shop_wallets': [
    { column: 'shop_id', referencedTable: 'springfood_shop.shops', referencedColumn: 'shop_id' }
  ],
  'springfood_product.categories': [
    { column: 'parent_id', referencedTable: 'springfood_product.categories', referencedColumn: 'category_name' }
  ],
  'springfood_product.products': [
    { column: 'shop_id', referencedTable: 'springfood_shop.shops', referencedColumn: 'shop_id' }
  ],
  'springfood_product.product_categories': [
    { column: 'product_id', referencedTable: 'springfood_product.products', referencedColumn: 'product_id' },
    { column: 'category_name', referencedTable: 'springfood_product.categories', referencedColumn: 'category_name' }
  ],
  'springfood_product.product_images': [
    { column: 'product_id', referencedTable: 'springfood_product.products', referencedColumn: 'product_id' }
  ],
  'springfood_product.feedbacks': [
    { column: 'product_id', referencedTable: 'springfood_product.products', referencedColumn: 'product_id' },
    { column: 'user_id', referencedTable: 'springfood_authentication.user', referencedColumn: 'user_id' }
  ],
  'springfood_product.product_sales': [
    { column: 'product_id', referencedTable: 'springfood_product.products', referencedColumn: 'product_id' },
    { column: 'sale_id', referencedTable: 'springfood_product.sales', referencedColumn: 'sale_id' }
  ],
  'springfood_order.orders': [
    { column: 'user_id', referencedTable: 'springfood_authentication.user', referencedColumn: 'user_id' },
    { column: 'shop_id', referencedTable: 'springfood_shop.shops', referencedColumn: 'shop_id' }
  ],
  'springfood_order.order_items': [
    { column: 'order_id', referencedTable: 'springfood_order.orders', referencedColumn: 'order_id' },
    { column: 'product_id', referencedTable: 'springfood_product.products', referencedColumn: 'product_id' }
  ],
  'springfood_payment.payment_transactions': [
    { column: 'order_id', referencedTable: 'springfood_order.orders', referencedColumn: 'order_id' }
  ],
  'springfood_chat.conversation': [
    { column: 'user_id', referencedTable: 'springfood_authentication.user', referencedColumn: 'user_id' }
  ],
  'springfood_chat.conversation_participant': [
    { column: 'conversation_id', referencedTable: 'springfood_chat.conversation', referencedColumn: 'conversation_id' },
    { column: 'user_id', referencedTable: 'springfood_authentication.user', referencedColumn: 'user_id' }
  ],
  'springfood_chat.message': [
    { column: 'conversation_id', referencedTable: 'springfood_chat.conversation', referencedColumn: 'conversation_id' },
    { column: 'sender_id', referencedTable: 'springfood_authentication.user', referencedColumn: 'user_id' }
  ],
  'springfood_notification.notifications': [
    { column: 'user_id', referencedTable: 'springfood_authentication.user', referencedColumn: 'user_id' }
  ],
  'springfood_media.media_file': [
    { column: 'uploaded_by', referencedTable: 'springfood_authentication.user', referencedColumn: 'user_id' }
  ]
};

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

/**
 * Build primary key registry from all SQL files
 */
function buildPKRegistry(seedDataDir: string): PKRegistry {
  const registry: PKRegistry = {};
  
  const sqlFiles = [
    '01_springfood_authentication_seed_data.sql',
    '02_springfood_shop_seed_data.sql',
    '03_springfood_product_seed_data.sql',
    '04_springfood_order_seed_data.sql',
    '05_springfood_payment_seed_data.sql',
    '06_springfood_media_seed_data.sql',
    '07_springfood_notification_seed_data.sql',
    '08_springfood_chat_seed_data.sql'
  ];
  
  // Table primary key definitions
  const primaryKeys: Record<string, string> = {
    'springfood_authentication.role': 'role_name',
    'springfood_authentication.user': 'user_id',
    'springfood_shop.shops': 'shop_id',
    'springfood_product.categories': 'category_name',
    'springfood_product.products': 'product_id',
    'springfood_product.sales': 'sale_id',
    'springfood_order.orders': 'order_id',
    'springfood_order.order_items': 'order_item_id',
    'springfood_payment.payment_transactions': 'transaction_id',
    'springfood_chat.conversation': 'conversation_id',
    'springfood_notification.notifications': 'notification_id',
    'springfood_media.media_file': 'file_id'
  };
  
  for (const filename of sqlFiles) {
    const filePath = path.join(seedDataDir, filename);
    
    if (!fs.existsSync(filePath)) {
      continue;
    }
    
    const content = fs.readFileSync(filePath, 'utf-8');
    
    // Parse each table in this file
    for (const [tableName, pkColumn] of Object.entries(primaryKeys)) {
      const records = parseInsertStatements(content, tableName);
      
      if (records.length > 0) {
        if (!registry[tableName]) {
          registry[tableName] = new Set();
        }
        
        for (const record of records) {
          const pkValue = extractValue(record[pkColumn]);
          if (pkValue && !pkValue.startsWith('__FUNCTION_')) {
            registry[tableName].add(pkValue);
          }
        }
      }
    }
  }
  
  return registry;
}

/**
 * Verify foreign key integrity
 */
function verifyForeignKeys(seedDataDir: string, pkRegistry: PKRegistry): FKViolation[] {
  const violations: FKViolation[] = [];
  
  const sqlFiles = [
    '01_springfood_authentication_seed_data.sql',
    '02_springfood_shop_seed_data.sql',
    '03_springfood_product_seed_data.sql',
    '04_springfood_order_seed_data.sql',
    '05_springfood_payment_seed_data.sql',
    '06_springfood_media_seed_data.sql',
    '07_springfood_notification_seed_data.sql',
    '08_springfood_chat_seed_data.sql'
  ];
  
  for (const filename of sqlFiles) {
    const filePath = path.join(seedDataDir, filename);
    
    if (!fs.existsSync(filePath)) {
      continue;
    }
    
    const content = fs.readFileSync(filePath, 'utf-8');
    
    // Check each table with foreign keys
    for (const [tableName, fks] of Object.entries(FOREIGN_KEYS)) {
      const records = parseInsertStatements(content, tableName);
      
      for (const record of records) {
        for (const fk of fks) {
          const fkValue = extractValue(record[fk.column]);
          
          // Skip NULL values (nullable foreign keys)
          if (fkValue === null) {
            continue;
          }
          
          // Skip function calls (can't verify at parse time)
          if (fkValue.startsWith('__FUNCTION_')) {
            continue;
          }
          
          // Check if referenced value exists
          const referencedPKs = pkRegistry[fk.referencedTable];
          if (!referencedPKs || !referencedPKs.has(fkValue)) {
            violations.push({
              table: tableName,
              column: fk.column,
              value: fkValue,
              referencedTable: fk.referencedTable,
              referencedColumn: fk.referencedColumn
            });
          }
        }
      }
    }
  }
  
  return violations;
}

describe('Referential Integrity Test', () => {
  const seedDataDir = path.join(__dirname, '../../../seed_data');
  let pkRegistry: PKRegistry;
  let violations: FKViolation[];
  
  beforeAll(() => {
    // Build primary key registry
    pkRegistry = buildPKRegistry(seedDataDir);
    
    // Verify foreign keys
    violations = verifyForeignKeys(seedDataDir, pkRegistry);
  });

  describe('Primary Key Registry', () => {
    it('should build registry for all tables', () => {
      expect(Object.keys(pkRegistry).length).toBeGreaterThan(0);
    });

    it('should register user IDs', () => {
      expect(pkRegistry['springfood_authentication.user']).toBeDefined();
      expect(pkRegistry['springfood_authentication.user'].size).toBe(10);
    });

    it('should register role names', () => {
      expect(pkRegistry['springfood_authentication.role']).toBeDefined();
      expect(pkRegistry['springfood_authentication.role'].size).toBe(5);
      
      // Check fixed roles
      expect(pkRegistry['springfood_authentication.role'].has('CUSTOMER')).toBe(true);
      expect(pkRegistry['springfood_authentication.role'].has('SHOP_OWNER')).toBe(true);
      expect(pkRegistry['springfood_authentication.role'].has('ADMIN')).toBe(true);
      expect(pkRegistry['springfood_authentication.role'].has('STAFF')).toBe(true);
      expect(pkRegistry['springfood_authentication.role'].has('DELIVER')).toBe(true);
    });

    it('should register shop IDs', () => {
      expect(pkRegistry['springfood_shop.shops']).toBeDefined();
      expect(pkRegistry['springfood_shop.shops'].size).toBeGreaterThanOrEqual(25);
    });

    it('should register category names', () => {
      expect(pkRegistry['springfood_product.categories']).toBeDefined();
      expect(pkRegistry['springfood_product.categories'].size).toBeGreaterThanOrEqual(50);
    });

    it('should register product IDs', () => {
      expect(pkRegistry['springfood_product.products']).toBeDefined();
      expect(pkRegistry['springfood_product.products'].size).toBeGreaterThanOrEqual(150);
    });

    it('should register sale IDs', () => {
      expect(pkRegistry['springfood_product.sales']).toBeDefined();
      expect(pkRegistry['springfood_product.sales'].size).toBeGreaterThanOrEqual(15);
    });

    it('should register order IDs', () => {
      expect(pkRegistry['springfood_order.orders']).toBeDefined();
      expect(pkRegistry['springfood_order.orders'].size).toBeGreaterThanOrEqual(10);
    });
  });

  describe('Foreign Key Integrity', () => {
    it('should have no foreign key violations', () => {
      if (violations.length > 0) {
        console.error('Foreign Key Violations:');
        for (const violation of violations) {
          console.error(`  - ${violation.table}.${violation.column} = '${violation.value}' ` +
                       `references ${violation.referencedTable}.${violation.referencedColumn} (NOT FOUND)`);
        }
      }
      
      expect(violations).toHaveLength(0);
    });

    it('should verify user_has_role references (Requirement 1.6)', () => {
      const authFile = path.join(seedDataDir, '01_springfood_authentication_seed_data.sql');
      const content = fs.readFileSync(authFile, 'utf-8');
      
      const records = parseInsertStatements(content, 'springfood_authentication.user_has_role');
      
      for (const record of records) {
        const userId = extractValue(record['user_id']);
        const roleName = extractValue(record['role_name']);
        
        if (userId && !userId.startsWith('__FUNCTION_')) {
          expect(pkRegistry['springfood_authentication.user'].has(userId)).toBe(true);
        }
        
        if (roleName) {
          expect(pkRegistry['springfood_authentication.role'].has(roleName)).toBe(true);
        }
      }
    });

    it('should verify product_categories references (Requirement 1.5)', () => {
      const productFile = path.join(seedDataDir, '03_springfood_product_seed_data.sql');
      const content = fs.readFileSync(productFile, 'utf-8');
      
      const records = parseInsertStatements(content, 'springfood_product.product_categories');
      
      for (const record of records) {
        const productId = extractValue(record['product_id']);
        const categoryName = extractValue(record['category_name']);
        
        if (productId && !productId.startsWith('__FUNCTION_')) {
          expect(pkRegistry['springfood_product.products'].has(productId)).toBe(true);
        }
        
        if (categoryName) {
          expect(pkRegistry['springfood_product.categories'].has(categoryName)).toBe(true);
        }
      }
    });

    it('should verify product_sales references (Requirement 9.3)', () => {
      const productFile = path.join(seedDataDir, '03_springfood_product_seed_data.sql');
      const content = fs.readFileSync(productFile, 'utf-8');
      
      const records = parseInsertStatements(content, 'springfood_product.product_sales');
      
      for (const record of records) {
        const productId = extractValue(record['product_id']);
        const saleId = extractValue(record['sale_id']);
        
        if (productId && !productId.startsWith('__FUNCTION_')) {
          expect(pkRegistry['springfood_product.products'].has(productId)).toBe(true);
        }
        
        if (saleId && !saleId.startsWith('__FUNCTION_')) {
          expect(pkRegistry['springfood_product.sales'].has(saleId)).toBe(true);
        }
      }
    });

    it('should verify products reference shops (Requirement 1.3)', () => {
      const productFile = path.join(seedDataDir, '03_springfood_product_seed_data.sql');
      const content = fs.readFileSync(productFile, 'utf-8');
      
      const records = parseInsertStatements(content, 'springfood_product.products');
      
      for (const record of records) {
        const shopId = extractValue(record['shop_id']);
        
        if (shopId && !shopId.startsWith('__FUNCTION_')) {
          expect(pkRegistry['springfood_shop.shops'].has(shopId)).toBe(true);
        }
      }
    });

    it('should verify orders reference users and shops (Requirement 1.2, 9.1, 9.5)', () => {
      const orderFile = path.join(seedDataDir, '04_springfood_order_seed_data.sql');
      const content = fs.readFileSync(orderFile, 'utf-8');
      
      const records = parseInsertStatements(content, 'springfood_order.orders');
      
      for (const record of records) {
        const userId = extractValue(record['user_id']);
        const shopId = extractValue(record['shop_id']);
        
        if (userId && !userId.startsWith('__FUNCTION_')) {
          expect(pkRegistry['springfood_authentication.user'].has(userId)).toBe(true);
        }
        
        if (shopId && !shopId.startsWith('__FUNCTION_')) {
          expect(pkRegistry['springfood_shop.shops'].has(shopId)).toBe(true);
        }
      }
    });

    it('should verify order_items reference orders and products (Requirement 1.4, 9.2)', () => {
      const orderFile = path.join(seedDataDir, '04_springfood_order_seed_data.sql');
      const content = fs.readFileSync(orderFile, 'utf-8');
      
      const records = parseInsertStatements(content, 'springfood_order.order_items');
      
      for (const record of records) {
        const orderId = extractValue(record['order_id']);
        const productId = extractValue(record['product_id']);
        
        if (orderId && !orderId.startsWith('__FUNCTION_')) {
          expect(pkRegistry['springfood_order.orders'].has(orderId)).toBe(true);
        }
        
        if (productId && !productId.startsWith('__FUNCTION_')) {
          expect(pkRegistry['springfood_product.products'].has(productId)).toBe(true);
        }
      }
    });

    it('should verify self-referencing categories (parent_id)', () => {
      const productFile = path.join(seedDataDir, '03_springfood_product_seed_data.sql');
      const content = fs.readFileSync(productFile, 'utf-8');
      
      const records = parseInsertStatements(content, 'springfood_product.categories');
      
      for (const record of records) {
        const parentId = extractValue(record['parent_id']);
        
        // parent_id can be NULL for root categories
        if (parentId && parentId !== null) {
          expect(pkRegistry['springfood_product.categories'].has(parentId)).toBe(true);
        }
      }
    });
  });

  describe('Violation Reporting', () => {
    it('should report violations with table, column, and invalid value', () => {
      // This test passes if there are no violations
      // If there are violations, they should be reported with full details
      
      for (const violation of violations) {
        expect(violation).toHaveProperty('table');
        expect(violation).toHaveProperty('column');
        expect(violation).toHaveProperty('value');
        expect(violation).toHaveProperty('referencedTable');
        expect(violation).toHaveProperty('referencedColumn');
      }
    });
  });
});
