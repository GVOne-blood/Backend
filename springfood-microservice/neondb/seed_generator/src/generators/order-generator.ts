/**
 * Order Generator Module
 * 
 * Generates at least 10 orders assigned to CUSTOMER users.
 * Sets realistic order_status and payment_status distributions.
 * Generates Vietnamese shipping addresses.
 * 
 * Requirements: 2.7, 3.6, 4.4, 13.8
 */

import { IDRegistry } from '../utils/id-registry';
import { generateUUID } from '../utils/uuid-generator';
import { User } from './user-generator';

export interface Order {
  order_id: string;
  user_id: string;
  shop_id: string;
  order_status: 'PENDING' | 'CONFIRMED' | 'PREPARING' | 'DELIVERING' | 'DELIVERED' | 'CANCELLED';
  payment_status: 'PENDING' | 'PAID' | 'FAILED';
  subtotal_amount: number; // Will be calculated after order_items
  shipping_fee: number;
  discount_amount: number;
  final_price: number; // Will be calculated after order_items
  shipping_address_street: string;
  shipping_address_ward: string;
  shipping_address_district: string;
  shipping_address_city: string;
  shipping_address_province: string;
  receiver_name: string;
  receiver_phone: string;
  note: string | null;
  created_at: Date;
  updated_at: Date;
}

/**
 * Order status distribution
 * - 10% PENDING
 * - 15% CONFIRMED
 * - 10% PREPARING
 * - 15% DELIVERING
 * - 45% DELIVERED
 * - 5% CANCELLED
 */
const ORDER_STATUS_DISTRIBUTION = {
  PENDING: 0.10,
  CONFIRMED: 0.15,
  PREPARING: 0.10,
  DELIVERING: 0.15,
  DELIVERED: 0.45,
  CANCELLED: 0.05
};

/**
 * Payment status distribution
 * - 10% PENDING
 * - 85% PAID
 * - 5% FAILED
 */
const PAYMENT_STATUS_DISTRIBUTION = {
  PENDING: 0.10,
  PAID: 0.85,
  FAILED: 0.05
};

/**
 * Vietnamese address templates
 */
const VIETNAMESE_ADDRESSES = {
  streets: [
    'Nguyễn Huệ',
    'Lê Lợi',
    'Trần Hưng Đạo',
    'Hai Bà Trưng',
    'Lý Thường Kiệt',
    'Võ Văn Tần',
    'Điện Biên Phủ',
    'Cách Mạng Tháng 8',
    'Nguyễn Thị Minh Khai',
    'Pasteur',
    'Nam Kỳ Khởi Nghĩa',
    'Lê Thánh Tôn',
    'Đồng Khởi',
    'Nguyễn Trãi',
    'Hoàng Văn Thụ'
  ],
  wards: [
    'Phường Bến Nghé',
    'Phường Bến Thành',
    'Phường Đa Kao',
    'Phường Nguyễn Thái Bình',
    'Phường Phạm Ngũ Lão',
    'Phường Cầu Ông Lãnh',
    'Phường Cô Giang',
    'Phường Nguyễn Cư Trinh',
    'Phường Tân Định',
    'Phường Đakao',
    'Phường Võ Thị Sáu',
    'Phường Phường 1',
    'Phường Phường 2',
    'Phường Phường 3',
    'Phường Phường 4'
  ],
  districts: [
    'Quận 1',
    'Quận 3',
    'Quận 5',
    'Quận 10',
    'Quận Bình Thạnh',
    'Quận Phú Nhuận',
    'Quận Tân Bình',
    'Quận Gò Vấp',
    'Quận Thủ Đức',
    'Quận 2',
    'Quận 4',
    'Quận 7',
    'Quận 8',
    'Quận 11',
    'Quận 12'
  ],
  cities: [
    'Thành phố Hồ Chí Minh',
    'Hà Nội',
    'Đà Nẵng',
    'Cần Thơ',
    'Hải Phòng',
    'Biên Hòa',
    'Nha Trang',
    'Huế',
    'Vũng Tàu',
    'Buôn Ma Thuột'
  ],
  provinces: [
    'Hồ Chí Minh',
    'Hà Nội',
    'Đà Nẵng',
    'Cần Thơ',
    'Hải Phòng',
    'Đồng Nai',
    'Khánh Hòa',
    'Thừa Thiên Huế',
    'Bà Rịa - Vũng Tàu',
    'Đắk Lắk'
  ]
};

/**
 * Order notes templates
 */
