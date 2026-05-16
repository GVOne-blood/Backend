/**
 * Unit Tests for ID Registry
 * 
 * Tests ID registration, retrieval, random selection, and error handling.
 * Requirements: 1.1, 9.1
 */

import { IDRegistry } from '../src/utils/id-registry';

describe('IDRegistry', () => {
  let registry: IDRegistry;

  beforeEach(() => {
    registry = new IDRegistry();
  });

  describe('register', () => {
    it('should register an ID with data for a table', () => {
      const userId = '550e8400-e29b-41d4-a716-446655440000';
      const userData = { name: 'John Doe', email: 'john@example.com' };

      registry.register('users', userId, userData);

      expect(registry.exists('users', userId)).toBe(true);
      expect(registry.getData('users', userId)).toEqual(userData);
    });

    it('should register multiple IDs for the same table', () => {
      const user1Id = '550e8400-e29b-41d4-a716-446655440001';
      const user2Id = '550e8400-e29b-41d4-a716-446655440002';

      registry.register('users', user1Id, { name: 'User 1' });
      registry.register('users', user2Id, { name: 'User 2' });

      expect(registry.getCount('users')).toBe(2);
      expect(registry.getAllIds('users')).toContain(user1Id);
      expect(registry.getAllIds('users')).toContain(user2Id);
    });

    it('should register IDs for different tables independently', () => {
      const userId = '550e8400-e29b-41d4-a716-446655440000';
      const shopId = '660e8400-e29b-41d4-a716-446655440000';

      registry.register('users', userId, { name: 'User' });
      registry.register('shops', shopId, { name: 'Shop' });

      expect(registry.getCount('users')).toBe(1);
      expect(registry.getCount('shops')).toBe(1);
      expect(registry.getTables()).toContain('users');
      expect(registry.getTables()).toContain('shops');
    });

    it('should overwrite data if same ID is registered twice', () => {
      const userId = '550e8400-e29b-41d4-a716-446655440000';

      registry.register('users', userId, { name: 'Old Name' });
      registry.register('users', userId, { name: 'New Name' });

      expect(registry.getCount('users')).toBe(1);
      expect(registry.getData('users', userId)).toEqual({ name: 'New Name' });
    });
  });

  describe('getRandomId', () => {
    it('should return a random ID from the table', () => {
      const user1Id = '550e8400-e29b-41d4-a716-446655440001';
      const user2Id = '550e8400-e29b-41d4-a716-446655440002';
      const user3Id = '550e8400-e29b-41d4-a716-446655440003';

      registry.register('users', user1Id, {});
      registry.register('users', user2Id, {});
      registry.register('users', user3Id, {});

      const randomId = registry.getRandomId('users');

      expect([user1Id, user2Id, user3Id]).toContain(randomId);
    });

    it('should return different IDs on multiple calls (probabilistic)', () => {
      // Register 10 IDs to increase probability of getting different values
      const ids: string[] = [];
      for (let i = 0; i < 10; i++) {
        const id = `550e8400-e29b-41d4-a716-44665544000${i}`;
        ids.push(id);
        registry.register('users', id, {});
      }

      const randomIds = new Set<string>();
      for (let i = 0; i < 20; i++) {
        randomIds.add(registry.getRandomId('users'));
      }

      // With 10 IDs and 20 calls, we should get at least 2 different IDs
      expect(randomIds.size).toBeGreaterThanOrEqual(2);
    });

    it('should throw error if table does not exist', () => {
      expect(() => registry.getRandomId('nonexistent')).toThrow(
        "Table 'nonexistent' not found in registry"
      );
    });

    it('should throw error if table has no IDs', () => {
      // After clearing, the table is removed from registry entirely
      // So it will throw "Table not found" error
      registry.register('users', '550e8400-e29b-41d4-a716-446655440000', {});
      registry.clear('users');

      expect(() => registry.getRandomId('users')).toThrow(
        "Table 'users' not found in registry"
      );
    });
  });

  describe('getAllIds', () => {
    it('should return all IDs for a table', () => {
      const user1Id = '550e8400-e29b-41d4-a716-446655440001';
      const user2Id = '550e8400-e29b-41d4-a716-446655440002';

      registry.register('users', user1Id, {});
      registry.register('users', user2Id, {});

      const allIds = registry.getAllIds('users');

      expect(allIds).toHaveLength(2);
      expect(allIds).toContain(user1Id);
      expect(allIds).toContain(user2Id);
    });

    it('should return empty array if table does not exist', () => {
      const allIds = registry.getAllIds('nonexistent');

      expect(allIds).toEqual([]);
    });

    it('should return empty array if table has no IDs', () => {
      registry.register('users', '550e8400-e29b-41d4-a716-446655440000', {});
      registry.clear('users');

      const allIds = registry.getAllIds('users');

      expect(allIds).toEqual([]);
    });
  });

  describe('exists', () => {
    it('should return true if ID exists in table', () => {
      const userId = '550e8400-e29b-41d4-a716-446655440000';

      registry.register('users', userId, {});

      expect(registry.exists('users', userId)).toBe(true);
    });

    it('should return false if ID does not exist in table', () => {
      registry.register('users', '550e8400-e29b-41d4-a716-446655440000', {});

      expect(registry.exists('users', '660e8400-e29b-41d4-a716-446655440000')).toBe(false);
    });

    it('should return false if table does not exist', () => {
      expect(registry.exists('nonexistent', '550e8400-e29b-41d4-a716-446655440000')).toBe(false);
    });
  });

  describe('getData', () => {
    it('should return data associated with an ID', () => {
      const userId = '550e8400-e29b-41d4-a716-446655440000';
      const userData = { name: 'John Doe', email: 'john@example.com' };

      registry.register('users', userId, userData);

      expect(registry.getData('users', userId)).toEqual(userData);
    });

    it('should return undefined if ID does not exist', () => {
      expect(registry.getData('users', '550e8400-e29b-41d4-a716-446655440000')).toBeUndefined();
    });

    it('should return undefined if table does not exist', () => {
      expect(registry.getData('nonexistent', '550e8400-e29b-41d4-a716-446655440000')).toBeUndefined();
    });
  });

  describe('getCount', () => {
    it('should return the number of registered IDs', () => {
      registry.register('users', '550e8400-e29b-41d4-a716-446655440001', {});
      registry.register('users', '550e8400-e29b-41d4-a716-446655440002', {});
      registry.register('users', '550e8400-e29b-41d4-a716-446655440003', {});

      expect(registry.getCount('users')).toBe(3);
    });

    it('should return 0 if table does not exist', () => {
      expect(registry.getCount('nonexistent')).toBe(0);
    });

    it('should return 0 if table has no IDs', () => {
      registry.register('users', '550e8400-e29b-41d4-a716-446655440000', {});
      registry.clear('users');

      expect(registry.getCount('users')).toBe(0);
    });
  });

  describe('clear', () => {
    it('should clear all IDs for a specific table', () => {
      registry.register('users', '550e8400-e29b-41d4-a716-446655440001', {});
      registry.register('users', '550e8400-e29b-41d4-a716-446655440002', {});
      registry.register('shops', '660e8400-e29b-41d4-a716-446655440001', {});

      registry.clear('users');

      expect(registry.getCount('users')).toBe(0);
      expect(registry.getCount('shops')).toBe(1);
    });

    it('should not throw error if table does not exist', () => {
      expect(() => registry.clear('nonexistent')).not.toThrow();
    });
  });

  describe('clearAll', () => {
    it('should clear all IDs across all tables', () => {
      registry.register('users', '550e8400-e29b-41d4-a716-446655440001', {});
      registry.register('shops', '660e8400-e29b-41d4-a716-446655440001', {});
      registry.register('products', '770e8400-e29b-41d4-a716-446655440001', {});

      registry.clearAll();

      expect(registry.getCount('users')).toBe(0);
      expect(registry.getCount('shops')).toBe(0);
      expect(registry.getCount('products')).toBe(0);
      expect(registry.getTables()).toEqual([]);
    });
  });

  describe('getTables', () => {
    it('should return all table names in the registry', () => {
      registry.register('users', '550e8400-e29b-41d4-a716-446655440001', {});
      registry.register('shops', '660e8400-e29b-41d4-a716-446655440001', {});
      registry.register('products', '770e8400-e29b-41d4-a716-446655440001', {});

      const tables = registry.getTables();

      expect(tables).toHaveLength(3);
      expect(tables).toContain('users');
      expect(tables).toContain('shops');
      expect(tables).toContain('products');
    });

    it('should return empty array if no tables registered', () => {
      expect(registry.getTables()).toEqual([]);
    });
  });
});
