/**
 * Order Items Generator Module
 * 
 * Generates at least 2 order items for each order.
 * Ensures product_id references existing products from the same shop as the order.
 * Populates product_name and product_image_url from referenced product.
 * 
 * Requirements: 13.1, 13.2, 13.3, 13.4
 */

import { IDRegistry } from '../utils/id-registry';
import { generateUUID } from '../utils/uuid-generator';
import { Order } from './order-generator';
import { Product } from './product-generator';

export interface OrderItem {
  order_item_id: string;
  order_id: string;
  product_id: string;
  product_name: string;
  product_image_url: string;
  quantity: number;
  price_at_booking: number;
  created_at: Date;
  updated_at: Date;
  subtotal: number; // Temporary field for calculation (not in DB schema)
}

/**
 * Generate random quantity (1-5 items)
 */
function generateQuantity(): number {
  return Math.floor(Math.random() * 5) + 1;
}

/**
 * Extract first image URL from product images JSONB
 */
function extractFirstImageUrl(imagesJson: string): string {
  try {
    const images = JSON.parse(imagesJson);
    if (Array.isArray(images) && images.length > 0) {
      return images[0];
    }
  } catch (error) {
    console.warn('Failed to parse product images JSON:', error);
  }
  
  // Fallback to placeholder
  return 'https://via.placeholder.com/400x300/FF6B6B/FFFFFF?text=Product';
}

/**
 * Generate at least 2 order items for each order
 * 
 * @param registry - ID registry containing orders and products
 * @returns Array of order_items records
 * 
 * Requirements:
 * - 13.1: Generate at least 2 order items for each order
 * - 13.2: Ensure product_id references existing products from the same shop as the order
 * - 13.3: Populate product_name and product_image_url from referenced product
 * - 13.4: Set price_at_booking to match product price at time of order
 */
export function generateOrderItems(registry: IDRegistry): OrderItem[] {
  const orderItems: OrderItem[] = [];
  
  // Get all orders and products from registry
  const orders = registry.getAllData('orders') as Order[];
  const products = registry.getAllData('products') as Product[];
  
  if (orders.length === 0) {
    throw new Error('No orders found in registry. Generate orders before generating order_items.');
  }
  
  if (products.length === 0) {
    throw new Error('No products found in registry. Generate products before generating order_items.');
  }

  // Build product map by shop_id for quick lookup
  const productsByShop = new Map<string, Product[]>();
  for (const product of products) {
    if (!productsByShop.has(product.shop_id)) {
      productsByShop.set(product.shop_id, []);
    }
    productsByShop.get(product.shop_id)!.push(product);
  }

  // Generate order items for each order
  for (const order of orders) {
    // Get products from the same shop as the order
    const shopProducts = productsByShop.get(order.shop_id) || [];
    
    if (shopProducts.length === 0) {
      console.warn(`No products found for shop ${order.shop_id}, skipping order ${order.order_id}`);
      continue;
    }

    // Generate 2-5 order items per order
    const itemCount = Math.floor(Math.random() * 4) + 2; // 2-5 items
    
    // Shuffle products to get random selection
    const shuffledProducts = [...shopProducts].sort(() => Math.random() - 0.5);
    
    // Take first N products (avoid duplicates)
    const selectedProducts = shuffledProducts.slice(0, Math.min(itemCount, shopProducts.length));
    
    for (const product of selectedProducts) {
      const orderItemId = generateUUID();
      const quantity = generateQuantity();
      const priceAtBooking = product.price;
      const subtotal = quantity * priceAtBooking;
      
      const orderItem: OrderItem = {
        order_item_id: orderItemId,
        order_id: order.order_id,
        product_id: product.product_id,
        product_name: product.name,
        product_image_url: extractFirstImageUrl(product.images),
        quantity: quantity,
        price_at_booking: priceAtBooking,
        created_at: order.created_at,
        updated_at: order.updated_at,
        subtotal: subtotal // Temporary field for calculation
      };

      orderItems.push(orderItem);
    }
  }

  return orderItems;
}

/**
 * Validate that each order has at least 2 order items
 */
export function validateMinimumItemsPerOrder(orderItems: OrderItem[], registry: IDRegistry): boolean {
  const orders = registry.getAllData('orders') as Order[];
  
  // Count items per order
  const itemsPerOrder = new Map<string, number>();
  for (const item of orderItems) {
    itemsPerOrder.set(item.order_id, (itemsPerOrder.get(item.order_id) || 0) + 1);
  }
  
  // Check each order has at least 2 items
  for (const order of orders) {
    const itemCount = itemsPerOrder.get(order.order_id) || 0;
    
    if (itemCount < 2) {
      console.error(`Order ${order.order_id} has only ${itemCount} items, expected at least 2`);
      return false;
    }
  }
  
  return true;
}

/**
 * Validate that order items reference products from same shop as order
 */
export function validateOrderItemsShopConsistency(orderItems: OrderItem[], registry: IDRegistry): boolean {
  const orders = registry.getAllData('orders') as Order[];
  const products = registry.getAllData('products') as Product[];
  
  // Build maps for quick lookup
  const orderMap = new Map<string, Order>();
  for (const order of orders) {
    orderMap.set(order.order_id, order);
  }
  
  const productMap = new Map<string, Product>();
  for (const product of products) {
    productMap.set(product.product_id, product);
  }
  
  // Validate each order item
  for (const item of orderItems) {
    const order = orderMap.get(item.order_id);
    const product = productMap.get(item.product_id);
    
    if (!order) {
      console.error(`Order item ${item.order_item_id} references non-existent order ${item.order_id}`);
      return false;
    }
    
    if (!product) {
      console.error(`Order item ${item.order_item_id} references non-existent product ${item.product_id}`);
      return false;
    }
    
    if (order.shop_id !== product.shop_id) {
      console.error(`Order item ${item.order_item_id} references product ${item.product_id} from shop ${product.shop_id}, but order ${item.order_id} is from shop ${order.shop_id}`);
      return false;
    }
  }
  
  return true;
}

