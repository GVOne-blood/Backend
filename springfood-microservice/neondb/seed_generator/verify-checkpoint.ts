/**
 * Checkpoint Verification Script
 * 
 * Verifies that all templates and utilities are complete and realistic
 * for Task 5: Checkpoint - Verify templates and utilities
 */

import { getAllCategories, validateCategoryHierarchy, ROOT_CATEGORIES, CHILD_CATEGORIES } from './src/templates/categories';
import { PRODUCT_TEMPLATES, PRODUCT_STATS } from './src/templates/products';
import { SHOP_NAMES, SHOP_NAME_STATS } from './src/templates/shops';
import { SALES_CAMPAIGNS, SALES_DISTRIBUTION } from './src/templates/sales';
import { IDRegistry } from './src/utils/id-registry';
import { hashPassword, verifyPassword } from './src/utils/bcrypt-hasher';
import { generateUUID, isValidUUID } from './src/utils/uuid-generator';

console.log('='.repeat(80));
console.log('CHECKPOINT VERIFICATION: Templates and Utilities');
console.log('='.repeat(80));
console.log();

// ============================================
// 1. VERIFY CATEGORY TEMPLATES
// ============================================
console.log('1. CATEGORY TEMPLATES');
console.log('-'.repeat(80));

const allCategories = getAllCategories();
console.log(`✓ Total categories: ${allCategories.length}`);
console.log(`✓ Root categories: ${ROOT_CATEGORIES.length}`);
console.log(`✓ Child categories: ${CHILD_CATEGORIES.length}`);

// Verify hierarchy depth
const hierarchyValid = validateCategoryHierarchy();
console.log(`✓ Category hierarchy valid (max 2 levels): ${hierarchyValid}`);

// Check for realistic Vietnamese names
const sampleCategories = [
  'Món Việt Truyền Thống',
  'Đồ Uống',
  'Phở',
  'Bún',
  'Cà Phê',
  'Trà Sữa'
];
const allCategoryNames = allCategories.map(c => c.category_name);
const hasRealisticNames = sampleCategories.every(name => allCategoryNames.includes(name));
console.log(`✓ Contains realistic Vietnamese category names: ${hasRealisticNames}`);

console.log();

// ============================================
// 2. VERIFY PRODUCT TEMPLATES
// ============================================
console.log('2. PRODUCT TEMPLATES');
console.log('-'.repeat(80));

console.log(`✓ Total products: ${PRODUCT_STATS.totalProducts}`);
console.log(`✓ Product categories: ${PRODUCT_STATS.categories}`);

// Verify minimum product count (150+)
const meetsMinimum = PRODUCT_STATS.totalProducts >= 150;
console.log(`✓ Meets minimum 150 products: ${meetsMinimum} (${PRODUCT_STATS.totalProducts})`);

// Check distribution (5-12 products per category)
console.log('\nProduct distribution by category:');
const productCounts = PRODUCT_STATS.productsByCategory;
let allWithinRange = true;
for (const [category, count] of Object.entries(productCounts)) {
  const inRange = count >= 5 && count <= 12;
  if (!inRange) allWithinRange = false;
  console.log(`  ${category}: ${count} products ${inRange ? '✓' : '✗ (out of 5-12 range)'}`);
}
console.log(`✓ All categories have 5-12 products: ${allWithinRange}`);

// Check for realistic Vietnamese food names
const sampleProducts = [
  'Phở Bò Tái',
  'Bún Chả',
  'Cơm Tấm Sườn',
  'Cà Phê Sữa Đá',
  'Trà Sữa Trân Châu Đường Đen'
];
const allProductNames = PRODUCT_TEMPLATES.map(p => p.name);
const hasRealisticProducts = sampleProducts.every(name => allProductNames.includes(name));
console.log(`✓ Contains realistic Vietnamese food names: ${hasRealisticProducts}`);

console.log();

// ============================================
// 3. VERIFY SHOP TEMPLATES
// ============================================
console.log('3. SHOP TEMPLATES');
console.log('-'.repeat(80));

