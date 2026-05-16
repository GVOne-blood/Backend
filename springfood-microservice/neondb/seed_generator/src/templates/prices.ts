/**
 * Price Range Templates for Vietnamese Food Categories
 * 
 * Defines realistic price ranges (in VND) for each food category based on
 * Vietnamese food delivery market research (GrabFood, ShopeeFood, BeFood).
 * 
 * Requirements: 2.3, 4.6
 */

export interface PriceRange {
  min: number;
  max: number;
  category: string;
}

/**
 * Price ranges by category (in VND)
 * 
 * Based on realistic Vietnamese food delivery prices:
 * - Beverages: 15k-65k VND
 * - Main dishes: 35k-85k VND
 * - Snacks: 10k-50k VND
 * - Premium items: 50k-150k VND
 */
export const PRICE_RANGES: Record<string, PriceRange> = {
  // Beverages - Đồ Uống
  'Cà Phê': { min: 15000, max: 45000, category: 'Cà Phê' },
  'Trà Sữa': { min: 25000, max: 65000, category: 'Trà Sữa' },
  'Trà Trái Cây': { min: 20000, max: 55000, category: 'Trà Trái Cây' },
  'Nước Ép': { min: 20000, max: 50000, category: 'Nước Ép' },
  'Sinh Tố': { min: 25000, max: 55000, category: 'Sinh Tố' },

  // Main Dishes - Món Chính
  'Phở': { min: 35000, max: 75000, category: 'Phở' },
  'Bún': { min: 30000, max: 70000, category: 'Bún' },
  'Cơm': { min: 35000, max: 85000, category: 'Cơm' },
  'Bánh Mì': { min: 15000, max: 35000, category: 'Bánh Mì' },
  'Xôi': { min: 15000, max: 40000, category: 'Xôi' },
  'Cháo': { min: 20000, max: 45000, category: 'Cháo' },

  // Hotpot & BBQ - Món Lẩu & Nướng
  'Lẩu': { min: 80000, max: 200000, category: 'Lẩu' },
  'Món Nướng': { min: 50000, max: 150000, category: 'Món Nướng' },
  'Thịt Nướng': { min: 45000, max: 120000, category: 'Thịt Nướng' },
  'Hải Sản Nướng': { min: 60000, max: 180000, category: 'Hải Sản Nướng' },
  'Hải Sản Nướng BBQ': { min: 60000, max: 180000, category: 'Hải Sản Nướng BBQ' },
  'Xiên Nướng': { min: 30000, max: 80000, category: 'Xiên Nướng' },

  // Seafood - Món Hải Sản
  'Món Hải Sản': { min: 50000, max: 150000, category: 'Món Hải Sản' },
  'Hải Sản Hấp': { min: 50000, max: 140000, category: 'Hải Sản Hấp' },
  'Hải Sản Chiên': { min: 45000, max: 130000, category: 'Hải Sản Chiên' },

  // Hotpot Types - Các loại Lẩu
  'Lẩu Thái': { min: 80000, max: 180000, category: 'Lẩu Thái' },
  'Lẩu Hải Sản': { min: 90000, max: 200000, category: 'Lẩu Hải Sản' },
  'Lẩu Bò': { min: 85000, max: 190000, category: 'Lẩu Bò' },

  // Desserts - Tráng Miệng
  'Chè': { min: 15000, max: 35000, category: 'Chè' },
  'Kem': { min: 20000, max: 50000, category: 'Kem' },
  'Bánh Ngọt': { min: 20000, max: 60000, category: 'Bánh Ngọt' },
  'Trái Cây Dầm': { min: 25000, max: 55000, category: 'Trái Cây Dầm' },

  // Snacks - Món Ăn Vặt
  'Món Ăn Vặt': { min: 15000, max: 50000, category: 'Món Ăn Vặt' },
  'Nem Rán': { min: 20000, max: 45000, category: 'Nem Rán' },
  'Bánh Tráng Trộn': { min: 15000, max: 35000, category: 'Bánh Tráng Trộn' },
  'Bánh Bao': { min: 15000, max: 30000, category: 'Bánh Bao' },
  'Nem & Chả': { min: 25000, max: 60000, category: 'Nem & Chả' },

  // Fast Food - Đồ Ăn Nhanh
  'Đồ Ăn Nhanh': { min: 35000, max: 90000, category: 'Đồ Ăn Nhanh' },
  'Burger': { min: 35000, max: 75000, category: 'Burger' },
  'Pizza': { min: 50000, max: 150000, category: 'Pizza' },
  'Gà Rán': { min: 40000, max: 90000, category: 'Gà Rán' },

  // Asian Cuisine - Món Á
  'Món Á': { min: 40000, max: 120000, category: 'Món Á' },
  'Sushi': { min: 50000, max: 150000, category: 'Sushi' },
  'Dimsum': { min: 35000, max: 90000, category: 'Dimsum' },
  'Mì Ý': { min: 40000, max: 100000, category: 'Mì Ý' },

  // Western Cuisine - Món Âu
  'Món Âu': { min: 60000, max: 180000, category: 'Món Âu' },
  'Pasta': { min: 50000, max: 120000, category: 'Pasta' },
  'Steak': { min: 80000, max: 200000, category: 'Steak' },
  'Salad': { min: 35000, max: 80000, category: 'Salad' },

  // Bakery - Bánh Ngọt & Bánh Mì
  'Bánh Ngọt & Bánh Mì': { min: 20000, max: 70000, category: 'Bánh Ngọt & Bánh Mì' },
  'Bánh Mì Ngọt': { min: 15000, max: 45000, category: 'Bánh Mì Ngọt' },
  'Bánh Kem': { min: 30000, max: 100000, category: 'Bánh Kem' },
  'Bánh Bông Lan': { min: 25000, max: 70000, category: 'Bánh Bông Lan' },

  // Healthy Food - Món Healthy
  'Món Healthy': { min: 40000, max: 100000, category: 'Món Healthy' },
  'Salad Healthy': { min: 35000, max: 85000, category: 'Salad Healthy' },
  'Smoothie Bowl': { min: 40000, max: 90000, category: 'Smoothie Bowl' },
  'Ức Gà': { min: 35000, max: 75000, category: 'Ức Gà' },

  // Vegetarian - Món Chay
  'Món Chay': { min: 25000, max: 65000, category: 'Món Chay' },
  'Cơm Chay': { min: 30000, max: 65000, category: 'Cơm Chay' },
  'Bún Chay': { min: 25000, max: 60000, category: 'Bún Chay' },
  'Lẩu Chay': { min: 60000, max: 150000, category: 'Lẩu Chay' },

  // Regional Specialties - Món Đặc Sản Miền
  'Món Đặc Sản Miền': { min: 35000, max: 90000, category: 'Món Đặc Sản Miền' },
  'Miền Bắc': { min: 35000, max: 85000, category: 'Miền Bắc' },
  'Miền Trung': { min: 30000, max: 80000, category: 'Miền Trung' },
  'Miền Nam': { min: 35000, max: 90000, category: 'Miền Nam' },

  // Parent Categories (fallback ranges)
  'Món Việt Truyền Thống': { min: 30000, max: 80000, category: 'Món Việt Truyền Thống' },
  'Đồ Uống': { min: 15000, max: 65000, category: 'Đồ Uống' },
  'Tráng Miệng': { min: 15000, max: 60000, category: 'Tráng Miệng' },
  'Món Nướng & BBQ': { min: 50000, max: 150000, category: 'Món Nướng & BBQ' },
  'Món Lẩu': { min: 80000, max: 200000, category: 'Món Lẩu' },
  'Món Ăn Sáng': { min: 15000, max: 45000, category: 'Món Ăn Sáng' },
  'Bánh Xèo & Bánh Cuốn': { min: 25000, max: 55000, category: 'Bánh Xèo & Bánh Cuốn' }
};

