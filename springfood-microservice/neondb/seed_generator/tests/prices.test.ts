/**
 * Unit tests for price range templates
 */

import {
  generateRealisticPrice,
  getPriceRange,
  getAllPriceCategories,
  isPriceInRange,
  PRICE_RANGES,
  PRICE_STATS
} from '../src/templates/prices';

describe('Price Range Templates', () => {
  describe('generateRealisticPrice', () => {
    it('should generate price within range for Cà Phê (15k-45k)', () => {
      for (let i = 0; i < 100; i++) {
        const price = generateRealisticPrice('Cà Phê');
        expect(price).toBeGreaterThanOrEqual(15000);
        expect(price).toBeLessThanOrEqual(45000);
        // Should be rounded to nearest 1000
        expect(price % 1000).toBe(0);
      }
    });

    it('should generate price within range for Trà Sữa (25k-65k)', () => {
      for (let i = 0; i < 100; i++) {
        const price = generateRealisticPrice('Trà Sữa');
        expect(price).toBeGreaterThanOrEqual(25000);
        expect(price).toBeLessThanOrEqual(65000);
        expect(price % 1000).toBe(0);
      }
    });

    it('should generate price within range for Phở (35k-75k)', () => {
      for (let i = 0; i < 100; i++) {
        const price = generateRealisticPrice('Phở');
        expect(price).toBeGreaterThanOrEqual(35000);
        expect(price).toBeLessThanOrEqual(75000);
        expect(price % 1000).toBe(0);
      }
    });

    it('should generate price within range for Cơm (35k-85k)', () => {
      for (let i = 0; i < 100; i++) {
        const price = generateRealisticPrice('Cơm');
        expect(price).toBeGreaterThanOrEqual(35000);
        expect(price).toBeLessThanOrEqual(85000);
        expect(price % 1000).toBe(0);
      }
    });

    it('should generate price within range for Lẩu (80k-200k)', () => {
      for (let i = 0; i < 100; i++) {
        const price = generateRealisticPrice('Lẩu');
        expect(price).toBeGreaterThanOrEqual(80000);
        expect(price).toBeLessThanOrEqual(200000);
        expect(price % 1000).toBe(0);
      }
    });

    it('should generate price within range for Món Hải Sản (50k-150k)', () => {
      for (let i = 0; i < 100; i++) {
        const price = generateRealisticPrice('Món Hải Sản');
        expect(price).toBeGreaterThanOrEqual(50000);
        expect(price).toBeLessThanOrEqual(150000);
        expect(price % 1000).toBe(0);
      }
    });

    it('should use fallback range for unknown category', () => {
      const consoleWarnSpy = jest.spyOn(console, 'warn').mockImplementation();
      
      const price = generateRealisticPrice('Unknown Category');
      
      expect(price).toBeGreaterThanOrEqual(20000);
      expect(price).toBeLessThanOrEqual(60000);
      expect(price % 1000).toBe(0);
      expect(consoleWarnSpy).toHaveBeenCalledWith(
        expect.stringContaining('Price range not found for category: Unknown Category')
      );
      
      consoleWarnSpy.mockRestore();
    });

    it('should always round to nearest 1000 VND', () => {
      const categories = ['Cà Phê', 'Phở', 'Bún', 'Cơm', 'Trà Sữa'];
      
      categories.forEach(category => {
        for (let i = 0; i < 50; i++) {
          const price = generateRealisticPrice(category);
          expect(price % 1000).toBe(0);
        }
      });
    });
  });

  describe('getPriceRange', () => {
    it('should return correct price range for Cà Phê', () => {
      const range = getPriceRange('Cà Phê');
      expect(range).toEqual({
        min: 15000,
        max: 45000,
        category: 'Cà Phê'
      });
    });

    it('should return correct price range for Phở', () => {
      const range = getPriceRange('Phở');
      expect(range).toEqual({
        min: 35000,
        max: 75000,
        category: 'Phở'
      });
    });

    it('should return correct price range for Lẩu', () => {
      const range = getPriceRange('Lẩu');
      expect(range).toEqual({
        min: 80000,
        max: 200000,
        category: 'Lẩu'
      });
    });

    it('should return undefined for unknown category', () => {
      const range = getPriceRange('Unknown Category');
      expect(range).toBeUndefined();
    });
  });

  describe('getAllPriceCategories', () => {
    it('should return all category names', () => {
      const categories = getAllPriceCategories();
      
      expect(categories).toContain('Cà Phê');
      expect(categories).toContain('Trà Sữa');
      expect(categories).toContain('Phở');
      expect(categories).toContain('Bún');
      expect(categories).toContain('Cơm');
      expect(categories).toContain('Lẩu');
      expect(categories).toContain('Món Hải Sản');
    });

    it('should return correct number of categories', () => {
      const categories = getAllPriceCategories();
      expect(categories.length).toBeGreaterThan(50); // We have 60+ categories
    });
  });

  describe('isPriceInRange', () => {
    it('should return true for price within Cà Phê range', () => {
      expect(isPriceInRange('Cà Phê', 15000)).toBe(true);
      expect(isPriceInRange('Cà Phê', 30000)).toBe(true);
      expect(isPriceInRange('Cà Phê', 45000)).toBe(true);
    });

    it('should return false for price outside Cà Phê range', () => {
      expect(isPriceInRange('Cà Phê', 14000)).toBe(false);
      expect(isPriceInRange('Cà Phê', 46000)).toBe(false);
      expect(isPriceInRange('Cà Phê', 100000)).toBe(false);
    });

    it('should return true for price within Phở range', () => {
      expect(isPriceInRange('Phở', 35000)).toBe(true);
      expect(isPriceInRange('Phở', 55000)).toBe(true);
      expect(isPriceInRange('Phở', 75000)).toBe(true);
    });

    it('should return false for price outside Phở range', () => {
      expect(isPriceInRange('Phở', 30000)).toBe(false);
      expect(isPriceInRange('Phở', 80000)).toBe(false);
    });

    it('should return false for unknown category', () => {
      expect(isPriceInRange('Unknown Category', 50000)).toBe(false);
    });
  });

  describe('PRICE_RANGES', () => {
    it('should have all major beverage categories', () => {
      expect(PRICE_RANGES['Cà Phê']).toBeDefined();
      expect(PRICE_RANGES['Trà Sữa']).toBeDefined();
      expect(PRICE_RANGES['Nước Ép']).toBeDefined();
      expect(PRICE_RANGES['Sinh Tố']).toBeDefined();
      expect(PRICE_RANGES['Trà Trái Cây']).toBeDefined();
    });

    it('should have all major main dish categories', () => {
      expect(PRICE_RANGES['Phở']).toBeDefined();
      expect(PRICE_RANGES['Bún']).toBeDefined();
      expect(PRICE_RANGES['Cơm']).toBeDefined();
      expect(PRICE_RANGES['Bánh Mì']).toBeDefined();
    });

    it('should have all premium categories', () => {
      expect(PRICE_RANGES['Lẩu']).toBeDefined();
      expect(PRICE_RANGES['Món Nướng']).toBeDefined();
      expect(PRICE_RANGES['Món Hải Sản']).toBeDefined();
      expect(PRICE_RANGES['Steak']).toBeDefined();
    });

    it('should have realistic price ranges', () => {
      // Beverages should be cheaper than main dishes
      expect(PRICE_RANGES['Cà Phê'].max).toBeLessThan(PRICE_RANGES['Phở'].max);
      expect(PRICE_RANGES['Trà Sữa'].max).toBeLessThan(PRICE_RANGES['Cơm'].max);
      
      // Premium items should be more expensive
      expect(PRICE_RANGES['Lẩu'].min).toBeGreaterThan(PRICE_RANGES['Phở'].max);
      expect(PRICE_RANGES['Lẩu'].max).toBeGreaterThan(PRICE_RANGES['Cơm'].max);
      
      // Steak is premium priced
      expect(PRICE_RANGES['Steak'].min).toBeGreaterThanOrEqual(PRICE_RANGES['Cơm'].min);
      expect(PRICE_RANGES['Steak'].max).toBeGreaterThan(PRICE_RANGES['Cơm'].max);
    });

    it('should have min less than max for all categories', () => {
      Object.values(PRICE_RANGES).forEach(range => {
        expect(range.min).toBeLessThan(range.max);
      });
    });

    it('should have prices in multiples of 1000', () => {
      Object.values(PRICE_RANGES).forEach(range => {
        expect(range.min % 1000).toBe(0);
        expect(range.max % 1000).toBe(0);
      });
    });
  });

  describe('PRICE_STATS', () => {
    it('should have correct total categories count', () => {
      expect(PRICE_STATS.totalCategories).toBe(Object.keys(PRICE_RANGES).length);
      expect(PRICE_STATS.totalCategories).toBeGreaterThan(50);
    });

    it('should have correct lowest min price', () => {
      expect(PRICE_STATS.lowestMinPrice).toBe(15000); // Cà Phê, Bánh Mì, Chè, etc.
    });

    it('should have correct highest max price', () => {
      expect(PRICE_STATS.highestMaxPrice).toBe(200000); // Lẩu, Steak
    });

    it('should have reasonable average prices', () => {
      expect(PRICE_STATS.averageMinPrice).toBeGreaterThan(20000);
      expect(PRICE_STATS.averageMinPrice).toBeLessThan(60000);
      expect(PRICE_STATS.averageMaxPrice).toBeGreaterThan(60000);
      expect(PRICE_STATS.averageMaxPrice).toBeLessThan(150000);
    });
  });

  describe('Price Distribution', () => {
    it('should generate varied prices for same category', () => {
      const prices = new Set<number>();
      
      for (let i = 0; i < 100; i++) {
        prices.add(generateRealisticPrice('Phở'));
      }
      
      // Should generate at least 10 different prices
      expect(prices.size).toBeGreaterThanOrEqual(10);
    });

    it('should generate prices across full range', () => {
      const prices: number[] = [];
      const category = 'Cơm';
      const range = getPriceRange(category)!;
      
      for (let i = 0; i < 1000; i++) {
        prices.push(generateRealisticPrice(category));
      }
      
      const minGenerated = Math.min(...prices);
      const maxGenerated = Math.max(...prices);
      
      // Should generate prices close to both ends of the range
      expect(minGenerated).toBeLessThanOrEqual(range.min + 5000);
      expect(maxGenerated).toBeGreaterThanOrEqual(range.max - 5000);
    });
  });

  describe('Requirements Validation', () => {
    it('should meet Requirement 2.3: realistic price ranges for Vietnamese food', () => {
      // Verify key categories have realistic ranges
      expect(PRICE_RANGES['Cà Phê']).toEqual({ min: 15000, max: 45000, category: 'Cà Phê' });
      expect(PRICE_RANGES['Trà Sữa']).toEqual({ min: 25000, max: 65000, category: 'Trà Sữa' });
      expect(PRICE_RANGES['Phở']).toEqual({ min: 35000, max: 75000, category: 'Phở' });
      expect(PRICE_RANGES['Cơm']).toEqual({ min: 35000, max: 85000, category: 'Cơm' });
    });

    it('should meet Requirement 4.6: numeric fields with correct precision', () => {
      // All prices should be integers (no decimal places)
      Object.values(PRICE_RANGES).forEach(range => {
        expect(Number.isInteger(range.min)).toBe(true);
        expect(Number.isInteger(range.max)).toBe(true);
      });
      
      // Generated prices should also be integers
      for (let i = 0; i < 50; i++) {
        const price = generateRealisticPrice('Phở');
        expect(Number.isInteger(price)).toBe(true);
      }
    });
  });
});
