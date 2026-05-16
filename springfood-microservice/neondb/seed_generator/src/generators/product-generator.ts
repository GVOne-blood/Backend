/**
 * Product Generator Module
 * 
 * Generates at least 150 products using product name templates.
 * Distributes products across categories (5-12 products per category) and shops.
 * 
 * Requirements: 2.1, 2.2, 2.3, 2.6, 3.1, 4.3, 4.6, 12.1, 12.2, 12.3, 12.5, 12.6
 */

import { IDRegistry } from '../utils/id-registry';
import { generateUUID } from '../utils/uuid-generator';
import { PRODUCT_TEMPLATES, ProductTemplate } from '../templates/products';
import { generateRealisticPrice } from '../templates/prices';

export interface Product {
  product_id: string;
  name: string;
  description: string;
  price: number;
  wholesale_price: number; // Giá bán sỉ (thường thấp hơn giá bán lẻ 10-30%)
  shop_id: string;
  product_status: 'AVAILABLE' | 'OUT_OF_STOCK' | 'DISCONTINUED';
  sku: string;
  quantity: number;
  images: string; // JSONB array of image URLs
  avg_rate: number; // Legacy field - kept for backward compatibility
  average_rating: number; // Đánh giá trung bình (0.0 - 5.0)
  total_feedbacks: number; // Tổng số feedback/đánh giá
  created_at: Date;
  updated_at: Date;
  category: string; // TEMPORARY FIELD: Used for category assignment logic, must be removed before SQL generation
}

/**
 * Product status distribution
 * - 80% AVAILABLE
 * - 15% OUT_OF_STOCK
 * - 5% DISCONTINUED
 */
const PRODUCT_STATUS_DISTRIBUTION = {
  AVAILABLE: 0.80,
  OUT_OF_STOCK: 0.15,
  DISCONTINUED: 0.05
};

/**
 * Generate product status based on distribution
 */
function generateProductStatus(): 'AVAILABLE' | 'OUT_OF_STOCK' | 'DISCONTINUED' {
  const random = Math.random();
  
  if (random < PRODUCT_STATUS_DISTRIBUTION.AVAILABLE) {
    return 'AVAILABLE';
  } else if (random < PRODUCT_STATUS_DISTRIBUTION.AVAILABLE + PRODUCT_STATUS_DISTRIBUTION.OUT_OF_STOCK) {
    return 'OUT_OF_STOCK';
  } else {
    return 'DISCONTINUED';
  }
}

/**
 * Generate SKU for product
 * Format: {CATEGORY_PREFIX}-{NUMBER}
 * 
 * @example
 * generateSKU('Phở', 1) // Returns: PHO-001
 * generateSKU('Trà Sữa', 5) // Returns: TRASUA-005
 */
function generateSKU(category: string, index: number): string {
  // Convert category to SKU prefix
  const prefix = category
    .toUpperCase()
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '') // Remove diacritics
    .replace(/Đ/g, 'D')
    .replace(/đ/g, 'd')
    .replace(/[^A-Z0-9]/g, '')
    .substring(0, 10);
  
  const number = String(index).padStart(3, '0');
  return `${prefix}-${number}`;
}

/**
 * Generate product images as JSONB array
 * Returns a JSON string representing an array of image URLs
 */
function generateProductImages(productName: string, count: number = 3): string {
  const images: string[] = [];
  
  for (let i = 0; i < count; i++) {
    // Use placeholder image service with product name
    const imageUrl = `https://via.placeholder.com/400x300/FF6B6B/FFFFFF?text=${encodeURIComponent(productName)}`;
    images.push(imageUrl);
  }
  
  return JSON.stringify(images);
}

/**
 * Generate quantity based on product status
 */
function generateQuantity(status: 'AVAILABLE' | 'OUT_OF_STOCK' | 'DISCONTINUED'): number {
  if (status === 'OUT_OF_STOCK') {
    return 0;
  } else if (status === 'DISCONTINUED') {
    return 0;
  } else {
    // AVAILABLE: 10-100 items
    return Math.floor(Math.random() * 91) + 10;
  }
}

/**
 * Generate average rating (0-5 stars)
 */
function generateAvgRate(): number {
  // Most products have 3.5-5.0 stars
  const rating = Math.random() * 1.5 + 3.5;
  return parseFloat(rating.toFixed(2));
}

/**
 * Generate wholesale price (10-30% lower than retail price)
 */
