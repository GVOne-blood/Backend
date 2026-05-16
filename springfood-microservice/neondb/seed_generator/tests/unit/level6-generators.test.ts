/**
 * Unit Tests for Level 6 Generators
 * 
 * Tests for:
 * - product_sales junction table generator
 * - order generator
 * - order_items generator
 * - order totals updater
 * 
 * Requirements: 2.7, 13.8, 13.2, 13.5
 */

import { describe, it, expect, beforeEach } from '@jest/globals';
import { IDRegistry } from '../../src/utils/id-registry';
import { generateUUID } from '../../src/utils/uuid-generator';

// Import generators
import { generateSales, Sale } from '../../src/generators/sales-generator';
import { generateProducts, Product } from '../../src/generators/product-generator';
import { 
  generateProductSales, 
  validateProductSalesCount,
  validateCategorySpecificSales,
  validateProductSalesForeignKeys,
  getProductSalesStats
} from '../../src/generators/product-sales-generator';
import {
  generateOrders,
  validateOrderCount,
  validateOrderCustomers,
  validateShippingFees,
  validateOrderTotals,
  Order
} from '../../src/generators/order-generator';
import {
  generateOrderItems,
  validateMinimumItemsPerOrder,
  validateOrderItemsShopConsistency,
  validateOrderItemsProductData,
  validateOrderItemsPrices,
  validateOrderItemsSubtotals,
  OrderItem
} from '../../src/generators/order-items-generator';
import {
  updateAllOrderTotals,
  validateAllOrderTotals,
  validateShippingFeeRange,
  validateDiscountAmounts
} from '../../src/generators/order-totals-updater';

// Import user generator for CUSTOMER users
import { generateUsers, User } from '../../src/generators/user-generator';

// Import shop generator
import { generateShops } from '../../src/generators/shop-generator';

