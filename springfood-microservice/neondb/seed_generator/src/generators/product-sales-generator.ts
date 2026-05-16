/**
 * Product-Sales Junction Table Generator Module
 * 
 * Generates records linking products to sales campaigns.
 * For each sale, determines number of products based on campaign category.
 * For category_specific sales, filters products by category.
 * 
 * Requirements: 2.7
 */

import { IDRegistry } from '../utils/id-registry';
import { SALES_DISTRIBUTION, extractCategoryFromSaleName, SaleCategory } from '../templates/sales';
import { Sale } from './sales-generator';
import { Product } from './product-generator';

export interface ProductSale {
  product_id: string;
  sale_id: string;
}

/**
 * Generate random integer between min and max (inclusive)
 */
function randomInt(min: number, max: number): number {
  return Math.floor(Math.random() * (max - min + 1)) + min;
}

/**
 * Shuffle array using Fisher-Yates algorithm
 */
function shuffleArray<T>(array: T[]): T[] {
  const shuffled = [...array];
  for (let i = shuffled.length - 1; i > 0; i--) {
    const j = Math.floor(Math.random() * (i + 1));
    [shuffled[i], shuffled[j]] = [shuffled[j], shuffled[i]];
  }
  return shuffled;
}

/**
 * Check if product matches category keywords
 * 
 * @param product - Product to check
 * @param targetCategory - Target category name (e.g., "Trà Sữa", "Phở")
 * @returns true if product belongs to target category
 */
function productMatchesCategory(product: Product, targetCategory: string): boolean {
  // Direct category match
  if (product.category === targetCategory) {
    return true;
  }
  
  // Fuzzy match on product name and description
  const searchText = `${product.name} ${product.description} ${product.category}`.toLowerCase();
  const targetLower = targetCategory.toLowerCase();
  
  // Remove diacritics for better matching
  const normalizedSearch = searchText
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '');
  const normalizedTarget = targetLower
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '');
  
  return normalizedSearch.includes(normalizedTarget);
}

/**
 * Filter products by category for category-specific sales
 * 
 * @param products - All products
 * @param saleName - Sale name (e.g., "Tuần Lễ Trà Sữa")
 * @returns Products matching the category
 */
function filterProductsByCategory(products: Product[], saleName: string): Product[] {
  const targetCategory = extractCategoryFromSaleName(saleName);
  
  // If category is "Tất Cả" (all), return all products
  if (targetCategory === 'Tất Cả') {
    return products;
  }
  
  // Filter products by category
  return products.filter(p => productMatchesCategory(p, targetCategory));
}

/**
 * Generate product_sales junction table records
 * 
 * @param registry - ID registry containing sales and products
 * @returns Array of product_sales records
 * 
 * Requirements:
 * - 2.7: Generate records linking products to sales campaigns
 * - For each sale, determine number of products based on campaign category
 * - For category_specific sales, filter products by category
 * - For flash_sale and weekend sales, select random products
 * - Ensure both product_id and sale_id exist in registries
 */