/**
 * Validate that product_name and product_image_url match referenced product
 */
export function validateOrderItemsProductData(orderItems: OrderItem[], registry: IDRegistry): boolean {
  const products = registry.getAllData('products') as Product[];
  
  // Build product map for quick lookup
  const productMap = new Map<string, Product>();
  for (const product of products) {
    productMap.set(product.product_id, product);
  }
  
  // Validate each order item
  for (const item of orderItems) {
    const product = productMap.get(item.product_id);
    
    if (!product) {
      console.error(`Order item ${item.order_item_id} references non-existent product ${item.product_id}`);
      return false;
    }
    
    if (item.product_name !== product.name) {
      console.error(`Order item ${item.order_item_id} has product_name "${item.product_name}", but product has name "${product.name}"`);
      return false;
    }
    
    // Validate image URL is from product images
    const expectedImageUrl = extractFirstImageUrl(product.images);
    if (item.product_image_url !== expectedImageUrl) {
      console.error(`Order item ${item.order_item_id} has product_image_url "${item.product_image_url}", but expected "${expectedImageUrl}"`);
      return false;
    }
  }
  
  return true;
}

/**
 * Validate that price_at_booking matches product price
 */
export function validateOrderItemsPrices(orderItems: OrderItem[], registry: IDRegistry): boolean {
  const products = registry.getAllData('products') as Product[];
  
  // Build product map for quick lookup
  const productMap = new Map<string, Product>();
  for (const product of products) {
    productMap.set(product.product_id, product);
  }
  
  // Validate each order item
  for (const item of orderItems) {
    const product = productMap.get(item.product_id);
    
    if (!product) {
      console.error(`Order item ${item.order_item_id} references non-existent product ${item.product_id}`);
      return false;
    }
    
    // Allow small rounding errors
    if (Math.abs(item.price_at_booking - product.price) > 0.01) {
      console.error(`Order item ${item.order_item_id} has price_at_booking ${item.price_at_booking}, but product price is ${product.price}`);
      return false;
    }
  }
  
  return true;
}

/**
 * Validate that subtotals are calculated correctly
 */
export function validateOrderItemsSubtotals(orderItems: OrderItem[]): boolean {
  for (const item of orderItems) {
    const expectedSubtotal = item.quantity * item.price_at_booking;
    
    // Allow small rounding errors
    if (Math.abs(item.subtotal - expectedSubtotal) > 0.01) {
      console.error(`Order item ${item.order_item_id} has subtotal ${item.subtotal}, but expected ${expectedSubtotal} (quantity ${item.quantity} * price ${item.price_at_booking})`);
      return false;
    }
  }
  
  return true;
}

/**
 * Get order items by order
 */
export function getOrderItemsByOrder(orderItems: OrderItem[], orderId: string): OrderItem[] {
  return orderItems.filter(item => item.order_id === orderId);
}

/**
 * Get order items by product
 */
export function getOrderItemsByProduct(orderItems: OrderItem[], productId: string): OrderItem[] {
  return orderItems.filter(item => item.product_id === productId);
}

/**
 * Calculate total subtotal for an order
 */
export function calculateOrderSubtotal(orderItems: OrderItem[], orderId: string): number {
  const items = getOrderItemsByOrder(orderItems, orderId);
  return items.reduce((sum, item) => sum + item.subtotal, 0);
}

/**
 * Get order items statistics
 */
export function getOrderItemsStats(orderItems: OrderItem[]): {
  total: number;
  avgQuantity: number;
  avgPrice: number;
  avgSubtotal: number;
  minQuantity: number;
  maxQuantity: number;
  minPrice: number;
  maxPrice: number;
} {
  if (orderItems.length === 0) {
    return {
      total: 0,
      avgQuantity: 0,
      avgPrice: 0,
      avgSubtotal: 0,
      minQuantity: 0,
      maxQuantity: 0,
      minPrice: 0,
      maxPrice: 0
    };
  }
  
  const quantities = orderItems.map(item => item.quantity);
  const prices = orderItems.map(item => item.price_at_booking);
  const subtotals = orderItems.map(item => item.subtotal);
  
  return {
    total: orderItems.length,
    avgQuantity: quantities.reduce((sum, q) => sum + q, 0) / quantities.length,
    avgPrice: prices.reduce((sum, p) => sum + p, 0) / prices.length,
    avgSubtotal: subtotals.reduce((sum, s) => sum + s, 0) / subtotals.length,
    minQuantity: Math.min(...quantities),
    maxQuantity: Math.max(...quantities),
    minPrice: Math.min(...prices),
    maxPrice: Math.max(...prices)
  };
}

/**
 * Build order items map for order total calculation
 * Returns a map of order_id to array of order items with subtotals
 */
export function buildOrderItemsMap(orderItems: OrderItem[]): Map<string, Array<{ subtotal: number }>> {
  const map = new Map<string, Array<{ subtotal: number }>>();
  
  for (const item of orderItems) {
    if (!map.has(item.order_id)) {
      map.set(item.order_id, []);
    }
    
    map.get(item.order_id)!.push({ subtotal: item.subtotal });
  }
  
  return map;
}