console.log(`✓ Total shops: ${SHOP_NAME_STATS.total}`);
console.log(`✓ Brand shops: ${SHOP_NAME_STATS.brands}`);
console.log(`✓ Traditional shops: ${SHOP_NAME_STATS.traditional}`);
console.log(`✓ Shop categories: ${SHOP_NAME_STATS.categories}`);

// Verify minimum shop count (25+)
const meetsShopMinimum = SHOP_NAME_STATS.total >= 25;
console.log(`✓ Meets minimum 25 shops: ${meetsShopMinimum} (${SHOP_NAME_STATS.total})`);

// Check for real brands
const realBrands = ['Gong Cha', 'The Coffee House', 'Highlands Coffee', 'Phúc Long Coffee & Tea'];
const allShopNames = SHOP_NAMES.map(s => s.name);
const hasRealBrands = realBrands.every(brand => allShopNames.includes(brand));
console.log(`✓ Contains real Vietnamese brands: ${hasRealBrands}`);

// Check for traditional shops
const traditionalShops = ['Phở Hà Nội 24h', 'Bún Chả Hà Nội', 'Cơm Tấm Sài Gòn'];
const hasTraditionalShops = traditionalShops.every(shop => allShopNames.includes(shop));
console.log(`✓ Contains traditional Vietnamese shops: ${hasTraditionalShops}`);

console.log();

// ============================================
// 4. VERIFY SALES TEMPLATES
// ============================================
console.log('4. SALES TEMPLATES');
console.log('-'.repeat(80));

console.log(`✓ Total sales campaigns: ${SALES_CAMPAIGNS.length}`);

// Verify minimum sales count (15-20)
const meetsSalesMinimum = SALES_CAMPAIGNS.length >= 15 && SALES_CAMPAIGNS.length <= 20;
console.log(`✓ Meets 15-20 sales campaigns: ${meetsSalesMinimum} (${SALES_CAMPAIGNS.length})`);

// Check for different sale categories
const saleCategories = new Set(SALES_CAMPAIGNS.map(s => s.category));
console.log(`✓ Sale categories: ${saleCategories.size}`);
console.log(`  Categories: ${Array.from(saleCategories).join(', ')}`);

// Check for realistic Vietnamese promotion names
const sampleSales = [
  'Flash Sale Cuối Tuần',
  'Khuyến Mãi Mùa Hè',
  'Tuần Lễ Trà Sữa',
  'Ưu Đãi Bữa Sáng'
];
const allSaleNames = SALES_CAMPAIGNS.map(s => s.name);
const hasRealisticSales = sampleSales.every(name => allSaleNames.includes(name));
console.log(`✓ Contains realistic Vietnamese promotion names: ${hasRealisticSales}`);

// Verify discount ranges
let allDiscountsValid = true;
for (const sale of SALES_CAMPAIGNS) {
  const range = SALES_DISTRIBUTION.discountRanges[sale.category];
  const valid = sale.discount_percentage >= range.min && sale.discount_percentage <= range.max;
  if (!valid) {
    console.log(`✗ Invalid discount for ${sale.name}: ${sale.discount_percentage}% (expected ${range.min}-${range.max}%)`);
    allDiscountsValid = false;
  }
}
console.log(`✓ All discounts within valid ranges: ${allDiscountsValid}`);

console.log();

// ============================================
// 5. VERIFY ID REGISTRY
// ============================================
console.log('5. ID REGISTRY');
console.log('-'.repeat(80));

const registry = new IDRegistry();

// Test registration
const testUserId = generateUUID();
registry.register('users', testUserId, { name: 'Test User' });
console.log(`✓ ID registration works: ${registry.exists('users', testUserId)}`);

// Test retrieval
const retrievedId = registry.getRandomId('users');
console.log(`✓ Random ID retrieval works: ${retrievedId === testUserId}`);

// Test multiple registrations
for (let i = 0; i < 10; i++) {
  registry.register('products', generateUUID(), { name: `Product ${i}` });
}
console.log(`✓ Multiple registrations work: ${registry.getCount('products') === 10}`);

