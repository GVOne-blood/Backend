/**
 * Sales/Promotion Campaign Templates
 * 
 * Realistic Vietnamese food delivery promotion campaigns with proper
 * discount ranges, durations, and conditions.
 */

export interface SaleCampaign {
  name: string;
  description: string;
  discount_percentage: number;
  duration_hours?: number;
  duration_days?: number;
  conditions: string;
  category: SaleCategory;
}

export type SaleCategory = 
  | 'flash_sale'
  | 'seasonal'
  | 'category_specific'
  | 'time_based'
  | 'new_customer'
  | 'combo'
  | 'shop_opening'
  | 'loyalty'
  | 'free_shipping'
  | 'weekend'
  | 'holiday';

/**
 * Sales Campaign Templates (15-20 campaigns)
 * Covers all major promotion types in Vietnamese food delivery
 */
export const SALES_CAMPAIGNS: SaleCampaign[] = [
  // ============================================
  // FLASH SALES (30-50% discount, 2-3 hours)
  // ============================================
  {
    name: 'Flash Sale Cuối Tuần',
    description: 'Giảm giá sốc các món ăn yêu thích, chỉ trong 3 giờ!',
    discount_percentage: 40.00,
    duration_hours: 3,
    conditions: 'Giới hạn 100 suất đầu tiên',
    category: 'flash_sale'
  },
  {
    name: 'Flash Sale Giờ Vàng',
    description: 'Ưu đãi đặc biệt từ 11h-13h mỗi ngày',
    discount_percentage: 35.00,
    duration_hours: 2,
    conditions: 'Áp dụng cho đơn từ 50k',
    category: 'flash_sale'
  },
  {
    name: 'Flash Sale Đêm Khuya',
    description: 'Giảm giá cực sốc cho đơn đặt từ 21h-23h',
    discount_percentage: 45.00,
    duration_hours: 2,
    conditions: 'Chỉ áp dụng từ 21h-23h, giới hạn 50 suất',
    category: 'flash_sale'
  },

  // ============================================
  // SEASONAL SALES (15-30% discount, 7-30 days)
  // ============================================
  {
    name: 'Khuyến Mãi Mùa Hè',
    description: 'Giảm giá các món đồ uống mát lạnh suốt mùa hè',
    discount_percentage: 20.00,
    duration_days: 30,
    conditions: 'Áp dụng cho tất cả đồ uống',
    category: 'seasonal'
  },
  {
    name: 'Ưu Đãi Tết Nguyên Đán',
    description: 'Mừng xuân mới, giảm giá đặc biệt cho tất cả món ăn',
    discount_percentage: 25.00,
    duration_days: 7,
    conditions: 'Áp dụng cho đơn từ 100k',
    category: 'seasonal'
  },
  {
    name: 'Khuyến Mãi Mùa Thu',
    description: 'Đón thu về với ưu đãi hấp dẫn',
    discount_percentage: 18.00,
    duration_days: 21,
    conditions: 'Áp dụng cho tất cả món ăn',
    category: 'seasonal'
  },

  // ============================================
  // CATEGORY-SPECIFIC SALES (10-25% discount)
  // ============================================
  {
    name: 'Tuần Lễ Trà Sữa',
    description: 'Giảm giá tất cả các loại trà sữa trong 7 ngày',
    discount_percentage: 15.00,
    duration_days: 7,
    conditions: 'Chỉ áp dụng cho trà sữa',
    category: 'category_specific'
  },
  {
    name: 'Ngày Phở Việt Nam',
    description: 'Ưu đãi đặc biệt cho tất cả món phở nhân ngày Phở Việt Nam',
    discount_percentage: 20.00,
    duration_days: 1,
    conditions: 'Áp dụng cho tất cả món phở',
    category: 'category_specific'
  },
  {
    name: 'Lễ Hội Cà Phê',
    description: 'Giảm giá tất cả đồ uống cà phê',
    discount_percentage: 18.00,
    duration_days: 5,
    conditions: 'Chỉ áp dụng cho cà phê',
    category: 'category_specific'
  },
  {
    name: 'Tuần Lễ Món Nướng',
    description: 'Ưu đãi đặc biệt cho tất cả món nướng và BBQ',
    discount_percentage: 22.00,
    duration_days: 7,
    conditions: 'Áp dụng cho món nướng và BBQ',
    category: 'category_specific'
  },

  // ============================================
  // TIME-BASED SALES (20-35% discount)
  // ============================================
  {
    name: 'Ưu Đãi Bữa Sáng',
    description: 'Giảm giá các món ăn sáng từ 6h-9h mỗi ngày',
    discount_percentage: 25.00,
    duration_days: 30,
    conditions: 'Chỉ áp dụng từ 6h-9h sáng',
    category: 'time_based'
  },
  {
    name: 'Happy Hour',
    description: 'Giảm giá đồ uống từ 14h-16h hàng ngày',
    discount_percentage: 30.00,
    duration_days: 30,
    conditions: 'Chỉ áp dụng từ 14h-16h',
    category: 'time_based'
  },

  // ============================================
  // NEW CUSTOMER SALES (40-50% discount)
  // ============================================
  {
    name: 'Ưu Đãi Khách Hàng Mới',
    description: 'Giảm giá đơn hàng đầu tiên cho khách hàng mới',
    discount_percentage: 50.00,
    duration_days: 365,
    conditions: 'Chỉ áp dụng cho đơn hàng đầu tiên',
    category: 'new_customer'
  },

  // ============================================
  // COMBO/BUNDLE SALES (25-40% discount)
  // ============================================
  {
    name: 'Combo Tiết Kiệm',
    description: 'Mua 2 tặng 1 cho các món đồ uống',
    discount_percentage: 33.00,
    duration_days: 14,
    conditions: 'Mua 2 món bất kỳ, tặng 1 món rẻ nhất',
    category: 'combo'
  },
  {
    name: 'Combo Gia Đình',
    description: 'Giảm giá khi đặt combo từ 4 món trở lên',
    discount_percentage: 28.00,
    duration_days: 30,
    conditions: 'Áp dụng cho đơn có từ 4 món trở lên',
    category: 'combo'
  },

  // ============================================
  // LOYALTY SALES (15-25% discount)
  // ============================================
  {
    name: 'Ưu Đãi Khách Hàng Thân Thiết',
    description: 'Giảm giá đặc biệt cho khách hàng VIP',
    discount_percentage: 20.00,
    duration_days: 90,
    conditions: 'Chỉ áp dụng cho khách hàng có từ 10 đơn trở lên',
    category: 'loyalty'
  },

  // ============================================
  // WEEKEND SALES (10-20% discount)
  // ============================================
  {
    name: 'Cuối Tuần Vui Vẻ',
    description: 'Giảm giá tất cả món vào cuối tuần',
    discount_percentage: 15.00,
    duration_days: 2,
    conditions: 'Chỉ áp dụng thứ 7 và chủ nhật',
    category: 'weekend'
  },

  // ============================================
  // HOLIDAY SALES (20-35% discount)
  // ============================================
  {
    name: 'Khuyến Mãi Quốc Khánh',
    description: 'Mừng ngày Quốc Khánh 2/9 với ưu đãi đặc biệt',
    discount_percentage: 29.00,
    duration_days: 3,
    conditions: 'Áp dụng cho tất cả món',
    category: 'holiday'
  },
  {
    name: 'Giáng Sinh An Lành',
    description: 'Giảm giá mừng Giáng Sinh',
    discount_percentage: 24.00,
    duration_days: 5,
    conditions: 'Áp dụng cho đơn từ 80k',
    category: 'holiday'
  }
];