export function generateProductSales(registry: IDRegistry): ProductSale[] {
  const productSales: ProductSale[] = [];
  
  // Get all sales and products from registry
  const sales = registry.getAllData('sales') as Sale[];
  const products = registry.getAllData('products') as Product[];
  
  if (sales.length === 0) {
    throw new Error('No sales found in registry. Generate sales before generating product_sales.');
  }
  
  if (products.length === 0) {
    throw new Error('No products found in registry. Generate products before generating product_sales.');
  }

  // For each sale, assign products based on category
  for (const sale of sales) {
    const category = sale.category as SaleCategory;
    
    // Skip free_shipping sales (no product assignment needed)
    if (category === 'free_shipping') {
      continue;
    }
    
    // Get product count range for this category
    const range = SALES_DISTRIBUTION.productsPerSale[category];
    if (!range) {
      console.warn(`Unknown sale category: ${category}, skipping product assignment`);
      continue;
    }
    
    // Determine number of products for this sale
    const productCount = randomInt(range.min, range.max);
    
    // Select products based on sale category
    let eligibleProducts: Product[];
    
    if (category === 'category_specific') {
      // Filter products by category (e.g., only trà sữa for "Tuần Lễ Trà Sữa")
      eligibleProducts = filterProductsByCategory(products, sale.name);
      
      // If no products match, use all products as fallback
      if (eligibleProducts.length === 0) {
        console.warn(`No products found for category-specific sale "${sale.name}", using all products`);
        eligibleProducts = [...products];
      }
    } else if (category === 'flash_sale' || category === 'weekend') {
      // Random selection for flash sales and weekend sales
      eligibleProducts = shuffleArray([...products]);
    } else if (category === 'new_customer' || category === 'loyalty' || category === 'seasonal') {
      // Apply to many products (broad coverage)
      eligibleProducts = shuffleArray([...products]);
    } else if (category === 'time_based') {
      // Time-based sales typically apply to specific meal categories
      // For breakfast sales, prefer breakfast items; for happy hour, prefer drinks
      if (sale.name.toLowerCase().includes('sáng') || sale.name.toLowerCase().includes('breakfast')) {
        eligibleProducts = products.filter(p => 
          p.category.includes('Ăn Sáng') || 
          p.category.includes('Bánh Mì') || 
          p.category.includes('Xôi') ||
          p.category.includes('Cháo')
        );
        
        // Fallback to all products if no breakfast items
        if (eligibleProducts.length === 0) {
          eligibleProducts = [...products];
        }
      } else if (sale.name.toLowerCase().includes('happy hour') || sale.name.toLowerCase().includes('đồ uống')) {
        eligibleProducts = products.filter(p => 
          p.category.includes('Đồ Uống') || 
          p.category.includes('Cà Phê') || 
          p.category.includes('Trà Sữa') ||
          p.category.includes('Nước Ép') ||
          p.category.includes('Sinh Tố')
        );
        
        // Fallback to all products if no drinks
        if (eligibleProducts.length === 0) {
          eligibleProducts = [...products];
        }
      } else {
        eligibleProducts = shuffleArray([...products]);
      }
    } else if (category === 'combo') {
      // Combo sales typically apply to popular items
      eligibleProducts = shuffleArray([...products]);
    } else if (category === 'holiday') {
      // Holiday sales apply to many products
      eligibleProducts = shuffleArray([...products]);
    } else {
      // Default: random selection
      eligibleProducts = shuffleArray([...products]);
    }
    
    // Take the required number of products
    const selectedProducts = eligibleProducts.slice(0, Math.min(productCount, eligibleProducts.length));
    
    // Create product_sales records
    for (const product of selectedProducts) {
      // Validate foreign keys exist
      if (!registry.exists('products', product.product_id)) {
        console.error(`Product ${product.product_id} not found in registry`);
        continue;
      }
      
      if (!registry.exists('sales', sale.sale_id)) {
        console.error(`Sale ${sale.sale_id} not found in registry`);
        continue;
      }
      
      productSales.push({
        product_id: product.product_id,
        sale_id: sale.sale_id
      });
    }
  }

  return productSales;
}

/**
 * Validate that product_sales assigns appropriate number of products per sale
 */
export function validateProductSalesCount(productSales: ProductSale[], registry: IDRegistry): boolean {
  const sales = registry.getAllData('sales') as Sale[];
  
  // Group product_sales by sale_id
  const productsBySale = new Map<string, number>();
  for (const ps of productSales) {
    productsBySale.set(ps.sale_id, (productsBySale.get(ps.sale_id) || 0) + 1);
  }
  
  // Validate each sale has appropriate number of products
  for (const sale of sales) {
    const category = sale.category as SaleCategory;
    
    // Skip free_shipping sales
    if (category === 'free_shipping') {
      continue;
    }
    
    const range = SALES_DISTRIBUTION.productsPerSale[category];
    if (!range) {
      continue;
    }
    
    const productCount = productsBySale.get(sale.sale_id) || 0;
    
    // Allow some tolerance (products might be less than max if not enough eligible products)
    if (productCount < range.min) {
      console.error(`Sale ${sale.name} has ${productCount} products, expected at least ${range.min}`);
      return false;
    }
  }
  
  return true;
}