function generateWholesalePrice(retailPrice: number): number {
  // Wholesale price is typically 70-90% of retail price (10-30% discount)
  const discountPercent = Math.random() * 0.20 + 0.70; // 0.70 to 0.90
  return parseFloat((retailPrice * discountPercent).toFixed(2));
}

/**
 * Generate total feedbacks based on product age
 * Older products tend to have more feedbacks
 */
function generateTotalFeedbacks(createdAt: Date): number {
  const now = new Date();
  const ageInDays = Math.floor((now.getTime() - createdAt.getTime()) / (1000 * 60 * 60 * 24));
  
  // Newer products (< 30 days): 0-20 feedbacks
  // Medium age (30-90 days): 10-50 feedbacks
  // Older products (> 90 days): 20-100 feedbacks
  if (ageInDays < 30) {
    return Math.floor(Math.random() * 21); // 0-20
  } else if (ageInDays < 90) {
    return Math.floor(Math.random() * 41) + 10; // 10-50
  } else {
    return Math.floor(Math.random() * 81) + 20; // 20-100
  }
}

/**
 * Generate average rating (double precision, 0.0-5.0)
 * Should match avg_rate for consistency
 */
function generateAverageRating(avgRate: number): number {
  // Use the same value as avg_rate for consistency
  return avgRate;
}

/**
 * Generate at least 150 products using product name templates
 * 
 * @param registry - ID registry to register product IDs
 * @returns Array of product records
 * 
 * Requirements:
 * - 2.1: Generate product names that represent Vietnamese food items
 * - 2.2: Generate product descriptions that accurately describe the food items
 * - 2.3: Generate product prices within realistic ranges for Vietnamese food delivery
 * - 2.6: Generate images field as JSONB array of image URLs
 * - 3.1: Generate at least 100 records for product.products table (we generate 150+)
 * - 4.3: Generate data with correct data types
 * - 4.6: Generate realistic prices using price range templates
 * - 12.1: Assign each product to exactly one shop via shop_id foreign key
 * - 12.2: Distribute products across shops with varying quantities
 * - 12.3: Ensure each shop has at least 5 products
 * - 12.5: Set product_status with realistic distribution
 * - 12.6: Generate realistic SKU values
 */
export function generateProducts(registry: IDRegistry): Product[] {
  const now = new Date();
  const products: Product[] = [];
  
  // Get all shop IDs from registry
  const shopIds = registry.getAllIds('shops');
  
  if (shopIds.length === 0) {
    throw new Error('No shops found in registry. Generate shops before generating products.');
  }

  // Track products per shop to ensure minimum 5 products per shop
  const productsPerShop = new Map<string, number>();
  shopIds.forEach(shopId => productsPerShop.set(shopId, 0));

  // Category counters for SKU generation
  const categoryCounters = new Map<string, number>();

  // Generate products from templates
  for (let i = 0; i < PRODUCT_TEMPLATES.length; i++) {
    const template = PRODUCT_TEMPLATES[i];
    const productId = generateUUID();
    
    // Assign shop (round-robin to ensure even distribution)
    const shopId = shopIds[i % shopIds.length];
    productsPerShop.set(shopId, (productsPerShop.get(shopId) || 0) + 1);

    // Generate SKU
    const categoryCounter = (categoryCounters.get(template.category) || 0) + 1;
    categoryCounters.set(template.category, categoryCounter);
    const sku = generateSKU(template.category, categoryCounter);

    // Generate product status
    const status = generateProductStatus();
    
    // Generate price and related fields
    const retailPrice = generateRealisticPrice(template.category);
    const wholesalePrice = generateWholesalePrice(retailPrice);
    
    // Generate timestamps
    const createdAt = new Date(now.getTime() - Math.random() * 180 * 24 * 60 * 60 * 1000); // Random date within last 6 months
    
    // Generate rating fields
    const avgRate = generateAvgRate();
    const averageRating = generateAverageRating(avgRate);
    const totalFeedbacks = generateTotalFeedbacks(createdAt);

    const product: Product = {
      product_id: productId,
      name: template.name,
      description: template.description,
      price: retailPrice,
      wholesale_price: wholesalePrice,
      shop_id: shopId,
      product_status: status,
      sku: sku,
      quantity: generateQuantity(status),
      images: generateProductImages(template.name, 3),
      avg_rate: avgRate,
      average_rating: averageRating,
      total_feedbacks: totalFeedbacks,
      created_at: createdAt,
      updated_at: now,
      category: template.category // Temporary field for category assignment
    };

    products.push(product);

    // Register product ID in registry
    registry.register('products', productId, product);
  }

  // Validate that each shop has at least 5 products
  const shopsWithFewProducts = Array.from(productsPerShop.entries())
    .filter(([_, count]) => count < 5);
  
  if (shopsWithFewProducts.length > 0) {
    console.warn(`Warning: ${shopsWithFewProducts.length} shops have fewer than 5 products`);
    console.warn('Shops with few products:', shopsWithFewProducts.map(([shopId, count]) => `${shopId}: ${count}`));
  }

  return products;
}