// Test cross-table independence
registry.register('shops', generateUUID(), { name: 'Shop 1' });
console.log(`✓ Cross-table independence: users=${registry.getCount('users')}, products=${registry.getCount('products')}, shops=${registry.getCount('shops')}`);

console.log();

// ============================================
// 6. VERIFY BCRYPT HASHING
// ============================================
console.log('6. BCRYPT PASSWORD HASHING');
console.log('-'.repeat(80));

const plainPassword = 'Password123!';
const hashedPassword = hashPassword(plainPassword);

// Check BCrypt format
const bcryptFormat = /^\$2[aby]\$\d{2}\$.{53}$/;
const validFormat = bcryptFormat.test(hashedPassword);
console.log(`✓ Produces valid BCrypt format: ${validFormat}`);
console.log(`  Hash: ${hashedPassword.substring(0, 20)}...`);

// Check hash length
console.log(`✓ Hash length is 60 characters: ${hashedPassword.length === 60}`);

// Check verification
const verificationWorks = verifyPassword(plainPassword, hashedPassword);
console.log(`✓ Password verification works: ${verificationWorks}`);

// Check wrong password rejection
const wrongPasswordRejected = !verifyPassword('WrongPassword', hashedPassword);
console.log(`✓ Wrong password rejected: ${wrongPasswordRejected}`);

// Check uniqueness (different salts)
const hash1 = hashPassword(plainPassword);
const hash2 = hashPassword(plainPassword);
const uniqueHashes = hash1 !== hash2;
console.log(`✓ Different hashes for same password (salt): ${uniqueHashes}`);

console.log();

// ============================================
// 7. VERIFY UUID GENERATION
// ============================================
console.log('7. UUID GENERATION');
console.log('-'.repeat(80));

const uuid = generateUUID();
console.log(`✓ Generates UUID: ${uuid}`);

// Check UUID v4 format
const validUUID = isValidUUID(uuid);
console.log(`✓ Valid UUID v4 format: ${validUUID}`);

// Check version (4)
const versionChar = uuid.charAt(14);
console.log(`✓ Correct version (4): ${versionChar === '4'}`);

// Check variant
const variantChar = uuid.charAt(19).toLowerCase();
const validVariant = ['8', '9', 'a', 'b'].includes(variantChar);
console.log(`✓ Correct variant (8/9/a/b): ${validVariant}`);

// Check uniqueness
const uuids = new Set<string>();
for (let i = 0; i < 100; i++) {
  uuids.add(generateUUID());
}
console.log(`✓ Generates unique UUIDs: ${uuids.size === 100}`);

console.log();

// ============================================
// SUMMARY
// ============================================
console.log('='.repeat(80));
console.log('CHECKPOINT VERIFICATION SUMMARY');
console.log('='.repeat(80));
console.log();

const checks = [
  { name: 'Category templates complete', passed: allCategories.length >= 50 },
  { name: 'Category hierarchy valid', passed: hierarchyValid },
  { name: 'Product templates complete', passed: PRODUCT_STATS.totalProducts >= 150 },
  { name: 'Product distribution valid', passed: allWithinRange },
  { name: 'Shop templates complete', passed: SHOP_NAME_STATS.total >= 25 },
  { name: 'Sales templates complete', passed: SALES_CAMPAIGNS.length >= 15 },
  { name: 'Sales discounts valid', passed: allDiscountsValid },
  { name: 'ID Registry works', passed: registry.getCount('users') > 0 },
  { name: 'BCrypt hashing works', passed: validFormat && verificationWorks },
  { name: 'UUID generation works', passed: validUUID && uuids.size === 100 }
];

let allPassed = true;
for (const check of checks) {
  const status = check.passed ? '✓ PASS' : '✗ FAIL';
  console.log(`${status}: ${check.name}`);
  if (!check.passed) allPassed = false;
}

console.log();
if (allPassed) {
  console.log('✓ ALL CHECKS PASSED - Ready to proceed to next tasks');
} else {
  console.log('✗ SOME CHECKS FAILED - Review and fix issues before proceeding');
}
console.log();