describe('Level 6 Generators', () => {
  let registry: IDRegistry;

  beforeEach(() => {
    registry = new IDRegistry();
  });

  describe('Product-Sales Generator', () => {
    it('should generate product_sales records linking products to sales', () => {
      // Setup: Generate sales and products
      const sales = generateSales(registry, 15);
      
      // Mock products with categories
      const mockProducts: Product[] = [];
      for (let i = 0; i < 50; i++) {
        const productId = generateUUID();
        const categories = ['Phở', 'Trà Sữa', 'Cà Phê', 'Cơm', 'Bún'];
        const category = categories[i % categories.length];
        
        const product: Product = {
          product_id: productId,
          name: `Product ${i}`,
          description: `Description ${i}`,
          price: 50000,
          shop_id: generateUUID(),
          product_status: 'AVAILABLE',
          sku: `SKU-${i}`,
          quantity: 100,
          images: JSON.stringify(['image1.jpg']),
          avg_rate: 4.5,
          created_at: new Date(),
          updated_at: new Date(),
          category: category
        };
        
        mockProducts.push(product);
        registry.register('products', productId, product);
      }
      
      // Generate product_sales
      const productSales = generateProductSales(registry);
      
      // Assertions
      expect(productSales.length).toBeGreaterThan(0);
      expect(productSales.every(ps => ps.product_id && ps.sale_id)).toBe(true);
    });

    it('should assign appropriate number of products per sale based on category', () => {
      // Setup
      const sales = generateSales(registry, 15);
      
      const mockProducts: Product[] = [];
      for (let i = 0; i < 100; i++) {
        const productId = generateUUID();
        const product: Product = {
          product_id: productId,
          name: `Product ${i}`,
          description: `Description ${i}`,
          price: 50000,
          shop_id: generateUUID(),
          product_status: 'AVAILABLE',
          sku: `SKU-${i}`,
          quantity: 100,
          images: JSON.stringify(['image1.jpg']),
          avg_rate: 4.5,
          created_at: new Date(),
          updated_at: new Date(),
          category: 'General'
        };
        
        mockProducts.push(product);
        registry.register('products', productId, product);
      }
      
      const productSales = generateProductSales(registry);
      
      // Validate product counts per sale
      const isValid = validateProductSalesCount(productSales, registry);
      expect(isValid).toBe(true);
    });

    it('should filter products by category for category_specific sales', () => {
      // Setup: Create sales with category_specific type
      const sales = generateSales(registry, 15);
      
      // Create products with specific categories
      const mockProducts: Product[] = [];
      const categories = ['Trà Sữa', 'Phở', 'Cà Phê', 'Cơm'];
      
      for (let i = 0; i < 40; i++) {
        const productId = generateUUID();
        const category = categories[i % categories.length];
        
        const product: Product = {
          product_id: productId,
          name: `${category} Product ${i}`,
          description: `${category} Description`,
          price: 50000,
          shop_id: generateUUID(),
          product_status: 'AVAILABLE',
          sku: `SKU-${i}`,
          quantity: 100,
          images: JSON.stringify(['image1.jpg']),
          avg_rate: 4.5,
          created_at: new Date(),
          updated_at: new Date(),
          category: category
        };
        
        mockProducts.push(product);
        registry.register('products', productId, product);
      }
      
      const productSales = generateProductSales(registry);
      
      // Validate category-specific sales
      const isValid = validateCategorySpecificSales(productSales, registry);
      expect(isValid).toBe(true);
    });

    it('should ensure all foreign keys exist in registries', () => {
      // Setup
      const sales = generateSales(registry, 15);
      
      const mockProducts: Product[] = [];
      for (let i = 0; i < 50; i++) {
        const productId = generateUUID();
        const product: Product = {
          product_id: productId,
          name: `Product ${i}`,
          description: `Description ${i}`,
          price: 50000,
          shop_id: generateUUID(),
          product_status: 'AVAILABLE',
          sku: `SKU-${i}`,
          quantity: 100,
          images: JSON.stringify(['image1.jpg']),
          avg_rate: 4.5,
          created_at: new Date(),
          updated_at: new Date(),
          category: 'General'
        };
        
        mockProducts.push(product);
        registry.register('products', productId, product);
      }
      
      const productSales = generateProductSales(registry);
      
      // Validate foreign keys
      const isValid = validateProductSalesForeignKeys(productSales, registry);
      expect(isValid).toBe(true);
    });

    it('should provide product_sales statistics', () => {
      // Setup
      const sales = generateSales(registry, 15);
      
      const mockProducts: Product[] = [];
      for (let i = 0; i < 50; i++) {
        const productId = generateUUID();
        const product: Product = {
          product_id: productId,
          name: `Product ${i}`,
          description: `Description ${i}`,
          price: 50000,
          shop_id: generateUUID(),
          product_status: 'AVAILABLE',
          sku: `SKU-${i}`,
          quantity: 100,
          images: JSON.stringify(['image1.jpg']),
          avg_rate: 4.5,
          created_at: new Date(),
          updated_at: new Date(),
          category: 'General'
        };
        
        mockProducts.push(product);
        registry.register('products', productId, product);
      }
      
      const productSales = generateProductSales(registry);
      const stats = getProductSalesStats(productSales, registry);
      
      // Assertions
      expect(stats.total).toBe(productSales.length);
      expect(stats.salesWithProducts).toBeGreaterThan(0);
      expect(stats.avgProductsPerSale).toBeGreaterThan(0);
    });
  });

  describe('Order Generator', () => {
    it('should generate at least 10 orders', () => {
      // Setup: Generate users and shops
      const users = generateUsers(registry);
      const shops = generateShops(registry, 10);
      
      // Generate orders
      const orders = generateOrders(registry, 10);
      
      // Assertions
      expect(orders.length).toBeGreaterThanOrEqual(10);
      expect(validateOrderCount(orders)).toBe(true);
    });

    it('should assign all orders to users with CUSTOMER role', () => {
      // Setup
      const users = generateUsers(registry);
      const shops = generateShops(registry, 10);
      
      const orders = generateOrders(registry, 10);
      
      // Validate
      const isValid = validateOrderCustomers(orders, registry);
      expect(isValid).toBe(true);
    });

    it('should generate shipping fees within 15,000 - 50,000 VND range', () => {
      // Setup
      const users = generateUsers(registry);
      const shops = generateShops(registry, 10);
      
      const orders = generateOrders(registry, 10);
      
      // Validate
      const isValid = validateShippingFees(orders);
      expect(isValid).toBe(true);
    });

    it('should set realistic order_status distribution', () => {
      // Setup
      const users = generateUsers(registry);
      const shops = generateShops(registry, 10);
      
      const orders = generateOrders(registry, 50); // Generate more for better distribution
      
      // Count statuses
      const statusCounts: Record<string, number> = {};
      for (const order of orders) {
        statusCounts[order.order_status] = (statusCounts[order.order_status] || 0) + 1;
      }
      
      // Assertions - at least some variety in statuses
      expect(Object.keys(statusCounts).length).toBeGreaterThan(1);
      expect(statusCounts['DELIVERED']).toBeGreaterThan(0); // Most common status
    });

    it('should set realistic payment_status distribution', () => {
      // Setup
      const users = generateUsers(registry);
      const shops = generateShops(registry, 10);
      
      const orders = generateOrders(registry, 50);
      
      // Count payment statuses
      const paymentCounts: Record<string, number> = {};
      for (const order of orders) {
        paymentCounts[order.payment_status] = (paymentCounts[order.payment_status] || 0) + 1;
      }
      
      // Assertions
      expect(Object.keys(paymentCounts).length).toBeGreaterThan(0);
      expect(paymentCounts['PAID']).toBeGreaterThan(0); // Most common payment status
    });

    it('should generate Vietnamese shipping addresses', () => {
      // Setup
      const users = generateUsers(registry);
      const shops = generateShops(registry, 10);
      
      const orders = generateOrders(registry, 10);
      
      // Assertions
      for (const order of orders) {
        expect(order.shipping_address_street).toBeTruthy();
        expect(order.shipping_address_ward).toBeTruthy();
        expect(order.shipping_address_district).toBeTruthy();
        expect(order.shipping_address_city).toBeTruthy();
        expect(order.shipping_address_province).toBeTruthy();
        expect(order.receiver_name).toBeTruthy();
        expect(order.receiver_phone).toBeTruthy();
      }
    });
  });

  describe('Order Items Generator', () => {
    it('should generate at least 2 order items for each order', () => {
      // Setup
      const users = generateUsers(registry);
      const shops = generateShops(registry, 10);
      const orders = generateOrders(registry, 10);
      
      // Generate products for shops
      const mockProducts: Product[] = [];
      const shopIds = registry.getAllIds('shops');
      
      for (let i = 0; i < 50; i++) {
        const productId = generateUUID();
        const shopId = shopIds[i % shopIds.length];
        
        const product: Product = {
          product_id: productId,
          name: `Product ${i}`,
          description: `Description ${i}`,
          price: 50000,
          shop_id: shopId,
          product_status: 'AVAILABLE',
          sku: `SKU-${i}`,
          quantity: 100,
          images: JSON.stringify(['image1.jpg']),
          avg_rate: 4.5,
          created_at: new Date(),
          updated_at: new Date(),
          category: 'General'
        };
        
        mockProducts.push(product);
        registry.register('products', productId, product);
      }
      
      const orderItems = generateOrderItems(registry);
      
      // Validate
      const isValid = validateMinimumItemsPerOrder(orderItems, registry);
      expect(isValid).toBe(true);
    });

    it('should ensure order items reference products from same shop as order', () => {
      // Setup
      const users = generateUsers(registry);
      const shops = generateShops(registry, 10);
      const orders = generateOrders(registry, 10);
      
      const mockProducts: Product[] = [];
      const shopIds = registry.getAllIds('shops');
      
      for (let i = 0; i < 50; i++) {
        const productId = generateUUID();
        const shopId = shopIds[i % shopIds.length];
        
        const product: Product = {
          product_id: productId,
          name: `Product ${i}`,
          description: `Description ${i}`,
          price: 50000,
          shop_id: shopId,
          product_status: 'AVAILABLE',
          sku: `SKU-${i}`,
          quantity: 100,
          images: JSON.stringify(['image1.jpg']),
          avg_rate: 4.5,
          created_at: new Date(),
          updated_at: new Date(),
          category: 'General'
        };
        
        mockProducts.push(product);
        registry.register('products', productId, product);
      }
      
      const orderItems = generateOrderItems(registry);
      
      // Validate
      const isValid = validateOrderItemsShopConsistency(orderItems, registry);
      expect(isValid).toBe(true);
    });

    it('should populate product_name and product_image_url from referenced product', () => {
      // Setup
      const users = generateUsers(registry);
      const shops = generateShops(registry, 10);
      const orders = generateOrders(registry, 10);
      
      const mockProducts: Product[] = [];
      const shopIds = registry.getAllIds('shops');
      
      for (let i = 0; i < 50; i++) {
        const productId = generateUUID();
        const shopId = shopIds[i % shopIds.length];
        
        const product: Product = {
          product_id: productId,
          name: `Product ${i}`,
          description: `Description ${i}`,
          price: 50000,
          shop_id: shopId,
          product_status: 'AVAILABLE',
          sku: `SKU-${i}`,
          quantity: 100,
          images: JSON.stringify(['https://example.com/image1.jpg']),
          avg_rate: 4.5,
          created_at: new Date(),
          updated_at: new Date(),
          category: 'General'
        };
        
        mockProducts.push(product);
        registry.register('products', productId, product);
      }
      
      const orderItems = generateOrderItems(registry);
      
      // Validate
      const isValid = validateOrderItemsProductData(orderItems, registry);
      expect(isValid).toBe(true);
    });

    it('should set price_at_booking to match product price', () => {
      // Setup
      const users = generateUsers(registry);
      const shops = generateShops(registry, 10);
      const orders = generateOrders(registry, 10);
      
      const mockProducts: Product[] = [];
      const shopIds = registry.getAllIds('shops');
      
      for (let i = 0; i < 50; i++) {
        const productId = generateUUID();
        const shopId = shopIds[i % shopIds.length];
        
        const product: Product = {
          product_id: productId,
          name: `Product ${i}`,
          description: `Description ${i}`,
          price: 50000 + i * 1000, // Varying prices
          shop_id: shopId,
          product_status: 'AVAILABLE',
          sku: `SKU-${i}`,
          quantity: 100,
          images: JSON.stringify(['image1.jpg']),
          avg_rate: 4.5,
          created_at: new Date(),
          updated_at: new Date(),
          category: 'General'
        };
        
        mockProducts.push(product);
        registry.register('products', productId, product);
      }
      
      const orderItems = generateOrderItems(registry);
      
      // Validate
      const isValid = validateOrderItemsPrices(orderItems, registry);
      expect(isValid).toBe(true);
    });

    it('should calculate subtotals correctly (quantity * price_at_booking)', () => {
      // Setup
      const users = generateUsers(registry);
      const shops = generateShops(registry, 10);
      const orders = generateOrders(registry, 10);
      
      const mockProducts: Product[] = [];
      const shopIds = registry.getAllIds('shops');
      
      for (let i = 0; i < 50; i++) {
        const productId = generateUUID();
        const shopId = shopIds[i % shopIds.length];
        
        const product: Product = {
          product_id: productId,
          name: `Product ${i}`,
          description: `Description ${i}`,
          price: 50000,
          shop_id: shopId,
          product_status: 'AVAILABLE',
          sku: `SKU-${i}`,
          quantity: 100,
          images: JSON.stringify(['image1.jpg']),
          avg_rate: 4.5,
          created_at: new Date(),
          updated_at: new Date(),
          category: 'General'
        };
        
        mockProducts.push(product);
        registry.register('products', productId, product);
      }
      
      const orderItems = generateOrderItems(registry);
      
      // Validate
      const isValid = validateOrderItemsSubtotals(orderItems);
      expect(isValid).toBe(true);
    });
  });

  describe('Order Totals Updater', () => {
    it('should calculate order totals correctly', () => {
      // Setup
      const users = generateUsers(registry);
      const shops = generateShops(registry, 10);
      const orders = generateOrders(registry, 10);
      
      const mockProducts: Product[] = [];
      const shopIds = registry.getAllIds('shops');
      
      for (let i = 0; i < 50; i++) {
        const productId = generateUUID();
        const shopId = shopIds[i % shopIds.length];
        
        const product: Product = {
          product_id: productId,
          name: `Product ${i}`,
          description: `Description ${i}`,
          price: 50000,
          shop_id: shopId,
          product_status: 'AVAILABLE',
          sku: `SKU-${i}`,
          quantity: 100,
          images: JSON.stringify(['image1.jpg']),
          avg_rate: 4.5,
          created_at: new Date(),
          updated_at: new Date(),
          category: 'General'
        };
        
        mockProducts.push(product);
        registry.register('products', productId, product);
      }
      
      const orderItems = generateOrderItems(registry);
      
      // Update order totals
      updateAllOrderTotals(orders, orderItems);
      
      // Validate
      const isValid = validateAllOrderTotals(orders, orderItems);
      expect(isValid).toBe(true);
    });

    it('should ensure final_price = subtotal_amount + shipping_fee - discount_amount', () => {
      // Setup
      const users = generateUsers(registry);
      const shops = generateShops(registry, 10);
      const orders = generateOrders(registry, 10);
      
      const mockProducts: Product[] = [];
      const shopIds = registry.getAllIds('shops');
      
      for (let i = 0; i < 50; i++) {
        const productId = generateUUID();
        const shopId = shopIds[i % shopIds.length];
        
        const product: Product = {
          product_id: productId,
          name: `Product ${i}`,
          description: `Description ${i}`,
          price: 50000,
          shop_id: shopId,
          product_status: 'AVAILABLE',
          sku: `SKU-${i}`,
          quantity: 100,
          images: JSON.stringify(['image1.jpg']),
          avg_rate: 4.5,
          created_at: new Date(),
          updated_at: new Date(),
          category: 'General'
        };
        
        mockProducts.push(product);
        registry.register('products', productId, product);
      }
      
      const orderItems = generateOrderItems(registry);
      updateAllOrderTotals(orders, orderItems);
      
      // Validate formula
      for (const order of orders) {
        const expectedFinalPrice = Math.max(0, order.subtotal_amount + order.shipping_fee - order.discount_amount);
        expect(Math.abs(order.final_price - expectedFinalPrice)).toBeLessThan(0.01);
      }
    });

    it('should validate shipping fee range', () => {
      // Setup
      const users = generateUsers(registry);
      const shops = generateShops(registry, 10);
      const orders = generateOrders(registry, 10);
      
      // Validate
      const isValid = validateShippingFeeRange(orders);
      expect(isValid).toBe(true);
    });

    it('should validate discount amounts are reasonable', () => {
      // Setup
      const users = generateUsers(registry);
      const shops = generateShops(registry, 10);
      const orders = generateOrders(registry, 10);
      
      const mockProducts: Product[] = [];
      const shopIds = registry.getAllIds('shops');
      
      for (let i = 0; i < 50; i++) {
        const productId = generateUUID();
        const shopId = shopIds[i % shopIds.length];
        
        const product: Product = {
          product_id: productId,
          name: `Product ${i}`,
          description: `Description ${i}`,
          price: 50000,
          shop_id: shopId,
          product_status: 'AVAILABLE',
          sku: `SKU-${i}`,
          quantity: 100,
          images: JSON.stringify(['image1.jpg']),
          avg_rate: 4.5,
          created_at: new Date(),
          updated_at: new Date(),
          category: 'General'
        };
        
        mockProducts.push(product);
        registry.register('products', productId, product);
      }
      
      const orderItems = generateOrderItems(registry);
      updateAllOrderTotals(orders, orderItems);
      
      // Validate
      const isValid = validateDiscountAmounts(orders);
      expect(isValid).toBe(true);
    });
  });
});