const ORDER_NOTES = [
  'Giao hàng trước 12h',
  'Không gọi chuông, nhắn tin',
  'Giao tận tay, không gửi bảo vệ',
  'Gọi điện trước khi giao',
  'Để hàng ở bảo vệ',
  null, // No note
  null,
  null,
  'Giao hàng giờ hành chính',
  'Không cay'
];

/**
 * Generate order status based on distribution
 */
function generateOrderStatus(): 'PENDING' | 'CONFIRMED' | 'PREPARING' | 'DELIVERING' | 'DELIVERED' | 'CANCELLED' {
  const random = Math.random();
  let cumulative = 0;
  
  for (const [status, probability] of Object.entries(ORDER_STATUS_DISTRIBUTION)) {
    cumulative += probability;
    if (random < cumulative) {
      return status as 'PENDING' | 'CONFIRMED' | 'PREPARING' | 'DELIVERING' | 'DELIVERED' | 'CANCELLED';
    }
  }
  
  return 'DELIVERED'; // Fallback
}

/**
 * Generate payment status based on distribution
 */
function generatePaymentStatus(): 'PENDING' | 'PAID' | 'FAILED' {
  const random = Math.random();
  let cumulative = 0;
  
  for (const [status, probability] of Object.entries(PAYMENT_STATUS_DISTRIBUTION)) {
    cumulative += probability;
    if (random < cumulative) {
      return status as 'PENDING' | 'PAID' | 'FAILED';
    }
  }
  
  return 'PAID'; // Fallback
}

/**
 * Generate shipping fee (15,000 - 50,000 VND)
 */
function generateShippingFee(): number {
  return Math.floor(Math.random() * (50000 - 15000 + 1)) + 15000;
}

/**
 * Generate discount amount (0 - 50,000 VND)
 * 70% of orders have no discount
 */
function generateDiscountAmount(): number {
  const random = Math.random();
  
  if (random < 0.70) {
    return 0; // No discount
  } else {
    return Math.floor(Math.random() * 50000);
  }
}

/**
 * Generate Vietnamese shipping address
 */
function generateShippingAddress(): {
  street: string;
  ward: string;
  district: string;
  city: string;
  province: string;
} {
  const street = VIETNAMESE_ADDRESSES.streets[Math.floor(Math.random() * VIETNAMESE_ADDRESSES.streets.length)];
  const ward = VIETNAMESE_ADDRESSES.wards[Math.floor(Math.random() * VIETNAMESE_ADDRESSES.wards.length)];
  const district = VIETNAMESE_ADDRESSES.districts[Math.floor(Math.random() * VIETNAMESE_ADDRESSES.districts.length)];
  const cityIndex = Math.floor(Math.random() * VIETNAMESE_ADDRESSES.cities.length);
  const city = VIETNAMESE_ADDRESSES.cities[cityIndex];
  const province = VIETNAMESE_ADDRESSES.provinces[cityIndex];
  
  return {
    street: `${Math.floor(Math.random() * 500) + 1} ${street}`,
    ward,
    district,
    city,
    province
  };
}

/**
 * Generate order note
 */
function generateOrderNote(): string | null {
  return ORDER_NOTES[Math.floor(Math.random() * ORDER_NOTES.length)];
}

/**
 * Generate realistic phone number
 */
function generatePhoneNumber(): string {
  const prefixes = ['090', '091', '093', '094', '097', '098', '086', '096', '032', '033', '034', '035', '036', '037', '038', '039'];
  const prefix = prefixes[Math.floor(Math.random() * prefixes.length)];
  const middle = Math.floor(Math.random() * 900 + 100); // 100-999
  const last = Math.floor(Math.random() * 9000 + 1000); // 1000-9999
  
  return `${prefix}${middle}${last}`;
}

/**
 * Generate at least 10 orders
 * 
 * @param registry - ID registry containing users and shops
 * @param count - Number of orders to generate (default: 10)
 * @returns Array of order records
 * 
 * Requirements:
 * - 2.7: Generate order data with realistic order statuses
 * - 3.6: Generate at least 10 records for order.orders table
 * - 4.4: Generate timestamp fields within last 6 months
 * - 13.8: Ensure all orders belong to users with CUSTOMER role
 */
