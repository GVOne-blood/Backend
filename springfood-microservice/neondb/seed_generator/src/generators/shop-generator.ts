/**
 * Shop Generator Module
 * 
 * Generates at least 25 shops using shop name templates.
 * Includes realistic Vietnamese food business information.
 * 
 * Requirements: 2.4, 3.5, 4.1, 4.2, 12.1, 12.2, 12.3
 */

import { IDRegistry } from '../utils/id-registry';
import { generateUUID } from '../utils/uuid-generator';
import { SHOP_NAMES, ShopTemplate } from '../templates/shops';

export interface Shop {
  shop_id: string;
  shop_name: string;
  logo: string;
  introduction: string;
  shop_status: string;
  total_product: number;
  total_sold: number;
  avg_star: number;
  email: string;
  phone_number: string;
  shop_address: string;
  city: string;
  province: string;
  created_at: Date;
  updated_at: Date;
}

/**
 * Vietnamese cities and provinces
 */
const VIETNAMESE_LOCATIONS = [
  { city: 'Quận 1', province: 'Hồ Chí Minh' },
  { city: 'Quận 3', province: 'Hồ Chí Minh' },
  { city: 'Quận 5', province: 'Hồ Chí Minh' },
  { city: 'Quận 7', province: 'Hồ Chí Minh' },
  { city: 'Quận Bình Thạnh', province: 'Hồ Chí Minh' },
  { city: 'Quận Tân Bình', province: 'Hồ Chí Minh' },
  { city: 'Quận Phú Nhuận', province: 'Hồ Chí Minh' },
  { city: 'Quận Gò Vấp', province: 'Hồ Chí Minh' },
  { city: 'Quận Hoàn Kiếm', province: 'Hà Nội' },
  { city: 'Quận Ba Đình', province: 'Hà Nội' },
  { city: 'Quận Đống Đa', province: 'Hà Nội' },
  { city: 'Quận Hai Bà Trưng', province: 'Hà Nội' },
  { city: 'Quận Cầu Giấy', province: 'Hà Nội' },
  { city: 'Quận Thanh Xuân', province: 'Hà Nội' },
  { city: 'Quận Hải Châu', province: 'Đà Nẵng' },
  { city: 'Quận Sơn Trà', province: 'Đà Nẵng' },
  { city: 'Quận Ngũ Hành Sơn', province: 'Đà Nẵng' },
  { city: 'Thành phố Nha Trang', province: 'Khánh Hòa' },
  { city: 'Thành phố Huế', province: 'Thừa Thiên Huế' },
  { city: 'Thành phố Cần Thơ', province: 'Cần Thơ' }
];

/**
 * Vietnamese street names
 */
const VIETNAMESE_STREETS = [
  'Nguyễn Huệ', 'Lê Lợi', 'Trần Hưng Đạo', 'Hai Bà Trưng',
  'Lý Thường Kiệt', 'Võ Văn Tần', 'Pasteur', 'Cách Mạng Tháng 8',
  'Điện Biên Phủ', 'Nguyễn Thị Minh Khai', 'Lê Thánh Tôn', 'Đồng Khởi',
  'Nam Kỳ Khởi Nghĩa', 'Phạm Ngũ Lão', 'Bùi Viện', 'Nguyễn Trãi',
  'Hoàng Văn Thụ', 'Phan Xích Long', 'Trường Chinh', 'Xô Viết Nghệ Tĩnh'
];

/**
 * Generate realistic shop address
 */
function generateShopAddress(): string {
  const streetNumber = Math.floor(Math.random() * 500) + 1;
  const street = VIETNAMESE_STREETS[Math.floor(Math.random() * VIETNAMESE_STREETS.length)];
  return `${streetNumber} ${street}`;
}

/**
 * Generate shop email based on shop name
 */
function generateShopEmail(shopName: string): string {
  // Convert shop name to email-friendly format
  const emailName = shopName
    .toLowerCase()
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '') // Remove diacritics
    .replace(/đ/g, 'd')
    .replace(/[^a-z0-9]/g, '')
    .substring(0, 20);
  
  return `${emailName}@springfood.vn`;
}

/**
 * Generate shop phone number
 * Format: 0xxx-xxx-xxx
 */
function generateShopPhoneNumber(): string {
  const prefixes = ['028', '024', '0236', '0258', '0254', '0292'];
  const prefix = prefixes[Math.floor(Math.random() * prefixes.length)];
  const middle = Math.floor(Math.random() * 900 + 100); // 100-999
  const last = Math.floor(Math.random() * 9000 + 1000); // 1000-9999
  
  return `${prefix}${middle}${last}`;
}

/**
 * Generate shop logo URL based on shop type
 */
function generateShopLogo(shopTemplate: ShopTemplate): string {
  // Use a placeholder logo service
  const logoId = Math.floor(Math.random() * 100) + 1;
  return `https://ui-avatars.com/api/?name=${encodeURIComponent(shopTemplate.name)}&size=200&background=random`;
}

