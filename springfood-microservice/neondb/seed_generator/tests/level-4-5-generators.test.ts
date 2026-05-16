/**
 * Unit Tests for Level 4-5 Generators
 * 
 * Tests for:
 * - Category generator (hierarchy, count, validation)
 * - Product generator (prices, shop references, distribution)
 * - Product categories junction table generator
 * - Sales generator (status distribution, discount ranges, date ranges)
 * 
 * Requirements: 2.3, 11.8, 12.3, 2.7
 */

import { IDRegistry } from '../src/utils/id-registry';
import { generateShops } from '../src/generators/shop-generator';
import { 
  generateCategories, 
  validateCategoryCount, 
  validateCategoryActive, 
  validateCategoryHierarchy,
  getRootCategories,
  getChildCategories,
  validateParentReferences
} from '../src/generators/category-generator';
import { 
  generateProducts, 
  validateProductCount, 
  validateProductPrices, 
  validateProductShopReferences,
  validateMinimumProductsPerShop,
  validateProductStatusDistribution,
  getProductCountByShop
} from '../src/generators/product-generator';
import { 
  generateProductCategories,
  validateProductReferences,
  validateCategoryReferences,
  validateProductCategoryCount
} from '../src/generators/product-categories-generator';
import { 
  generateSales,
  validateSalesCount,
  validateSalesStatusDistribution,
  validateSalesDiscountRanges,
  validateSalesDateRanges,
  getSalesByStatus
} from '../src/generators/sales-generator';