export function generateOrders(registry: IDRegistry, count: number = 10): Order[] {
  const now = new Date();
  const orders: Order[] = [];
  
  // Get all users and filter for CUSTOMER role
  const users = registry.getAllData('users') as User[];
  const customers = users.filter(u => u.role === 'CUSTOMER');
  
  if (customers.length === 0) {
    throw new Error('No customers found in registry. Generate users before generating orders.');
  }
  
  // Get all shop IDs
  const shopIds = registry.getAllIds('shops');
  
  if (shopIds.length === 0) {
    throw new Error('No shops found in registry. Generate shops before generating orders.');
  }

  for (let i = 0; i < count; i++) {
    const orderId = generateUUID();
    
    // Assign order to a random customer
    const customer = customers[Math.floor(Math.random() * customers.length)];
    
    // Assign order to a random shop
    const shopId = shopIds[Math.floor(Math.random() * shopIds.length)];
    
    // Generate order status and payment status
    const orderStatus = generateOrderStatus();
    const paymentStatus = generatePaymentStatus();
    
    // Generate shipping address
    const address = generateShippingAddress();
    
    // Generate created_at within last 6 months
    const sixMonthsAgo = new Date(now.getTime() - 180 * 24 * 60 * 60 * 1000);
    const createdAt = new Date(
      sixMonthsAgo.getTime() + Math.random() * (now.getTime() - sixMonthsAgo.getTime())
    );
    
    const order: Order = {
      order_id: orderId,
      user_id: customer.user_id,
      shop_id: shopId,
      order_status: orderStatus,
      payment_status: paymentStatus,
      subtotal_amount: 0, // Will be calculated after order_items
      shipping_fee: generateShippingFee(),
      discount_amount: generateDiscountAmount(),
      final_price: 0, // Will be calculated after order_items
      shipping_address_street: address.street,
      shipping_address_ward: address.ward,
      shipping_address_district: address.district,
      shipping_address_city: address.city,
      shipping_address_province: address.province,
      receiver_name: `${customer.first_name} ${customer.last_name}`,
      receiver_phone: generatePhoneNumber(),
      note: generateOrderNote(),
      created_at: createdAt,
      updated_at: createdAt
    };

    orders.push(order);

    // Register order ID in registry
    registry.register('orders', orderId, order);
  }

  return orders;
}

/**
 * Update order totals after order_items are generated
 * 
 * @param orders - Array of order records
 * @param orderItemsMap - Map of order_id to array of order items with subtotals
 * 
 * Requirements:
 * - 13.5: Calculate subtotal_amount as sum of (quantity * price_at_booking) from order_items
 * - 13.6: Generate realistic shipping_fee values (15,000 VND to 50,000 VND)
 * - 13.7: Calculate final_price as subtotal_amount + shipping_fee - discount_amount
 */
export function updateOrderTotals(
  orders: Order[],
  orderItemsMap: Map<string, Array<{ subtotal: number }>>
): void {
  for (const order of orders) {
    const items = orderItemsMap.get(order.order_id) || [];
    
    // Calculate subtotal_amount
    order.subtotal_amount = items.reduce((sum, item) => sum + item.subtotal, 0);
    
    // Calculate final_price
    order.final_price = order.subtotal_amount + order.shipping_fee - order.discount_amount;
    
    // Ensure final_price is not negative
    if (order.final_price < 0) {
      order.final_price = 0;
    }
  }
}

/**
 * Validate that at least 10 orders were generated
 */
export function validateOrderCount(orders: Order[]): boolean {
  return orders.length >= 10;
}

/**
 * Validate that all orders belong to users with CUSTOMER role
 */
export function validateOrderCustomers(orders: Order[], registry: IDRegistry): boolean {
  const users = registry.getAllData('users') as User[];
  const userMap = new Map<string, User>();
  for (const user of users) {
    userMap.set(user.user_id, user);
  }
  
  for (const order of orders) {
    const user = userMap.get(order.user_id);
    
    if (!user) {
      console.error(`Order ${order.order_id} references non-existent user ${order.user_id}`);
      return false;
    }
    
    if (user.role !== 'CUSTOMER') {
      console.error(`Order ${order.order_id} belongs to user ${order.user_id} with role ${user.role}, expected CUSTOMER`);
      return false;
    }
  }
  
  return true;
}

/**
 * Validate order status distribution
 */
