/**
 * Unit tests for shop name templates
 * 
 * Tests:
 * - Verify minimum 25 shop names exist
 * - Verify real brands are included
 * - Verify traditional shop names are included
 * - Verify helper functions work correctly
 */

import {
  SHOP_NAMES,
  SHOP_NAME_STATS,
  getAllShopNames,
  getShopNamesByType,
  getShopNamesByCategory,
  getRandomShopName,
  getRandomShopNames
} from '../src/templates/shops';

describe('Shop Name Templates', () => {
  describe('SHOP_NAMES array', () => {
    it('should have at least 25 shop names', () => {
      expect(SHOP_NAMES.length).toBeGreaterThanOrEqual(25);
    });

    it('should include real brands', () => {
      const brandNames = SHOP_NAMES
        .filter(shop => shop.type === 'brand')
        .map(shop => shop.name);

      expect(brandNames).toContain('Gong Cha');
      expect(brandNames).toContain('The Coffee House');
      expect(brandNames).toContain('Highlands Coffee');
      expect(brandNames).toContain('Phúc Long Coffee & Tea');
    });

    it('should include traditional Vietnamese shop names', () => {
      const traditionalNames = SHOP_NAMES
        .filter(shop => shop.type === 'traditional')
        .map(shop => shop.name);

      expect(traditionalNames).toContain('Phở Hà Nội 24h');
      expect(traditionalNames).toContain('Bún Chả Hà Nội');
      expect(traditionalNames).toContain('Cơm Tấm Sài Gòn');
    });

    it('should have valid structure for each shop', () => {
      SHOP_NAMES.forEach(shop => {
        expect(shop).toHaveProperty('name');
        expect(shop).toHaveProperty('type');
        expect(shop).toHaveProperty('category');
        expect(shop).toHaveProperty('description');
        expect(typeof shop.name).toBe('string');
        expect(['brand', 'traditional']).toContain(shop.type);
        expect(typeof shop.category).toBe('string');
        expect(typeof shop.description).toBe('string');
      });
    });

    it('should have unique shop names', () => {
      const names = SHOP_NAMES.map(shop => shop.name);
      const uniqueNames = new Set(names);
      expect(uniqueNames.size).toBe(names.length);
    });
  });

  describe('getAllShopNames()', () => {
    it('should return all shop names as strings', () => {
      const names = getAllShopNames();
      expect(names.length).toBe(SHOP_NAMES.length);
      expect(names.every(name => typeof name === 'string')).toBe(true);
    });
  });

  describe('getShopNamesByType()', () => {
    it('should return only brand shops', () => {
      const brands = getShopNamesByType('brand');
      expect(brands.length).toBeGreaterThan(0);
      expect(brands).toContain('Gong Cha');
      expect(brands).toContain('The Coffee House');
    });

    it('should return only traditional shops', () => {
      const traditional = getShopNamesByType('traditional');
      expect(traditional.length).toBeGreaterThan(0);
      expect(traditional).toContain('Phở Hà Nội 24h');
      expect(traditional).toContain('Bún Chả Hà Nội');
    });

    it('should have correct counts', () => {
      const brands = getShopNamesByType('brand');
      const traditional = getShopNamesByType('traditional');
      expect(brands.length + traditional.length).toBe(SHOP_NAMES.length);
    });
  });

  describe('getShopNamesByCategory()', () => {
    it('should return shops by category', () => {
      const coffeeShops = getShopNamesByCategory('Cà Phê');
      expect(coffeeShops.length).toBeGreaterThan(0);
      expect(coffeeShops).toContain('The Coffee House');
      expect(coffeeShops).toContain('Highlands Coffee');
    });

    it('should return empty array for non-existent category', () => {
      const shops = getShopNamesByCategory('Non-existent Category');
      expect(shops).toEqual([]);
    });
  });

  describe('getRandomShopName()', () => {
    it('should return a valid shop template', () => {
      const shop = getRandomShopName();
      expect(shop).toHaveProperty('name');
      expect(shop).toHaveProperty('type');
      expect(shop).toHaveProperty('category');
      expect(shop).toHaveProperty('description');
    });

    it('should return different shops on multiple calls (probabilistic)', () => {
      const shops = new Set();
      for (let i = 0; i < 20; i++) {
        shops.add(getRandomShopName().name);
      }
      // With 27 shops, getting at least 2 different ones in 20 tries is highly probable
      expect(shops.size).toBeGreaterThan(1);
    });
  });

  describe('getRandomShopNames()', () => {
    it('should return requested number of shops', () => {
      const shops = getRandomShopNames(10);
      expect(shops.length).toBe(10);
    });

    it('should return all shops if count exceeds total', () => {
      const shops = getRandomShopNames(100);
      expect(shops.length).toBe(SHOP_NAMES.length);
    });

    it('should return unique shops (no duplicates)', () => {
      const shops = getRandomShopNames(15);
      const names = shops.map(s => s.name);
      const uniqueNames = new Set(names);
      expect(uniqueNames.size).toBe(names.length);
    });

    it('should return valid shop templates', () => {
      const shops = getRandomShopNames(5);
      shops.forEach(shop => {
        expect(shop).toHaveProperty('name');
        expect(shop).toHaveProperty('type');
        expect(shop).toHaveProperty('category');
        expect(shop).toHaveProperty('description');
      });
    });
  });

  describe('SHOP_NAME_STATS', () => {
    it('should have correct total count', () => {
      expect(SHOP_NAME_STATS.total).toBe(SHOP_NAMES.length);
    });

    it('should have correct brand count', () => {
      const actualBrands = SHOP_NAMES.filter(s => s.type === 'brand').length;
      expect(SHOP_NAME_STATS.brands).toBe(actualBrands);
    });

    it('should have correct traditional count', () => {
      const actualTraditional = SHOP_NAMES.filter(s => s.type === 'traditional').length;
      expect(SHOP_NAME_STATS.traditional).toBe(actualTraditional);
    });

    it('should have correct category count', () => {
      const actualCategories = new Set(SHOP_NAMES.map(s => s.category)).size;
      expect(SHOP_NAME_STATS.categories).toBe(actualCategories);
    });

    it('brands + traditional should equal total', () => {
      expect(SHOP_NAME_STATS.brands + SHOP_NAME_STATS.traditional).toBe(SHOP_NAME_STATS.total);
    });
  });

  describe('Requirements validation', () => {
    it('should meet Requirement 2.4: realistic Vietnamese food business names', () => {
      // Check for Vietnamese food-related keywords in shop names
      const vietnameseKeywords = ['Phở', 'Bún', 'Cơm', 'Bánh', 'Nem', 'Chè', 'Xôi', 'Cháo'];
      const hasVietnameseNames = SHOP_NAMES.some(shop =>
        vietnameseKeywords.some(keyword => shop.name.includes(keyword))
      );
      expect(hasVietnameseNames).toBe(true);
    });

    it('should meet Requirement 3.5: at least 10 shops (actually 25+)', () => {
      expect(SHOP_NAMES.length).toBeGreaterThanOrEqual(25);
    });
  });
});