/**
 * Sales Distribution Strategy
 * Defines how sales should be distributed across statuses and products
 */
export const SALES_DISTRIBUTION = {
  // Total sales to generate
  totalSales: { min: 15, max: 20 },
  
  // Status distribution (percentages)
  statusDistribution: {
    active: 0.35,    // 35% currently active
    upcoming: 0.25,  // 25% starting soon
    expired: 0.40    // 40% past sales (for history)
  },
  
  // Number of products per sale based on category
  productsPerSale: {
    flash_sale: { min: 10, max: 20 },
    seasonal: { min: 20, max: 40 },
    category_specific: { min: 5, max: 15 },
    time_based: { min: 10, max: 25 },
    new_customer: { min: 30, max: 50 },
    combo: { min: 5, max: 10 },
    shop_opening: { min: 5, max: 15 },
    loyalty: { min: 20, max: 40 },
    free_shipping: { min: 0, max: 0 }, // No product assignment needed
    weekend: { min: 15, max: 30 },
    holiday: { min: 25, max: 50 }
  },
  
  // Discount ranges per category (for validation)
  discountRanges: {
    flash_sale: { min: 30, max: 50 },
    seasonal: { min: 15, max: 30 },
    category_specific: { min: 10, max: 25 },
    time_based: { min: 20, max: 35 },
    new_customer: { min: 40, max: 50 },
    combo: { min: 25, max: 40 },
    shop_opening: { min: 30, max: 40 },
    loyalty: { min: 15, max: 25 },
    free_shipping: { min: 0, max: 0 },
    weekend: { min: 10, max: 20 },
    holiday: { min: 20, max: 35 }
  }
};

