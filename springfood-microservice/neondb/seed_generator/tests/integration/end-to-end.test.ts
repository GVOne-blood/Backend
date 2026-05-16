/**
 * End-to-End Generation Test
 * 
 * Tests the full data generation pipeline from DDL parsing to SQL file creation.
 * 
 * Requirements: 3.1, 3.2, 3.3, 3.4, 3.5, 3.6, 3.7, 2.7
 */

import * as fs from 'fs';
import * as path from 'path';
import { generateAllSQLFiles } from '../../src/generate-sql-files';

describe('End-to-End Generation Test', () => {
  const seedDataDir = path.join(__dirname, '../../../seed_data');
  
  beforeAll(async () => {
    // Run the full generation pipeline
    await generateAllSQLFiles();
  });

  describe('SQL File Creation', () => {
    it('should create all 8 SQL files with correct naming', () => {
      const expectedFiles = [
        '01_springfood_authentication_seed_data.sql',
        '02_springfood_shop_seed_data.sql',
        '03_springfood_product_seed_data.sql',
        '04_springfood_order_seed_data.sql',
        '05_springfood_payment_seed_data.sql',
        '06_springfood_media_seed_data.sql',
        '07_springfood_notification_seed_data.sql',
        '08_springfood_chat_seed_data.sql'
      ];

      for (const filename of expectedFiles) {
        const filePath = path.join(seedDataDir, filename);
        
        // Check if file exists
        expect(fs.existsSync(filePath)).toBe(true);
        
        // Check if file is not empty (except for schemas not yet implemented)
        const content = fs.readFileSync(filePath, 'utf-8');
        
        // Files 01-04 should have content (implemented)
        if (filename.startsWith('01') || filename.startsWith('02') || 
            filename.startsWith('03') || filename.startsWith('04')) {
          expect(content.length).toBeGreaterThan(0);
          expect(content).toContain('INSERT INTO');
        }
        
        // Files 05-08 may be empty (not yet implemented in Task 9.1)
        // We just verify they exist
      }
    });

    it('should create master execution script', () => {
      const masterScriptPath = path.join(seedDataDir, 'run_all_seeds.sql');
      expect(fs.existsSync(masterScriptPath)).toBe(true);
      
      const content = fs.readFileSync(masterScriptPath, 'utf-8');
      expect(content.length).toBeGreaterThan(0);
      
      // Should reference all 8 SQL files in order
      expect(content).toContain('01_springfood_authentication_seed_data.sql');
      expect(content).toContain('02_springfood_shop_seed_data.sql');
      expect(content).toContain('03_springfood_product_seed_data.sql');
      expect(content).toContain('04_springfood_order_seed_data.sql');
    });

    it('should create batch/shell wrapper scripts', () => {
      const batScriptPath = path.join(seedDataDir, 'run_seeds.bat');
      const shScriptPath = path.join(seedDataDir, 'run_seeds.sh');
      
      expect(fs.existsSync(batScriptPath)).toBe(true);
      expect(fs.existsSync(shScriptPath)).toBe(true);
      
      const batContent = fs.readFileSync(batScriptPath, 'utf-8');
      const shContent = fs.readFileSync(shScriptPath, 'utf-8');
      
      expect(batContent.length).toBeGreaterThan(0);
      expect(shContent.length).toBeGreaterThan(0);
    });

    it('should create README documentation', () => {
      const readmePath = path.join(seedDataDir, 'README.md');
      expect(fs.existsSync(readmePath)).toBe(true);
      
      const content = fs.readFileSync(readmePath, 'utf-8');
      expect(content.length).toBeGreaterThan(0);
      
      // Should document execution order
      expect(content).toContain('execution');
      
      // Should document user accounts
      expect(content).toContain('user');
      expect(content).toContain('password');
    });
  });

  describe('Record Count Verification', () => {
    it('should generate at least 150 products (Requirement 3.1)', () => {
      const productFile = path.join(seedDataDir, '03_springfood_product_seed_data.sql');
      const content = fs.readFileSync(productFile, 'utf-8');
      
      // Count INSERT statements for products table
      const productInserts = content.match(/INSERT INTO springfood_product\.products/gi) || [];
      
      // Each INSERT may contain multiple records, so we need to count actual records
      // Count the number of gen_random_uuid() calls in products section
      const productsSection = content.split('INSERT INTO springfood_product.products')[1]?.split('INSERT INTO')[0] || '';
      const uuidCalls = productsSection.match(/gen_random_uuid\(\)/gi) || [];
      
      // Each product has one UUID (product_id)
      const productCount = uuidCalls.length;
      
      expect(productCount).toBeGreaterThanOrEqual(150);
    });

    it('should generate at least 50 categories (Requirement 3.2)', () => {
      const productFile = path.join(seedDataDir, '03_springfood_product_seed_data.sql');
      const content = fs.readFileSync(productFile, 'utf-8');
      
      // Count category records
      const categoriesSection = content.split('INSERT INTO springfood_product.categories')[1]?.split('INSERT INTO')[0] || '';
      
      // Count the number of category_name values (each line with a category)
      const categoryLines = categoriesSection.split('\n').filter(line => 
        line.trim().startsWith('(') && line.includes(',')
      );
      
      expect(categoryLines.length).toBeGreaterThanOrEqual(50);
    });

    it('should generate at least 25 shops (Requirement 3.5)', () => {
      const shopFile = path.join(seedDataDir, '02_springfood_shop_seed_data.sql');
      const content = fs.readFileSync(shopFile, 'utf-8');
      
      // Count shop records
      const shopsSection = content.split('INSERT INTO springfood_shop.shops')[1] || '';
      const uuidCalls = shopsSection.match(/gen_random_uuid\(\)/gi) || [];
      
      expect(uuidCalls.length).toBeGreaterThanOrEqual(25);
    });

    it('should generate 15-20 sales campaigns (Requirement 2.7)', () => {
      const productFile = path.join(seedDataDir, '03_springfood_product_seed_data.sql');
      const content = fs.readFileSync(productFile, 'utf-8');
      
      // Count sales records
      const salesSection = content.split('INSERT INTO springfood_product.sales')[1]?.split('INSERT INTO')[0] || '';
      const uuidCalls = salesSection.match(/gen_random_uuid\(\)/gi) || [];
      
      expect(uuidCalls.length).toBeGreaterThanOrEqual(15);
      expect(uuidCalls.length).toBeLessThanOrEqual(20);
    });

    it('should generate at least 10 orders (Requirement 3.6)', () => {
      const orderFile = path.join(seedDataDir, '04_springfood_order_seed_data.sql');
      const content = fs.readFileSync(orderFile, 'utf-8');
      
      // Count order records
      const ordersSection = content.split('INSERT INTO springfood_order.orders')[1]?.split('INSERT INTO')[0] || '';
      const uuidCalls = ordersSection.match(/gen_random_uuid\(\)/gi) || [];
      
      expect(uuidCalls.length).toBeGreaterThanOrEqual(10);
    });

    it('should generate exactly 5 roles (Requirement 3.3)', () => {
      const authFile = path.join(seedDataDir, '01_springfood_authentication_seed_data.sql');
      const content = fs.readFileSync(authFile, 'utf-8');
      
      // Count role records
      const rolesSection = content.split('INSERT INTO springfood_authentication.role')[1]?.split('INSERT INTO')[0] || '';
      
      // Count the fixed roles
      expect(rolesSection).toContain('CUSTOMER');
      expect(rolesSection).toContain('SHOP_OWNER');
      expect(rolesSection).toContain('ADMIN');
      expect(rolesSection).toContain('STAFF');
      expect(rolesSection).toContain('DELIVER');
      
      // Count role lines
      const roleLines = rolesSection.split('\n').filter(line => 
        line.trim().startsWith('(') && line.includes(',')
      );
      
      expect(roleLines.length).toBe(5);
    });

    it('should generate exactly 10 users (Requirement 3.4)', () => {
      const authFile = path.join(seedDataDir, '01_springfood_authentication_seed_data.sql');
      const content = fs.readFileSync(authFile, 'utf-8');
      
      // Count user records
      const usersSection = content.split('INSERT INTO springfood_authentication.user')[1]?.split('INSERT INTO')[0] || '';
      const uuidCalls = usersSection.match(/gen_random_uuid\(\)/gi) || [];
      
      expect(uuidCalls.length).toBe(10);
    });
  });

  describe('SQL Syntax Validation', () => {
    it('should generate valid SQL syntax for all files', () => {
      const sqlFiles = [
        '01_springfood_authentication_seed_data.sql',
        '02_springfood_shop_seed_data.sql',
        '03_springfood_product_seed_data.sql',
        '04_springfood_order_seed_data.sql'
      ];

      for (const filename of sqlFiles) {
        const filePath = path.join(seedDataDir, filename);
        const content = fs.readFileSync(filePath, 'utf-8');
        
        // Check for basic SQL syntax elements
        expect(content).toContain('BEGIN;');
        expect(content).toContain('COMMIT;');
        expect(content).toContain('INSERT INTO');
        
        // Check for proper schema qualification
        const schemaName = filename.split('_')[1]; // Extract schema name
        expect(content).toMatch(new RegExp(`INSERT INTO springfood_${schemaName}\\.`, 'i'));
        
        // Check for ON CONFLICT clause (idempotency)
        expect(content).toContain('ON CONFLICT');
        expect(content).toContain('DO NOTHING');
      }
    });

    it('should use gen_random_uuid() for UUID generation', () => {
      const authFile = path.join(seedDataDir, '01_springfood_authentication_seed_data.sql');
      const content = fs.readFileSync(authFile, 'utf-8');
      
      expect(content).toContain('gen_random_uuid()');
    });

    it('should use NOW() for timestamp fields', () => {
      const authFile = path.join(seedDataDir, '01_springfood_authentication_seed_data.sql');
      const content = fs.readFileSync(authFile, 'utf-8');
      
      expect(content).toContain('NOW()');
    });

    it('should properly escape special characters in strings', () => {
      const productFile = path.join(seedDataDir, '03_springfood_product_seed_data.sql');
      const content = fs.readFileSync(productFile, 'utf-8');
      
      // Check that single quotes are escaped
      // Vietnamese text often contains apostrophes
      if (content.includes("'")) {
        // If there are single quotes in data, they should be escaped as ''
        const dataSection = content.split('VALUES')[1] || '';
        
        // Check for proper escaping pattern ('' instead of ')
        // This is a basic check - proper escaping should not have unescaped quotes
        expect(dataSection).not.toMatch(/[^']'[^']/); // No single unescaped quotes
      }
    });
  });

  describe('Data Quality Checks', () => {
    it('should generate realistic Vietnamese food product names', () => {
      const productFile = path.join(seedDataDir, '03_springfood_product_seed_data.sql');
      const content = fs.readFileSync(productFile, 'utf-8');
      
      // Check for Vietnamese food keywords
      const vietnameseKeywords = ['Phở', 'Bún', 'Cơm', 'Bánh', 'Cà Phê', 'Trà'];
      const hasVietnameseFood = vietnameseKeywords.some(keyword => content.includes(keyword));
      
      expect(hasVietnameseFood).toBe(true);
    });

    it('should generate realistic shop names', () => {
      const shopFile = path.join(seedDataDir, '02_springfood_shop_seed_data.sql');
      const content = fs.readFileSync(shopFile, 'utf-8');
      
      // Check for shop name patterns
      expect(content.length).toBeGreaterThan(0);
      
      // Should contain Vietnamese shop names or brand names
      const shopKeywords = ['Quán', 'Coffee', 'Phở', 'Bún', 'Gong Cha', 'Highlands'];
      const hasRealisticShops = shopKeywords.some(keyword => content.includes(keyword));
      
      expect(hasRealisticShops).toBe(true);
    });

    it('should generate realistic price ranges', () => {
      const productFile = path.join(seedDataDir, '03_springfood_product_seed_data.sql');
      const content = fs.readFileSync(productFile, 'utf-8');
      
      // Prices should be in VND range (10,000 - 500,000)
      // Check for numeric values in reasonable range
      const priceMatches = content.match(/\d+\.\d{2}/g) || [];
      
      expect(priceMatches.length).toBeGreaterThan(0);
      
      // Sample check: at least some prices should be in reasonable range
      const prices = priceMatches.map(p => parseFloat(p));
      const reasonablePrices = prices.filter(p => p >= 10000 && p <= 500000);
      
      expect(reasonablePrices.length).toBeGreaterThan(0);
    });

    it('should generate BCrypt hashed passwords', () => {
      const authFile = path.join(seedDataDir, '01_springfood_authentication_seed_data.sql');
      const content = fs.readFileSync(authFile, 'utf-8');
      
      // BCrypt hashes start with $2a$, $2b$, or $2y$
      expect(content).toMatch(/\$2[aby]\$\d{2}\$/);
      
      // Should NOT contain plain text password
      expect(content).not.toContain('Password123!');
    });
  });

  describe('Execution Order Verification', () => {
    it('should order schemas correctly in master script', () => {
      const masterScriptPath = path.join(seedDataDir, 'run_all_seeds.sql');
      const content = fs.readFileSync(masterScriptPath, 'utf-8');
      
      // Find positions of each schema file
      const authPos = content.indexOf('01_springfood_authentication');
      const shopPos = content.indexOf('02_springfood_shop');
      const productPos = content.indexOf('03_springfood_product');
      const orderPos = content.indexOf('04_springfood_order');
      
      // Verify order: authentication → shop → product → order
      expect(authPos).toBeLessThan(shopPos);
      expect(shopPos).toBeLessThan(productPos);
      expect(productPos).toBeLessThan(orderPos);
    });
  });
});