export function validateOrderStatusDistribution(orders: Order[]): boolean {
  const total = orders.length;
  const statusCounts: Record<string, number> = {
    PENDING: 0,
    CONFIRMED: 0,
    PREPARING: 0,
    DELIVERING: 0,
    DELIVERED: 0,
    CANCELLED: 0
  };
  
  for (const order of orders) {
    statusCounts[order.order_status]++;
  }
  
  // Allow 15% tolerance for each status
  for (const [status, expectedPercent] of Object.entries(ORDER_STATUS_DISTRIBUTION)) {
    const actualPercent = statusCounts[status] / total;
    const tolerance = 0.15;
    
    if (actualPercent < expectedPercent - tolerance || actualPercent > expectedPercent + tolerance) {
      console.warn(`Order status ${status}: expected ${expectedPercent * 100}%, got ${actualPercent * 100}%`);
    }
  }
  
  return true;
}

/**
 * Validate payment status distribution
 */
export function validatePaymentStatusDistribution(orders: Order[]): boolean {
  const total = orders.length;
  const statusCounts: Record<string, number> = {
    PENDING: 0,
    PAID: 0,
    FAILED: 0
  };
  
  for (const order of orders) {
    statusCounts[order.payment_status]++;
  }
  
  // Allow 15% tolerance for each status
  for (const [status, expectedPercent] of Object.entries(PAYMENT_STATUS_DISTRIBUTION)) {
    const actualPercent = statusCounts[status] / total;
    const tolerance = 0.15;
    
    if (actualPercent < expectedPercent - tolerance || actualPercent > expectedPercent + tolerance) {
      console.warn(`Payment status ${status}: expected ${expectedPercent * 100}%, got ${actualPercent * 100}%`);
    }
  }
  
  return true;
}

/**
 * Validate shipping fees are within range
 */
export function validateShippingFees(orders: Order[]): boolean {
  return orders.every(o => o.shipping_fee >= 15000 && o.shipping_fee <= 50000);
}

/**
 * Validate order totals are calculated correctly
 */
export function validateOrderTotals(orders: Order[]): boolean {
  for (const order of orders) {
    const expectedFinalPrice = order.subtotal_amount + order.shipping_fee - order.discount_amount;
    const actualFinalPrice = order.final_price;
    
    // Allow for rounding errors
    if (Math.abs(expectedFinalPrice - actualFinalPrice) > 1) {
      console.error(`Order ${order.order_id} has incorrect final_price: expected ${expectedFinalPrice}, got ${actualFinalPrice}`);
      return false;
    }
  }
  
  return true;
}

/**
 * Get orders by status
 */
export function getOrdersByStatus(
  orders: Order[],
  status: 'PENDING' | 'CONFIRMED' | 'PREPARING' | 'DELIVERING' | 'DELIVERED' | 'CANCELLED'
): Order[] {
  return orders.filter(o => o.order_status === status);
}

/**
 * Get orders by payment status
 */
export function getOrdersByPaymentStatus(
  orders: Order[],
  status: 'PENDING' | 'PAID' | 'FAILED'
): Order[] {
  return orders.filter(o => o.payment_status === status);
}

/**
 * Get orders by customer
 */
export function getOrdersByCustomer(orders: Order[], userId: string): Order[] {
  return orders.filter(o => o.user_id === userId);
}

/**
 * Get orders by shop
 */
export function getOrdersByShop(orders: Order[], shopId: string): Order[] {
  return orders.filter(o => o.shop_id === shopId);
}

/**
 * Get order statistics
 */
export function getOrderStats(orders: Order[]): {
  total: number;
  byStatus: Record<string, number>;
  byPaymentStatus: Record<string, number>;
  avgSubtotal: number;
  avgShippingFee: number;
  avgDiscount: number;
  avgFinalPrice: number;
} {
  const byStatus: Record<string, number> = {};
  const byPaymentStatus: Record<string, number> = {};
  
  let totalSubtotal = 0;
  let totalShippingFee = 0;
  let totalDiscount = 0;
  let totalFinalPrice = 0;
  
  for (const order of orders) {
    byStatus[order.order_status] = (byStatus[order.order_status] || 0) + 1;
    byPaymentStatus[order.payment_status] = (byPaymentStatus[order.payment_status] || 0) + 1;
    
    totalSubtotal += order.subtotal_amount;
    totalShippingFee += order.shipping_fee;
    totalDiscount += order.discount_amount;
    totalFinalPrice += order.final_price;
  }
  
  const count = orders.length;
  
  return {
    total: count,
    byStatus,
    byPaymentStatus,
    avgSubtotal: count > 0 ? totalSubtotal / count : 0,
    avgShippingFee: count > 0 ? totalShippingFee / count : 0,
    avgDiscount: count > 0 ? totalDiscount / count : 0,
    avgFinalPrice: count > 0 ? totalFinalPrice / count : 0
  };
}