/**
 * Generate shop introduction based on template
 */
function generateShopIntroduction(shopTemplate: ShopTemplate): string {
  const introTemplates = {
    brand: `${shopTemplate.name} - ${shopTemplate.description}. Chúng tôi cam kết mang đến cho khách hàng những sản phẩm chất lượng cao với dịch vụ tốt nhất.`,
    traditional: `${shopTemplate.name} - ${shopTemplate.description}. Với hơn 10 năm kinh nghiệm, chúng tôi tự hào phục vụ món ăn ngon, đậm đà hương vị truyền thống.`
  };
  
  return introTemplates[shopTemplate.type];
}

/**
 * Generate at least 25 shops using shop name templates
 * 
 * @param registry - ID registry to register shop IDs
 * @param count - Number of shops to generate (default: 27, all available templates)
 * @returns Array of shop records
 * 
 * Requirements:
 * - 2.4: Generate shop names that resemble Vietnamese food businesses
 * - 3.5: Generate at least 10 records for shop.shops table (we generate 25+)
 * - 4.1: Populate all non-nullable columns with valid data
 * - 4.2: Populate nullable columns with realistic data
 * - 12.1: Assign each product to exactly one shop via shop_id foreign key
 * - 12.2: Distribute products across shops with varying quantities
 * - 12.3: Ensure each shop has at least 5 products
 */
export function generateShops(registry: IDRegistry, count: number = SHOP_NAMES.length): Shop[] {
  const now = new Date();
  const shops: Shop[] = [];
  
  // Use all available shop templates (27 shops)
  const shopTemplates = SHOP_NAMES.slice(0, Math.min(count, SHOP_NAMES.length));

  for (let i = 0; i < shopTemplates.length; i++) {
    const template = shopTemplates[i];
    const shopId = generateUUID();
    const location = VIETNAMESE_LOCATIONS[i % VIETNAMESE_LOCATIONS.length];

    const shop: Shop = {
      shop_id: shopId,
      shop_name: template.name,
      logo: generateShopLogo(template),
      introduction: generateShopIntroduction(template),
      shop_status: 'ACTIVE',
      total_product: 0, // Will be updated after products are generated
      total_sold: Math.floor(Math.random() * 1000), // Random initial sales
      avg_star: parseFloat((Math.random() * 1.5 + 3.5).toFixed(2)), // 3.5 - 5.0 stars
      email: generateShopEmail(template.name),
      phone_number: generateShopPhoneNumber(),
      shop_address: generateShopAddress(),
      city: location.city,
      province: location.province,
      created_at: new Date(now.getTime() - Math.random() * 365 * 24 * 60 * 60 * 1000), // Random date within last year
      updated_at: now
    };

    shops.push(shop);

    // Register shop ID in registry
    registry.register('shops', shopId, shop);
  }

  return shops;
}

/**
 * Update shop total_product counts
 * This should be called after products are generated
 * 
 * @param shops - Array of shop records
 * @param registry - ID registry containing product data
 * 
 * Requirements:
 * - 12.4: Update total_product count in shop.shops table to match actual product count
 */
export function updateShopProductCounts(shops: Shop[], registry: IDRegistry): void {
  // Get all products from registry
  const productIds = registry.getAllIds('products');
  
  // Count products per shop
  const productCountByShop = new Map<string, number>();
  
  for (const productId of productIds) {
    const product = registry.getData('products', productId);
    if (product && product.shop_id) {
      const currentCount = productCountByShop.get(product.shop_id) || 0;
      productCountByShop.set(product.shop_id, currentCount + 1);
    }
  }
  
  // Update shop total_product counts
  for (const shop of shops) {
    shop.total_product = productCountByShop.get(shop.shop_id) || 0;
    
    // Update shop in registry
    registry.register('shops', shop.shop_id, shop);
  }
}

/**
 * Validate that at least 25 shops were generated
 */
export function validateShopCount(shops: Shop[]): boolean {
  return shops.length >= 25;
}

/**
 * Validate that all shops have ACTIVE status
 */
export function validateShopStatus(shops: Shop[]): boolean {
  return shops.every(s => s.shop_status === 'ACTIVE');
}

/**
 * Validate that all shops have realistic data
 */
export function validateShopData(shops: Shop[]): boolean {
  for (const shop of shops) {
    // Check required fields are not empty
    if (!shop.shop_name || !shop.email || !shop.phone_number) {
      return false;
    }
    
    // Check avg_star is in valid range (0-5)
    if (shop.avg_star < 0 || shop.avg_star > 5) {
      return false;
    }
    
    // Check total_product is non-negative
    if (shop.total_product < 0) {
      return false;
    }
  }
  
  return true;
}

/**
 * Get shops by province
 */
export function getShopsByProvince(shops: Shop[], province: string): Shop[] {
  return shops.filter(s => s.province === province);
}

/**
 * Get shops by city
 */
export function getShopsByCity(shops: Shop[], city: string): Shop[] {
  return shops.filter(s => s.city === city);
}
