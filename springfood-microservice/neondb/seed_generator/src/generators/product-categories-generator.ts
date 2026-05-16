/**
 * Product Categories Junction Table Generator
 * 
 * Generates records linking products to categories.
 * Each product belongs to 1-3 categories.
 * 
 * Requirements: 1.5, 9.3
 */

import { IDRegistry } from '../utils/id-registry';
import { Product } from './product-generator';

export interface ProductCategory {
  product_id: string;
  category_name: string;
}

/**
 * Get related categories for a product based on its primary category
 * 
 * @param primaryCategory - The product's primary category
 * @param allCategories - All available category names
 * @returns Array of related category names (1-3 categories)
 */
function getRelatedCategories(primaryCategory: string, allCategories: string[]): string[] {
  const categories: string[] = [primaryCategory];
  
  // Category relationship mapping
  const categoryRelationships: Record<string, string[]> = {
    // Beverages can belong to parent category
    'Cà Phê': ['Đồ Uống'],
    'Trà Sữa': ['Đồ Uống'],
    'Trà Trái Cây': ['Đồ Uống'],
    'Nước Ép': ['Đồ Uống'],
    'Sinh Tố': ['Đồ Uống'],
    
    // Main dishes can belong to parent category
    'Phở': ['Món Việt Truyền Thống', 'Món Ăn Sáng'],
    'Bún': ['Món Việt Truyền Thống'],
    'Cơm': ['Món Việt Truyền Thống'],
    'Bánh Mì': ['Món Ăn Sáng', 'Bánh Ngọt & Bánh Mì'],
    'Xôi': ['Món Ăn Sáng'],
    'Cháo': ['Món Ăn Sáng'],
    
    // Hotpot
    'Lẩu Thái': ['Món Lẩu'],
    'Lẩu Hải Sản': ['Món Lẩu', 'Món Hải Sản'],
    'Lẩu Bò': ['Món Lẩu'],
    
    // BBQ & Grilled
    'Thịt Nướng': ['Món Nướng & BBQ'],
    'Hải Sản Nướng BBQ': ['Món Nướng & BBQ', 'Món Hải Sản'],
    'Xiên Nướng': ['Món Nướng & BBQ'],
    
    // Desserts
    'Chè': ['Tráng Miệng'],
    'Kem': ['Tráng Miệng'],
    'Bánh Ngọt': ['Tráng Miệng', 'Bánh Ngọt & Bánh Mì'],
    'Trái Cây Dầm': ['Tráng Miệng'],
    
    // Snacks
    'Nem Rán': ['Món Ăn Vặt', 'Nem & Chả'],
    'Bánh Tráng Trộn': ['Món Ăn Vặt'],
    'Bánh Bao': ['Món Ăn Vặt'],
    'Nem & Chả': ['Món Việt Truyền Thống'],
    
    // Fast Food
    'Burger': ['Đồ Ăn Nhanh'],
    'Pizza': ['Đồ Ăn Nhanh'],
    'Gà Rán': ['Đồ Ăn Nhanh'],
    
    // Asian
    'Sushi': ['Món Á'],
    'Dimsum': ['Món Á'],
    'Mì Ý': ['Món Á'],
    
    // Western
    'Pasta': ['Món Âu'],
    'Steak': ['Món Âu'],
    'Salad': ['Món Âu', 'Món Healthy'],
    
    // Bakery
    'Bánh Mì Ngọt': ['Bánh Ngọt & Bánh Mì'],
    'Bánh Kem': ['Bánh Ngọt & Bánh Mì', 'Tráng Miệng'],
    'Bánh Bông Lan': ['Bánh Ngọt & Bánh Mì'],
    
    // Healthy
    'Salad Healthy': ['Món Healthy'],
    'Smoothie Bowl': ['Món Healthy'],
    'Ức Gà': ['Món Healthy'],
    
    // Vegetarian
    'Cơm Chay': ['Món Chay'],
    'Bún Chay': ['Món Chay'],
    'Lẩu Chay': ['Món Chay', 'Món Lẩu'],
    
    // Regional
    'Miền Bắc': ['Món Đặc Sản Miền'],
    'Miền Trung': ['Món Đặc Sản Miền'],
    'Miền Nam': ['Món Đặc Sản Miền'],
    
    // Seafood
    'Món Hải Sản': ['Món Hải Sản'],
    'Hải Sản Nướng': ['Món Hải Sản', 'Món Nướng & BBQ'],
    'Hải Sản Hấp': ['Món Hải Sản'],
    'Hải Sản Chiên': ['Món Hải Sản']
  };
  
  // Get related categories
  const relatedCategories = categoryRelationships[primaryCategory] || [];
  
  // Add 1-2 related categories randomly
  const numRelated = Math.floor(Math.random() * 2); // 0-1 additional categories
  
  for (let i = 0; i < numRelated && i < relatedCategories.length; i++) {
    const relatedCategory = relatedCategories[i];
    if (allCategories.includes(relatedCategory) && !categories.includes(relatedCategory)) {
      categories.push(relatedCategory);
    }
  }
  
  return categories;
}

