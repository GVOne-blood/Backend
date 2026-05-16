/**
 * Order Totals Updater Module
 * 
 * Updates order totals after order_items are generated.
 * Calculates subtotal_amount and final_price for each order.
 * 
 * Requirements: 13.5, 13.6, 13.7
 */

import { Order, updateOrderTotals } from './order-generator';
import { OrderItem, buildOrderItemsMap } from './order-items-generator';

/**
 * Update order totals after order_items are generated
 * 
 * This is a convenience wrapper around the updateOrderTotals function
 * from order-generator.ts that handles the order items map building.
 * 
 * @param orders - Array of order records
 * @param orderItems - Array of order_items records
 * 
 * Requirements:
 * - 13.5: Calculate subtotal_amount as sum of (quantity * price_at_booking) from order_items
 * - 13.6: Generate realistic shipping_fee values (15,000 VND to 50,000 VND) - already set in order generator
 * - 13.7: Calculate final_price as subtotal_amount + shipping_fee - discount_amount
 * 
 * @example
 * ```typescript
 * const orders = generateOrders(registry, 10);
 * const orderItems = generateOrderItems(registry);
 * updateAllOrderTotals(orders, orderItems);
 * // Now orders have correct subtotal_amount and final_price
 * ```
 */
export function updateAllOrderTotals(orders: Order[], orderItems: OrderItem[]): void {
  // Build order items map for efficient lookup
  const orderItemsMap = buildOrderItemsMap(orderItems);
  
  // Update order totals using the function from order-generator
  updateOrderTotals(orders, orderItemsMap);
}

/**
 * Validate that order totals are calculated correctly
 * 
 * @param orders - Array of order records
 * @param orderItems - Array of order_items records
 * @returns true if all order totals are correct
 */
export function validateAllOrderTotals(orders: Order[], orderItems: OrderItem[]): boolean {
  // Build order items map
  const orderItemsMap = buildOrderItemsMap(orderItems);
  
  for (const order of orders) {
    const items = orderItemsMap.get(order.order_id) || [];
    
    // Calculate expected subtotal
    const expectedSubtotal = items.reduce((sum, item) => sum + item.subtotal, 0);
    
    // Calculate expected final price
    const expectedFinalPrice = Math.max(0, expectedSubtotal + order.shipping_fee - order.discount_amount);
    
    // Validate subtotal_amount
    if (Math.abs(order.subtotal_amount - expectedSubtotal) > 0.01) {
      console.error(`Order ${order.order_id} has incorrect subtotal_amount: expected ${expectedSubtotal}, got ${order.subtotal_amount}`);
      return false;
    }
    
    // Validate final_price
    if (Math.abs(order.final_price - expectedFinalPrice) > 0.01) {
      console.error(`Order ${order.order_id} has incorrect final_price: expected ${expectedFinalPrice}, got ${order.final_price}`);
      return false;
    }
    
    // Validate final_price is not negative
    if (order.final_price < 0) {
      console.error(`Order ${order.order_id} has negative final_price: ${order.final_price}`);
      return false;
    }
  }
  
  return true;
}

/**
 * Get order totals summary
 * 
 * @param orders - Array of order records
 * @returns Summary statistics of order totals
 */
export function getOrderTotalsSummary(orders: Order[]): {
  totalOrders: number;
  totalSubtotal: number;
  totalShippingFee: number;
  totalDiscount: number;
  totalFinalPrice: number;
  avgSubtotal: number;
  avgShippingFee: number;
  avgDiscount: number;
  avgFinalPrice: number;
  minFinalPrice: number;
  maxFinalPrice: number;
} {
  if (orders.length === 0) {
    return {
      totalOrders: 0,
      totalSubtotal: 0,
      totalShippingFee: 0,
      totalDiscount: 0,
      totalFinalPrice: 0,
      avgSubtotal: 0,
      avgShippingFee: 0,
      avgDiscount: 0,
      avgFinalPrice: 0,
      minFinalPrice: 0,
      maxFinalPrice: 0
    };
  }
  
  let totalSubtotal = 0;
  let totalShippingFee = 0;
  let totalDiscount = 0;
  let totalFinalPrice = 0;
  
  const finalPrices: number[] = [];
  
  for (const order of orders) {
    totalSubtotal += order.subtotal_amount;
    totalShippingFee += order.shipping_fee;
    totalDiscount += order.discount_amount;
    totalFinalPrice += order.final_price;
    finalPrices.push(order.final_price);
  }
  
  const count = orders.length;
  
  return {
    totalOrders: count,
    totalSubtotal,
    totalShippingFee,
    totalDiscount,
    totalFinalPrice,
    avgSubtotal: totalSubtotal / count,
    avgShippingFee: totalShippingFee / count,
    avgDiscount: totalDiscount / count,
    avgFinalPrice: totalFinalPrice / count,
    minFinalPrice: Math.min(...finalPrices),
    maxFinalPrice: Math.max(...finalPrices)
  };
}

/**
 * Validate that shipping fees are within expected range
 * 
 * @param orders - Array of order records
 * @returns true if all shipping fees are within 15,000 - 50,000 VND
 */
export function validateShippingFeeRange(orders: Order[]): boolean {
  for (const order of orders) {
    if (order.shipping_fee < 15000 || order.shipping_fee > 50000) {
      console.error(`Order ${order.order_id} has shipping_fee ${order.shipping_fee} outside range 15,000 - 50,000 VND`);
      return false;
    }
  }
  
  return true;
}

/**
 * Validate that discount amounts are reasonable
 * 
 * @param orders - Array of order records
 * @returns true if all discount amounts are reasonable
 */
export function validateDiscountAmounts(orders: Order[]): boolean {
  for (const order of orders) {
    // Discount should not be negative
    if (order.discount_amount < 0) {
      console.error(`Order ${order.order_id} has negative discount_amount: ${order.discount_amount}`);
      return false;
    }
    
    // Discount should not exceed subtotal + shipping fee
    if (order.discount_amount > order.subtotal_amount + order.shipping_fee) {
      console.error(`Order ${order.order_id} has discount_amount ${order.discount_amount} exceeding subtotal + shipping fee ${order.subtotal_amount + order.shipping_fee}`);
      return false;
    }
  }
  
  return true;
}

/**
 * Get orders with high discounts (discount > 30% of subtotal)
 * 
 * @param orders - Array of order records
 * @returns Orders with high discount percentages
 */
export function getOrdersWithHighDiscounts(orders: Order[]): Array<{
  order: Order;
  discountPercent: number;
}> {
  const highDiscountOrders: Array<{ order: Order; discountPercent: number }> = [];
  
  for (const order of orders) {
    if (order.subtotal_amount > 0) {
      const discountPercent = (order.discount_amount / order.subtotal_amount) * 100;
      
      if (discountPercent > 30) {
        highDiscountOrders.push({
          order,
          discountPercent
        });
      }
    }
  }
  
  return highDiscountOrders;
}

/**
 * Get orders with zero or negative final price
 * 
 * @param orders - Array of order records
 * @returns Orders with zero or negative final price
 */
export function getOrdersWithZeroOrNegativePrice(orders: Order[]): Order[] {
  return orders.filter(o => o.final_price <= 0);
}
