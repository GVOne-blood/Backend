/**
 * Category Generator Module
 * 
 * Generates at least 50 categories (10 root + 40 child) with proper hierarchy.
 * Maximum hierarchy depth: 2 levels (root and child only).
 * 
 * Requirements: 2.5, 3.2, 11.1, 11.2, 11.3, 11.4, 11.5, 11.6, 11.7, 11.8
 */

import { IDRegistry } from '../utils/id-registry';
import { ROOT_CATEGORIES, CHILD_CATEGORIES, CategoryTemplate } from '../templates/categories';

export interface Category {
  category_name: string; // Primary key (VARCHAR)
  description: string;
  slug: string;
  parent_id: string | null;
  category_group_code: string;
  is_active: boolean;
}

/**
 * Generate at least 50 categories (10 root + 40 child)
 * 
 * @param registry - ID registry to register category names
 * @returns Array of category records
 * 
 * Requirements:
 * - 2.5: Generate category names that represent food categories
 * - 3.2: Generate at least 50 records for product.categories table
 * - 11.1: Create root categories with parent_id as null
 * - 11.2: Create child categories with parent_id referencing existing parent categories
 * - 11.3: Generate at least 10 root categories representing main food types
 * - 11.4: Generate at least 40 child categories representing specific food items
 * - 11.5: Populate slug field with URL-friendly versions of category names
 * - 11.6: Set is_active to true for all categories
 * - 11.7: Assign appropriate category_group_code values for grouping related categories
 * - 11.8: Ensure category hierarchy depth does not exceed 2 levels
 */
export function generateCategories(registry: IDRegistry): Category[] {
  const categories: Category[] = [];

  // Generate root categories first (Level 1)
  for (const template of ROOT_CATEGORIES) {
    const category: Category = {
      category_name: template.category_name,
      description: template.description,
      slug: template.slug,
      parent_id: template.parent_id,
      category_group_code: template.category_group_code,
      is_active: template.is_active
    };

    categories.push(category);

    // Register category name in registry (categories use category_name as PK, not UUID)
    registry.register('categories', category.category_name, category);
  }

  // Generate child categories (Level 2)
  for (const template of CHILD_CATEGORIES) {
    // Validate that parent exists in registry
    if (template.parent_id && !registry.exists('categories', template.parent_id)) {
      throw new Error(`Parent category '${template.parent_id}' not found for child category '${template.category_name}'`);
    }

    const category: Category = {
      category_name: template.category_name,
      description: template.description,
      slug: template.slug,
      parent_id: template.parent_id,
      category_group_code: template.category_group_code,
      is_active: template.is_active
    };

    categories.push(category);

    // Register category name in registry
    registry.register('categories', category.category_name, category);
  }

  return categories;
}

/**
 * Validate that at least 50 categories were generated
 */
export function validateCategoryCount(categories: Category[]): boolean {
  return categories.length >= 50;
}

/**
 * Validate that all categories are active
 */
export function validateCategoryActive(categories: Category[]): boolean {
  return categories.every(c => c.is_active === true);
}

/**
 * Validate category hierarchy depth (max 2 levels)
 * 
 * Requirements:
 * - 11.8: Ensure category hierarchy depth does not exceed 2 levels
 */
export function validateCategoryHierarchy(categories: Category[]): boolean {
  // Check that no child category has children
  const childCategories = categories.filter(c => c.parent_id !== null);
  
  for (const child of childCategories) {
    // Check if any category has this child as parent
    const hasChildren = categories.some(c => c.parent_id === child.category_name);
    if (hasChildren) {
      console.error(`Category hierarchy violation: ${child.category_name} has children but is already a child category`);
      return false;
    }
  }
  
  return true;
}

/**
 * Get root categories (parent_id is null)
 */
export function getRootCategories(categories: Category[]): Category[] {
  return categories.filter(c => c.parent_id === null);
}

/**
 * Get child categories (parent_id is not null)
 */
export function getChildCategories(categories: Category[]): Category[] {
  return categories.filter(c => c.parent_id !== null);
}

/**
 * Get categories by parent
 */
export function getCategoriesByParent(categories: Category[], parentName: string): Category[] {
  return categories.filter(c => c.parent_id === parentName);
}

/**
 * Get category by name
 */
export function getCategoryByName(categories: Category[], name: string): Category | undefined {
  return categories.find(c => c.category_name === name);
}

/**
 * Validate that all parent references exist
 */
export function validateParentReferences(categories: Category[]): boolean {
  const categoryNames = new Set(categories.map(c => c.category_name));
  
  for (const category of categories) {
    if (category.parent_id && !categoryNames.has(category.parent_id)) {
      console.error(`Invalid parent reference: ${category.category_name} references non-existent parent ${category.parent_id}`);
      return false;
    }
  }
  
  return true;
}