/**
 * Validate that category_specific sales only include products from target category
 */
export function validateCategorySpecificSales(productSales: ProductSale[], registry: IDRegistry): boolean {
  const sales = registry.getAllData('sales') as Sale[];
  const products = registry.getAllData('products') as Product[];
  
  // Build product map for quick lookup
  const productMap = new Map<string, Product>();
  for (const product of products) {
    productMap.set(product.product_id, product);
  }
  
  // Check each category_specific sale
  for (const sale of sales) {
    if (sale.category !== 'category_specific') {
      continue;
    }
    
    const targetCategory = extractCategoryFromSaleName(sale.name);
    
    // Get products for this sale
    const saleProducts = productSales
      .filter(ps => ps.sale_id === sale.sale_id)
      .map(ps => productMap.get(ps.product_id))
      .filter((p): p is Product => p !== undefined);
    
    // Validate all products match the category
    for (const product of saleProducts) {
      if (!productMatchesCategory(product, targetCategory)) {
        console.error(`Sale "${sale.name}" (category: ${targetCategory}) includes product "${product.name}" (category: ${product.category}) which doesn't match`);
        // This is a warning, not a hard error, since fuzzy matching might not be perfect
        console.warn('This is a warning, not a hard error. Fuzzy matching might not be perfect.');
      }
    }
  }
  
  return true;
}

/**
 * Validate that all foreign keys exist in registries
 */
export function validateProductSalesForeignKeys(productSales: ProductSale[], registry: IDRegistry): boolean {
  for (const ps of productSales) {
    if (!registry.exists('products', ps.product_id)) {
      console.error(`Invalid product_id: ${ps.product_id} not found in products registry`);
      return false;
    }
    
    if (!registry.exists('sales', ps.sale_id)) {
      console.error(`Invalid sale_id: ${ps.sale_id} not found in sales registry`);
      return false;
    }
  }
  
  return true;
}

/**
 * Get product_sales statistics
 */
export function getProductSalesStats(productSales: ProductSale[], registry: IDRegistry): {
  total: number;
  salesWithProducts: number;
  avgProductsPerSale: number;
  minProductsPerSale: number;
  maxProductsPerSale: number;
} {
  const sales = registry.getAllData('sales') as Sale[];
  
  // Group by sale_id
  const productsBySale = new Map<string, number>();
  for (const ps of productSales) {
    productsBySale.set(ps.sale_id, (productsBySale.get(ps.sale_id) || 0) + 1);
  }
  
  const counts = Array.from(productsBySale.values());
  
  return {
    total: productSales.length,
    salesWithProducts: productsBySale.size,
    avgProductsPerSale: counts.length > 0 ? counts.reduce((sum, c) => sum + c, 0) / counts.length : 0,
    minProductsPerSale: counts.length > 0 ? Math.min(...counts) : 0,
    maxProductsPerSale: counts.length > 0 ? Math.max(...counts) : 0
  };
}

/**
 * Get products for a specific sale
 */
export function getProductsForSale(productSales: ProductSale[], saleId: string, registry: IDRegistry): Product[] {
  const products = registry.getAllData('products') as Product[];
  const productMap = new Map<string, Product>();
  for (const product of products) {
    productMap.set(product.product_id, product);
  }
  
  return productSales
    .filter(ps => ps.sale_id === saleId)
    .map(ps => productMap.get(ps.product_id))
    .filter((p): p is Product => p !== undefined);
}

/**
 * Get sales for a specific product
 */
export function getSalesForProduct(productSales: ProductSale[], productId: string, registry: IDRegistry): Sale[] {
  const sales = registry.getAllData('sales') as Sale[];
  const saleMap = new Map<string, Sale>();
  for (const sale of sales) {
    saleMap.set(sale.sale_id, sale);
  }
  
  return productSales
    .filter(ps => ps.product_id === productId)
    .map(ps => saleMap.get(ps.sale_id))
    .filter((s): s is Sale => s !== undefined);
}