/**
 * Generate product_categories junction table records
 * 
 * @param products - Array of product records
 * @param registry - ID registry containing category names
 * @returns Array of product_category records
 * 
 * Requirements:
 * - 1.5: Ensure both product_id and category_name exist in registries
 * - 9.3: Ensure both product_id and category_name exist in their respective tables
 */
export function generateProductCategories(products: Product[], registry: IDRegistry): ProductCategory[] {
  const productCategories: ProductCategory[] = [];
  
  // Get all category names from registry
  const allCategoryNames = registry.getAllIds('categories');
  
  if (allCategoryNames.length === 0) {
    throw new Error('No categories found in registry. Generate categories before generating product_categories.');
  }

  for (const product of products) {
    // Validate product exists in registry
    if (!registry.exists('products', product.product_id)) {
      throw new Error(`Product ${product.product_id} not found in registry`);
    }

    // Get related categories for this product (1-3 categories)
    const categories = getRelatedCategories(product.category, allCategoryNames);
    
    // Create product_category records
    for (const categoryName of categories) {
      // Validate category exists in registry
      if (!registry.exists('categories', categoryName)) {
        console.warn(`Category '${categoryName}' not found in registry. Skipping.`);
        continue;
      }

      productCategories.push({
        product_id: product.product_id,
        category_name: categoryName
      });
    }
  }

  return productCategories;
}

/**
 * Validate that all product_id values exist in products registry
 */
export function validateProductReferences(productCategories: ProductCategory[], registry: IDRegistry): boolean {
  const productIds = new Set(registry.getAllIds('products'));
  
  for (const pc of productCategories) {
    if (!productIds.has(pc.product_id)) {
      console.error(`Invalid product reference: ${pc.product_id} not found in products registry`);
      return false;
    }
  }
  
  return true;
}

/**
 * Validate that all category_name values exist in categories registry
 */
export function validateCategoryReferences(productCategories: ProductCategory[], registry: IDRegistry): boolean {
  const categoryNames = new Set(registry.getAllIds('categories'));
  
  for (const pc of productCategories) {
    if (!categoryNames.has(pc.category_name)) {
      console.error(`Invalid category reference: ${pc.category_name} not found in categories registry`);
      return false;
    }
  }
  
  return true;
}

/**
 * Validate that each product belongs to 1-3 categories
 */
export function validateProductCategoryCount(productCategories: ProductCategory[]): boolean {
  const productCategoryCounts = new Map<string, number>();
  
  for (const pc of productCategories) {
    const currentCount = productCategoryCounts.get(pc.product_id) || 0;
    productCategoryCounts.set(pc.product_id, currentCount + 1);
  }
  
  // Check that each product has 1-3 categories
  for (const [productId, count] of productCategoryCounts.entries()) {
    if (count < 1 || count > 3) {
      console.error(`Product ${productId} has ${count} categories (expected 1-3)`);
      return false;
    }
  }
  
  return true;
}

/**
 * Get categories for a specific product
 */
export function getCategoriesForProduct(productCategories: ProductCategory[], productId: string): string[] {
  return productCategories
    .filter(pc => pc.product_id === productId)
    .map(pc => pc.category_name);
}

/**
 * Get products for a specific category
 */
export function getProductsForCategory(productCategories: ProductCategory[], categoryName: string): string[] {
  return productCategories
    .filter(pc => pc.category_name === categoryName)
    .map(pc => pc.product_id);
}

/**
 * Get product category count statistics
 */
export function getProductCategoryStats(productCategories: ProductCategory[]): {
  totalRecords: number;
  uniqueProducts: number;
  uniqueCategories: number;
  avgCategoriesPerProduct: number;
} {
  const uniqueProducts = new Set(productCategories.map(pc => pc.product_id));
  const uniqueCategories = new Set(productCategories.map(pc => pc.category_name));
  
  return {
    totalRecords: productCategories.length,
    uniqueProducts: uniqueProducts.size,
    uniqueCategories: uniqueCategories.size,
    avgCategoriesPerProduct: productCategories.length / uniqueProducts.size
  };
}