/**
 * Generate a realistic price for a given category
 * 
 * @param category - The food category name
 * @returns A random price within the category's range (in VND)
 * 
 * @example
 * generateRealisticPrice('Cà Phê') // Returns: 25000 (between 15k-45k)
 * generateRealisticPrice('Phở') // Returns: 55000 (between 35k-75k)
 * generateRealisticPrice('Lẩu') // Returns: 120000 (between 80k-200k)
 */
export function generateRealisticPrice(category: string): number {
  const priceRange = PRICE_RANGES[category];
  
  if (!priceRange) {
    // Fallback to default range if category not found
    console.warn(`Price range not found for category: ${category}. Using default range.`);
    return generateRandomPrice(20000, 60000);
  }

  return generateRandomPrice(priceRange.min, priceRange.max);
}

/**
 * Generate a random price within a range
 * Prices are rounded to nearest 1000 VND for realism
 * 
 * @param min - Minimum price (in VND)
 * @param max - Maximum price (in VND)
 * @returns A random price rounded to nearest 1000 VND
 */
function generateRandomPrice(min: number, max: number): number {
  const randomPrice = Math.floor(Math.random() * (max - min + 1)) + min;
  // Round to nearest 1000 VND (e.g., 35000, 36000, 37000)
  return Math.round(randomPrice / 1000) * 1000;
}

/**
 * Get price range for a category
 * 
 * @param category - The food category name
 * @returns The price range object or undefined if not found
 */
export function getPriceRange(category: string): PriceRange | undefined {
  return PRICE_RANGES[category];
}

/**
 * Get all categories with price ranges
 * 
 * @returns Array of category names that have defined price ranges
 */
export function getAllPriceCategories(): string[] {
  return Object.keys(PRICE_RANGES);
}

/**
 * Validate if a price is within the category's range
 * 
 * @param category - The food category name
 * @param price - The price to validate (in VND)
 * @returns True if price is within range, false otherwise
 */
export function isPriceInRange(category: string, price: number): boolean {
  const priceRange = PRICE_RANGES[category];
  if (!priceRange) return false;
  return price >= priceRange.min && price <= priceRange.max;
}

/**
 * Get price statistics
 */
export const PRICE_STATS = {
  totalCategories: Object.keys(PRICE_RANGES).length,
  lowestMinPrice: Math.min(...Object.values(PRICE_RANGES).map(r => r.min)),
  highestMaxPrice: Math.max(...Object.values(PRICE_RANGES).map(r => r.max)),
  averageMinPrice: Math.round(
    Object.values(PRICE_RANGES).reduce((sum, r) => sum + r.min, 0) / 
    Object.keys(PRICE_RANGES).length
  ),
  averageMaxPrice: Math.round(
    Object.values(PRICE_RANGES).reduce((sum, r) => sum + r.max, 0) / 
    Object.keys(PRICE_RANGES).length
  )
};

/**
 * Price range examples for documentation
 */
export const PRICE_EXAMPLES = {
  beverages: {
    'Cà Phê': '15k-45k VND',
    'Trà Sữa': '25k-65k VND',
    'Nước Ép': '20k-50k VND'
  },
  mainDishes: {
    'Phở': '35k-75k VND',
    'Bún': '30k-70k VND',
    'Cơm': '35k-85k VND'
  },
  premium: {
    'Lẩu': '80k-200k VND',
    'Steak': '80k-200k VND',
    'Món Hải Sản': '50k-150k VND'
  },
  snacks: {
    'Bánh Mì': '15k-35k VND',
    'Chè': '15k-35k VND',
    'Món Ăn Vặt': '15k-50k VND'
  }
};