describe('Level 4-5 Generators', () => {
  let registry: IDRegistry;

  beforeEach(() => {
    registry = new IDRegistry();
  });

  describe('Category Generator', () => {
    it('should generate at least 50 categories', () => {
      const categories = generateCategories(registry);
      
      expect(validateCategoryCount(categories)).toBe(true);
      expect(categories.length).toBeGreaterThanOrEqual(50);
    });

    it('should have all categories active', () => {
      const categories = generateCategories(registry);
      
      expect(validateCategoryActive(categories)).toBe(true);
      expect(categories.every(c => c.is_active === true)).toBe(true);
    });

    it('should have category hierarchy with max depth of 2 levels', () => {
      const categories = generateCategories(registry);
      
      expect(validateCategoryHierarchy(categories)).toBe(true);
      
      // Check that no child category has children
      const childCategories = getChildCategories(categories);
      for (const child of childCategories) {
        const hasChildren = categories.some(c => c.parent_id === child.category_name);
        expect(hasChildren).toBe(false);
      }
    });

    it('should have at least 10 root categories', () => {
      const categories = generateCategories(registry);
      const rootCategories = getRootCategories(categories);
      
      expect(rootCategories.length).toBeGreaterThanOrEqual(10);
    });

    it('should have at least 40 child categories', () => {
      const categories = generateCategories(registry);
      const childCategories = getChildCategories(categories);
      
      expect(childCategories.length).toBeGreaterThanOrEqual(40);
    });

    it('should have valid parent references', () => {
      const categories = generateCategories(registry);
      
      expect(validateParentReferences(categories)).toBe(true);
    });

    it('should register all categories in registry', () => {
      const categories = generateCategories(registry);
      
      for (const category of categories) {
        expect(registry.exists('categories', category.category_name)).toBe(true);
      }
    });

    it('should have URL-friendly slugs', () => {
      const categories = generateCategories(registry);
      
      for (const category of categories) {
        expect(category.slug).toBeTruthy();
        expect(category.slug).toMatch(/^[a-z0-9-]+$/);
      }
    });

    it('should have category_group_code for all categories', () => {
      const categories = generateCategories(registry);
      
      for (const category of categories) {
        expect(category.category_group_code).toBeTruthy();
        expect(typeof category.category_group_code).toBe('string');
      }
    });
  });

  describe('Product Generator', () => {
    beforeEach(() => {
      // Generate shops first (required for products)
      generateShops(registry);
      // Generate categories (required for product_categories)
      generateCategories(registry);
    });

    it('should generate at least 150 products', () => {
      const products = generateProducts(registry);
      
      expect(validateProductCount(products)).toBe(true);
      expect(products.length).toBeGreaterThanOrEqual(150);
    });

    it('should have products with realistic prices within ranges', () => {
      const products = generateProducts(registry);
      
      expect(validateProductPrices(products)).toBe(true);
      
      // Check that all prices are between 10k-500k VND
      for (const product of products) {
        expect(product.price).toBeGreaterThanOrEqual(10000);
        expect(product.price).toBeLessThanOrEqual(500000);
        // Prices should be rounded to nearest 1000 VND
        expect(product.price % 1000).toBe(0);
      }
    });

    it('should have all product shop_id values exist in shops registry', () => {
      const products = generateProducts(registry);
      
      expect(validateProductShopReferences(products, registry)).toBe(true);
    });

    it('should ensure each shop has at least 5 products', () => {
      const products = generateProducts(registry);
      
      expect(validateMinimumProductsPerShop(products, registry)).toBe(true);
      
      // Verify by counting
      const productCountByShop = getProductCountByShop(products);
      const shopIds = registry.getAllIds('shops');
      
      for (const shopId of shopIds) {
        const count = productCountByShop[shopId] || 0;
        expect(count).toBeGreaterThanOrEqual(5);
      }
    });

    it('should have realistic product status distribution', () => {
      const products = generateProducts(registry);
      
      expect(validateProductStatusDistribution(products)).toBe(true);
      
      // Check approximate distribution
      const total = products.length;
      const availableCount = products.filter(p => p.product_status === 'AVAILABLE').length;
      const outOfStockCount = products.filter(p => p.product_status === 'OUT_OF_STOCK').length;
      const discontinuedCount = products.filter(p => p.product_status === 'DISCONTINUED').length;
      
      expect(availableCount / total).toBeGreaterThan(0.70);
      expect(availableCount / total).toBeLessThan(0.90);
      expect(outOfStockCount / total).toBeGreaterThan(0.05);
      expect(discontinuedCount / total).toBeLessThan(0.15);
    });

    it('should have valid SKU format', () => {
      const products = generateProducts(registry);
      
      for (const product of products) {
        expect(product.sku).toBeTruthy();
        expect(product.sku).toMatch(/^[A-Z0-9]+-\d{3}$/);
      }
    });

    it('should have images as JSONB array', () => {
      const products = generateProducts(registry);
      
      for (const product of products) {
        expect(product.images).toBeTruthy();
        
        // Parse JSONB
        const images = JSON.parse(product.images);
        expect(Array.isArray(images)).toBe(true);
        expect(images.length).toBeGreaterThan(0);
      }
    });

    it('should have avg_rate between 0 and 5', () => {
      const products = generateProducts(registry);
      
      for (const product of products) {
        expect(product.avg_rate).toBeGreaterThanOrEqual(0);
        expect(product.avg_rate).toBeLessThanOrEqual(5);
      }
    });

    it('should have quantity 0 for OUT_OF_STOCK and DISCONTINUED products', () => {
      const products = generateProducts(registry);
      
      for (const product of products) {
        if (product.product_status === 'OUT_OF_STOCK' || product.product_status === 'DISCONTINUED') {
          expect(product.quantity).toBe(0);
        }
      }
    });

    it('should register all products in registry', () => {
      const products = generateProducts(registry);
      
      for (const product of products) {
        expect(registry.exists('products', product.product_id)).toBe(true);
      }
    });
  });

  describe('Product Categories Junction Table Generator', () => {
    beforeEach(() => {
      // Generate prerequisites
      generateShops(registry);
      generateCategories(registry);
    });

    it('should generate product_categories records', () => {
      const products = generateProducts(registry);
      const productCategories = generateProductCategories(products, registry);
      
      expect(productCategories.length).toBeGreaterThan(0);
      expect(productCategories.length).toBeGreaterThanOrEqual(products.length); // At least 1 per product
    });

    it('should have all product_id values exist in products registry', () => {
      const products = generateProducts(registry);
      const productCategories = generateProductCategories(products, registry);
      
      expect(validateProductReferences(productCategories, registry)).toBe(true);
    });

    it('should have all category_name values exist in categories registry', () => {
      const products = generateProducts(registry);
      const productCategories = generateProductCategories(products, registry);
      
      expect(validateCategoryReferences(productCategories, registry)).toBe(true);
    });

    it('should ensure each product belongs to 1-3 categories', () => {
      const products = generateProducts(registry);
      const productCategories = generateProductCategories(products, registry);
      
      expect(validateProductCategoryCount(productCategories)).toBe(true);
      
      // Verify by counting
      const productCategoryCounts = new Map<string, number>();
      for (const pc of productCategories) {
        const currentCount = productCategoryCounts.get(pc.product_id) || 0;
        productCategoryCounts.set(pc.product_id, currentCount + 1);
      }
      
      for (const [productId, count] of productCategoryCounts.entries()) {
        expect(count).toBeGreaterThanOrEqual(1);
        expect(count).toBeLessThanOrEqual(3);
      }
    });
  });

  describe('Sales Generator', () => {
    it('should generate 15-20 sales', () => {
      const sales = generateSales(registry);
      
      expect(validateSalesCount(sales)).toBe(true);
      expect(sales.length).toBeGreaterThanOrEqual(15);
      expect(sales.length).toBeLessThanOrEqual(20);
    });

    it('should have correct status distribution (35% active, 25% upcoming, 40% expired)', () => {
      const sales = generateSales(registry);
      
      expect(validateSalesStatusDistribution(sales)).toBe(true);
      
      const { active, upcoming, expired } = getSalesByStatus(sales);
      const total = sales.length;
      
      // Allow 15% tolerance
      expect(active.length / total).toBeGreaterThan(0.20);
      expect(active.length / total).toBeLessThan(0.50);
      expect(upcoming.length / total).toBeGreaterThan(0.10);
      expect(upcoming.length / total).toBeLessThan(0.40);
      expect(expired.length / total).toBeGreaterThan(0.25);
      expect(expired.length / total).toBeLessThan(0.55);
    });

    it('should have discount percentages within valid ranges', () => {
      const sales = generateSales(registry);
      
      expect(validateSalesDiscountRanges(sales)).toBe(true);
    });

    it('should have realistic date ranges for each status', () => {
      const sales = generateSales(registry);
      
      expect(validateSalesDateRanges(sales)).toBe(true);
      
      const now = new Date();
      const { active, upcoming, expired } = getSalesByStatus(sales);
      
      // Validate active sales
      for (const sale of active) {
        expect(sale.start_date.getTime()).toBeLessThanOrEqual(now.getTime());
        expect(sale.end_date.getTime()).toBeGreaterThan(now.getTime());
      }
      
      // Validate upcoming sales
      for (const sale of upcoming) {
        expect(sale.start_date.getTime()).toBeGreaterThan(now.getTime());
      }
      
      // Validate expired sales
      for (const sale of expired) {
        expect(sale.end_date.getTime()).toBeLessThanOrEqual(now.getTime());
      }
    });

    it('should have start_date before end_date', () => {
      const sales = generateSales(registry);
      
      for (const sale of sales) {
        expect(sale.start_date.getTime()).toBeLessThan(sale.end_date.getTime());
      }
    });

    it('should have created_at before start_date', () => {
      const sales = generateSales(registry);
      
      for (const sale of sales) {
        expect(sale.created_at.getTime()).toBeLessThanOrEqual(sale.start_date.getTime());
      }
    });

    it('should register all sales in registry', () => {
      const sales = generateSales(registry);
      
      for (const sale of sales) {
        expect(registry.exists('sales', sale.sale_id)).toBe(true);
      }
    });

    it('should have valid discount percentages (0-100)', () => {
      const sales = generateSales(registry);
      
      for (const sale of sales) {
        expect(sale.discount_percentage).toBeGreaterThanOrEqual(0);
        expect(sale.discount_percentage).toBeLessThanOrEqual(100);
      }
    });

    it('should have non-empty name, description, and conditions', () => {
      const sales = generateSales(registry);
      
      for (const sale of sales) {
        expect(sale.name).toBeTruthy();
        expect(sale.description).toBeTruthy();
        expect(sale.conditions).toBeTruthy();
      }
    });
  });

  describe('Integration Tests', () => {
    it('should generate complete Level 4-5 data pipeline', () => {
      // Generate all Level 4-5 data
      generateShops(registry);
      const categories = generateCategories(registry);
      const products = generateProducts(registry);
      const productCategories = generateProductCategories(products, registry);
      const sales = generateSales(registry);
      
      // Validate counts
      expect(categories.length).toBeGreaterThanOrEqual(50);
      expect(products.length).toBeGreaterThanOrEqual(150);
      expect(productCategories.length).toBeGreaterThanOrEqual(products.length);
      expect(sales.length).toBeGreaterThanOrEqual(15);
      expect(sales.length).toBeLessThanOrEqual(20);
      
      // Validate referential integrity
      expect(validateProductShopReferences(products, registry)).toBe(true);
      expect(validateProductReferences(productCategories, registry)).toBe(true);
      expect(validateCategoryReferences(productCategories, registry)).toBe(true);
      
      // Validate business rules
      expect(validateCategoryHierarchy(categories)).toBe(true);
      expect(validateMinimumProductsPerShop(products, registry)).toBe(true);
      expect(validateProductCategoryCount(productCategories)).toBe(true);
      expect(validateSalesStatusDistribution(sales)).toBe(true);
    });
  });
});