/**
 * Validate that at least 150 products were generated
 */
export function validateProductCount(products: Product[]): boolean {
  return products.length >= 150;
}

/**
 * Validate that all products have realistic prices within ranges
 */
export function validateProductPrices(products: Product[]): boolean {
  // Prices should be between 10,000 and 500,000 VND
  return products.every(p => p.price >= 10000 && p.price <= 500000);
}

/**
 * Validate that all product shop_id values exist in shops registry
 */
export function validateProductShopReferences(products: Product[], registry: IDRegistry): boolean {
  const shopIds = new Set(registry.getAllIds('shops'));
  
  for (const product of products) {
    if (!shopIds.has(product.shop_id)) {
      console.error(`Invalid shop reference: Product ${product.product_id} references non-existent shop ${product.shop_id}`);
      return false;
    }
  }
  
  return true;
}

/**
 * Validate that each shop has at least 5 products
 */
export function validateMinimumProductsPerShop(products: Product[], registry: IDRegistry): boolean {
  const shopIds = registry.getAllIds('shops');
  const productsPerShop = new Map<string, number>();
  
  // Initialize counters
  shopIds.forEach(shopId => productsPerShop.set(shopId, 0));
  
  // Count products per shop
  for (const product of products) {
    const currentCount = productsPerShop.get(product.shop_id) || 0;
    productsPerShop.set(product.shop_id, currentCount + 1);
  }
  
  // Check minimum
  const shopsWithFewProducts = Array.from(productsPerShop.entries())
    .filter(([_, count]) => count < 5);
  
  if (shopsWithFewProducts.length > 0) {
    console.error(`${shopsWithFewProducts.length} shops have fewer than 5 products`);
    return false;
  }
  
  return true;
}

/**
 * Get products by category
 */
export function getProductsByCategory(products: Product[], category: string): Product[] {
  return products.filter(p => p.category === category);
}

/**
 * Get products by shop
 */
export function getProductsByShop(products: Product[], shopId: string): Product[] {
  return products.filter(p => p.shop_id === shopId);
}

/**
 * Get products by status
 */
export function getProductsByStatus(products: Product[], status: 'AVAILABLE' | 'OUT_OF_STOCK' | 'DISCONTINUED'): Product[] {
  return products.filter(p => p.product_status === status);
}

/**
 * Get product count by category
 */
export function getProductCountByCategory(products: Product[]): Record<string, number> {
  const counts: Record<string, number> = {};
  
  for (const product of products) {
    counts[product.category] = (counts[product.category] || 0) + 1;
  }
  
  return counts;
}

/**
 * Get product count by shop
 */
export function getProductCountByShop(products: Product[]): Record<string, number> {
  const counts: Record<string, number> = {};
  
  for (const product of products) {
    counts[product.shop_id] = (counts[product.shop_id] || 0) + 1;
  }
  
  return counts;
}

/**
 * Validate product status distribution
 * Should be approximately 80% AVAILABLE, 15% OUT_OF_STOCK, 5% DISCONTINUED
 */
export function validateProductStatusDistribution(products: Product[]): boolean {
  const total = products.length;
  const availableCount = products.filter(p => p.product_status === 'AVAILABLE').length;
  const outOfStockCount = products.filter(p => p.product_status === 'OUT_OF_STOCK').length;
  const discontinuedCount = products.filter(p => p.product_status === 'DISCONTINUED').length;
  
  const availablePercent = availableCount / total;
  const outOfStockPercent = outOfStockCount / total;
  const discontinuedPercent = discontinuedCount / total;
  
  // Allow 10% tolerance
  return (
    availablePercent >= 0.70 && availablePercent <= 0.90 &&
    outOfStockPercent >= 0.05 && outOfStockPercent <= 0.25 &&
    discontinuedPercent >= 0.00 && discontinuedPercent <= 0.15
  );
}