/**
 * Category keywords for matching sales to products
 * Used to filter products for category-specific sales
 */
export const CATEGORY_KEYWORDS: Record<string, string[]> = {
  'Trà Sữa': ['trà sữa', 'tra sua', 'milk tea'],
  'Phở': ['phở', 'pho'],
  'Cà Phê': ['cà phê', 'ca phe', 'coffee', 'espresso', 'cappuccino', 'latte'],
  'Đồ Uống': ['đồ uống', 'do uong', 'drink', 'nước', 'nuoc', 'sinh tố', 'nước ép'],
  'Món Ăn Sáng': ['ăn sáng', 'an sang', 'breakfast', 'bánh mì', 'xôi', 'cháo'],
  'Món Nướng': ['nướng', 'nuong', 'bbq', 'grill', 'xiên'],
  'Bún': ['bún', 'bun'],
  'Cơm': ['cơm', 'com', 'rice'],
  'Lẩu': ['lẩu', 'lau', 'hotpot'],
  'Hải Sản': ['hải sản', 'hai san', 'seafood', 'tôm', 'cua', 'mực', 'cá']
};

/**
 * Helper function to get random campaign from a specific category
 */
export function getRandomCampaignByCategory(category: SaleCategory): SaleCampaign {
  const campaigns = SALES_CAMPAIGNS.filter(c => c.category === category);
  return campaigns[Math.floor(Math.random() * campaigns.length)];
}

/**
 * Helper function to get all campaigns of a specific category
 */
export function getCampaignsByCategory(category: SaleCategory): SaleCampaign[] {
  return SALES_CAMPAIGNS.filter(c => c.category === category);
}

/**
 * Helper function to validate discount percentage is within range for category
 */
export function isValidDiscount(category: SaleCategory, discount: number): boolean {
  const range = SALES_DISTRIBUTION.discountRanges[category];
  return discount >= range.min && discount <= range.max;
}

/**
 * Helper function to extract category from sale name
 * Used for category-specific sales to filter products
 */
export function extractCategoryFromSaleName(saleName: string): string {
  const lowerName = saleName.toLowerCase();
  
  for (const [category, keywords] of Object.entries(CATEGORY_KEYWORDS)) {
    if (keywords.some(kw => lowerName.includes(kw))) {
      return category;
    }
  }
  
  return 'Tất Cả'; // Default: all categories
}
