/**
 * Shop Name Templates
 * 
 * Realistic Vietnamese food business names including:
 * - Real brands (Gong Cha, The Coffee House, Highlands Coffee, Phúc Long, etc.)
 * - Traditional Vietnamese shop names
 * 
 * Requirements: 2.4, 3.5
 */

export interface ShopTemplate {
  name: string;
  type: 'brand' | 'traditional';
  category: string;
  description: string;
}

/**
 * Shop name templates (25+ realistic Vietnamese food businesses)
 */
export const SHOP_NAMES: ShopTemplate[] = [
  // Real Brands - Coffee & Tea (8 shops)
  {
    name: 'Gong Cha',
    type: 'brand',
    category: 'Trà Sữa',
    description: 'Thương hiệu trà sữa nổi tiếng từ Đài Loan'
  },
  {
    name: 'The Coffee House',
    type: 'brand',
    category: 'Cà Phê',
    description: 'Chuỗi cà phê Việt Nam phong cách hiện đại'
  },
  {
    name: 'Highlands Coffee',
    type: 'brand',
    category: 'Cà Phê',
    description: 'Chuỗi cà phê hàng đầu Việt Nam'
  },
  {
    name: 'Phúc Long Coffee & Tea',
    type: 'brand',
    category: 'Cà Phê & Trà',
    description: 'Thương hiệu cà phê và trà truyền thống Việt Nam'
  },
  {
    name: 'Ding Tea',
    type: 'brand',
    category: 'Trà Sữa',
    description: 'Thương hiệu trà sữa Đài Loan tại Việt Nam'
  },
  {
    name: 'TocoToco',
    type: 'brand',
    category: 'Trà Sữa',
    description: 'Chuỗi trà sữa phong cách trẻ trung'
  },
  {
    name: 'Mixue',
    type: 'brand',
    category: 'Trà & Kem',
    description: 'Thương hiệu trà và kem giá rẻ từ Trung Quốc'
  },
  {
    name: 'Cà Phê Cộng',
    type: 'brand',
    category: 'Cà Phê',
    description: 'Cà phê phong cách hoài cổ Việt Nam'
  },

  // Traditional Vietnamese Shops - Phở & Bún (5 shops)
  {
    name: 'Phở Hà Nội 24h',
    type: 'traditional',
    category: 'Phở',
    description: 'Phở Hà Nội truyền thống phục vụ 24/7'
  },
  {
    name: 'Phở Bò Tái Lăn',
    type: 'traditional',
    category: 'Phở',
    description: 'Chuyên phở bò tái lăn đặc biệt'
  },
  {
    name: 'Bún Chả Hà Nội',
    type: 'traditional',
    category: 'Bún',
    description: 'Bún chả Hà Nội chính gốc'
  },
  {
    name: 'Bún Bò Huế Mẹ Liên',
    type: 'traditional',
    category: 'Bún',
    description: 'Bún bò Huế chuẩn vị xứ Huế'
  },
  {
    name: 'Phở Gà Tân Định',
    type: 'traditional',
    category: 'Phở',
    description: 'Phở gà truyền thống Sài Gòn'
  },

  // Traditional Vietnamese Shops - Cơm (3 shops)
  {
    name: 'Cơm Tấm Sài Gòn',
    type: 'traditional',
    category: 'Cơm',
    description: 'Cơm tấm sườn bì chả đặc sản Sài Gòn'
  },
  {
    name: 'Cơm Niêu Singapore',
    type: 'traditional',
    category: 'Cơm',
    description: 'Cơm niêu phong cách Singapore'
  },
  {
    name: 'Cơm Gà Xối Mỡ Hội An',
    type: 'traditional',
    category: 'Cơm',
    description: 'Cơm gà Hội An truyền thống'
  },

  // Traditional Vietnamese Shops - Bánh & Tráng Miệng (3 shops)
  {
    name: 'Bánh Mì Hòa Mã',
    type: 'traditional',
    category: 'Bánh Mì',
    description: 'Bánh mì Sài Gòn nổi tiếng từ 1958'
  },
  {
    name: 'Chè Thái Lan',
    type: 'traditional',
    category: 'Chè',
    description: 'Chè Thái và các loại chè truyền thống'
  },
  {
    name: 'Kem Tràng Tiền',
    type: 'traditional',
    category: 'Kem',
    description: 'Kem Hà Nội truyền thống từ 1958'
  },

  // Traditional Vietnamese Shops - Món Nướng & BBQ (3 shops)
  {
    name: 'Quán Nướng Sài Gòn',
    type: 'traditional',
    category: 'Món Nướng',
    description: 'Các món nướng BBQ phong cách Sài Gòn'
  },
  {
    name: 'BBQ Garden',
    type: 'traditional',
    category: 'BBQ',
    description: 'Buffet nướng và lẩu không giới hạn'
  },
  {
    name: 'Hải Sản Nướng Biển Đông',
    type: 'traditional',
    category: 'Hải Sản',
    description: 'Hải sản tươi sống nướng than hoa'
  },

  // Traditional Vietnamese Shops - Món Đặc Sản (3 shops)
  {
    name: 'Nem Nướng Nha Trang',
    type: 'traditional',
    category: 'Nem',
    description: 'Nem nướng Ninh Hòa chính gốc'
  },
  {
    name: 'Bánh Xèo Miền Tây',
    type: 'traditional',
    category: 'Bánh Xèo',
    description: 'Bánh xèo miền Tây đặc sản'
  },
  {
    name: 'Lẩu Thái Hải Sản',
    type: 'traditional',
    category: 'Lẩu',
    description: 'Lẩu Thái hải sản chua cay'
  },

  // Additional Traditional Shops (2 shops to reach 27 total)
  {
    name: 'Xôi Xéo Hà Nội',
    type: 'traditional',
    category: 'Xôi',
    description: 'Xôi xéo và các loại xôi Hà Nội'
  },
  {
    name: 'Cháo Lòng Bà Hoa',
    type: 'traditional',
    category: 'Cháo',
    description: 'Cháo lòng truyền thống Sài Gòn'
  }
];

/**
 * Get all shop names
 */
export function getAllShopNames(): string[] {
  return SHOP_NAMES.map(shop => shop.name);
}

/**
 * Get shop names by type
 */
export function getShopNamesByType(type: 'brand' | 'traditional'): string[] {
  return SHOP_NAMES
    .filter(shop => shop.type === type)
    .map(shop => shop.name);
}

/**
 * Get shop names by category
 */
export function getShopNamesByCategory(category: string): string[] {
  return SHOP_NAMES
    .filter(shop => shop.category === category)
    .map(shop => shop.name);
}

/**
 * Get random shop name
 */
export function getRandomShopName(): ShopTemplate {
  const randomIndex = Math.floor(Math.random() * SHOP_NAMES.length);
  return SHOP_NAMES[randomIndex];
}

/**
 * Get random shop names (without duplicates)
 */
export function getRandomShopNames(count: number): ShopTemplate[] {
  const shuffled = [...SHOP_NAMES].sort(() => Math.random() - 0.5);
  return shuffled.slice(0, Math.min(count, SHOP_NAMES.length));
}

/**
 * Shop name statistics
 */
export const SHOP_NAME_STATS = {
  total: SHOP_NAMES.length,
  brands: SHOP_NAMES.filter(s => s.type === 'brand').length,
  traditional: SHOP_NAMES.filter(s => s.type === 'traditional').length,
  categories: [...new Set(SHOP_NAMES.map(s => s.category))].length
};
