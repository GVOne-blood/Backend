/**
 * Unit Tests for Level 1-3 Generators
 * 
 * Tests for:
 * - Role generator
 * - User generator
 * - User_has_role generator
 * - Shop generator
 * 
 * Requirements: 3.3, 10.1, 10.2, 10.3, 10.4, 4.8
 */

import { IDRegistry } from '../src/utils/id-registry';
import { 
  generateRoles, 
  validateRoleCount, 
  validateRequiredRoles 
} from '../src/generators/role-generator';
import { 
  generateUsers, 
  assignShopsToOwners,
  validateUserRoleDistribution,
  validatePasswordHashing,
  getUsersByRole
} from '../src/generators/user-generator';
import { 
  generateUserHasRole,
  validateUserHasRoleForeignKeys,
  validateOneRolePerUser
} from '../src/generators/user-has-role-generator';
import { 
  generateShops,
  validateShopCount,
  validateShopStatus,
  validateShopData
} from '../src/generators/shop-generator';

describe('Role Generator', () => {
  let registry: IDRegistry;

  beforeEach(() => {
    registry = new IDRegistry();
  });

  test('should generate exactly 5 roles', () => {
    const roles = generateRoles(registry);
    
    expect(roles).toHaveLength(5);
    expect(validateRoleCount(roles)).toBe(true);
  });

  test('should generate all required roles', () => {
    const roles = generateRoles(registry);
    
    expect(validateRequiredRoles(roles)).toBe(true);
    
    const roleNames = roles.map(r => r.role_name);
    expect(roleNames).toContain('CUSTOMER');
    expect(roleNames).toContain('SHOP_OWNER');
    expect(roleNames).toContain('ADMIN');
    expect(roleNames).toContain('STAFF');
    expect(roleNames).toContain('DELIVER');
  });

  test('should register all roles in ID registry', () => {
    const roles = generateRoles(registry);
    
    for (const role of roles) {
      expect(registry.exists('roles', role.role_name)).toBe(true);
    }
  });

  test('should populate all required fields', () => {
    const roles = generateRoles(registry);
    
    for (const role of roles) {
      expect(role.role_name).toBeTruthy();
      expect(role.description).toBeTruthy();
      expect(role.created_at).toBeInstanceOf(Date);
      expect(role.updated_at).toBeInstanceOf(Date);
    }
  });
});

describe('User Generator', () => {
  let registry: IDRegistry;

  beforeEach(() => {
    registry = new IDRegistry();
    // Generate roles first (required for user generation)
    generateRoles(registry);
  });

  test('should generate exactly 10 users', () => {
    const users = generateUsers(registry);
    
    expect(users).toHaveLength(10);
  });

  test('should generate correct role distribution', () => {
    const users = generateUsers(registry);
    
    expect(validateUserRoleDistribution(users)).toBe(true);
    
    const adminUsers = getUsersByRole(users, 'ADMIN');
    const shopOwnerUsers = getUsersByRole(users, 'SHOP_OWNER');
    const staffUsers = getUsersByRole(users, 'STAFF');
    const customerUsers = getUsersByRole(users, 'CUSTOMER');
    
    expect(adminUsers).toHaveLength(1);
    expect(shopOwnerUsers).toHaveLength(2);
    expect(staffUsers).toHaveLength(3);
    expect(customerUsers).toHaveLength(4);
  });

  test('should hash all passwords with BCrypt', () => {
    const users = generateUsers(registry);
    
    expect(validatePasswordHashing(users)).toBe(true);
    
    // BCrypt hashes should start with $2a$, $2b$, or $2y$
    for (const user of users) {
      expect(user.password).toMatch(/^\$2[aby]\$/);
      expect(user.password).toHaveLength(60);
    }
  });

  test('should not store plain text passwords', () => {
    const users = generateUsers(registry);
    
    for (const user of users) {
      expect(user.password).not.toBe('Password123!');
    }
  });

  test('should set email_verified and phone_verified to true', () => {
    const users = generateUsers(registry);
    
    for (const user of users) {
      expect(user.email_verified).toBe(true);
      expect(user.phone_verified).toBe(true);
    }
  });

  test('should generate emails in correct format', () => {
    const users = generateUsers(registry);
    
    for (const user of users) {
      expect(user.email).toMatch(/^(admin|shop_owner|staff|customer)\d+@springfood\.vn$/);
    }
  });

  test('should generate realistic Vietnamese names', () => {
    const users = generateUsers(registry);
    
    const vietnameseLastNames = ['Nguyễn', 'Trần', 'Lê', 'Phạm', 'Hoàng', 'Vũ', 'Võ', 'Đặng', 'Bùi', 'Đỗ'];
    
    for (const user of users) {
      expect(user.first_name).toBeTruthy();
      expect(user.last_name).toBeTruthy();
      expect(vietnameseLastNames).toContain(user.last_name);
    }
  });

  test('should generate realistic phone numbers', () => {
    const users = generateUsers(registry);
    
    for (const user of users) {
      expect(user.phone).toMatch(/^0\d{9,10}$/);
    }
  });

  test('should register all users in ID registry', () => {
    const users = generateUsers(registry);
    
    for (const user of users) {
      expect(registry.exists('users', user.user_id)).toBe(true);
    }
  });

  test('should assign shop_id to SHOP_OWNER users after shops are generated', () => {
    const users = generateUsers(registry);
    const shops = generateShops(registry, 27);
    
    // Initially, SHOP_OWNER users should have null shop_id
    const shopOwners = getUsersByRole(users, 'SHOP_OWNER');
    for (const owner of shopOwners) {
      expect(owner.shop_id).toBeNull();
    }
    
    // After assignment, SHOP_OWNER users should have valid shop_id
    assignShopsToOwners(users, registry);
    
    for (const owner of shopOwners) {
      expect(owner.shop_id).toBeTruthy();
      expect(registry.exists('shops', owner.shop_id!)).toBe(true);
    }
  });

  test('should throw error if assigning shops before shops are generated', () => {
    const users = generateUsers(registry);
    
    expect(() => {
      assignShopsToOwners(users, registry);
    }).toThrow('No shops found in registry');
  });
});

describe('User Has Role Generator', () => {
  let registry: IDRegistry;

  beforeEach(() => {
    registry = new IDRegistry();
    generateRoles(registry);
  });

  test('should generate user_has_role records for all users', () => {
    const users = generateUsers(registry);
    const userHasRoles = generateUserHasRole(users, registry);
    
    expect(userHasRoles).toHaveLength(10);
  });

  test('should validate all foreign keys exist in registries', () => {
    const users = generateUsers(registry);
    const userHasRoles = generateUserHasRole(users, registry);
    
    expect(validateUserHasRoleForeignKeys(userHasRoles, registry)).toBe(true);
  });

  test('should ensure each user has exactly one role', () => {
    const users = generateUsers(registry);
    const userHasRoles = generateUserHasRole(users, registry);
    
    expect(validateOneRolePerUser(userHasRoles, users.length)).toBe(true);
  });

  test('should link users to correct roles', () => {
    const users = generateUsers(registry);
    const userHasRoles = generateUserHasRole(users, registry);
    
    for (const uhr of userHasRoles) {
      const user = users.find(u => u.user_id === uhr.user_id);
      expect(user).toBeDefined();
      expect(user!.role).toBe(uhr.role_name);
    }
  });

  test('should throw error if user_id not found in registry', () => {
    const users = generateUsers(registry);
    
    // Create a user with invalid ID
    const invalidUser = { ...users[0], user_id: 'invalid-id' };
    
    expect(() => {
      generateUserHasRole([invalidUser], registry);
    }).toThrow('User ID invalid-id not found in registry');
  });

  test('should throw error if role_name not found in registry', () => {
    const users = generateUsers(registry);
    
    // Create a user with invalid role
    const invalidUser = { ...users[0], role: 'INVALID_ROLE' };
    
    expect(() => {
      generateUserHasRole([invalidUser], registry);
    }).toThrow('Role INVALID_ROLE not found in registry');
  });
});

describe('Shop Generator', () => {
  let registry: IDRegistry;

  beforeEach(() => {
    registry = new IDRegistry();
  });

  test('should generate at least 25 shops', () => {
    const shops = generateShops(registry);
    
    expect(validateShopCount(shops)).toBe(true);
    expect(shops.length).toBeGreaterThanOrEqual(25);
  });

  test('should generate exactly 27 shops by default', () => {
    const shops = generateShops(registry);
    
    expect(shops).toHaveLength(27);
  });

  test('should set all shops to ACTIVE status', () => {
    const shops = generateShops(registry);
    
    expect(validateShopStatus(shops)).toBe(true);
    
    for (const shop of shops) {
      expect(shop.shop_status).toBe('ACTIVE');
    }
  });

  test('should initialize total_product to 0', () => {
    const shops = generateShops(registry);
    
    for (const shop of shops) {
      expect(shop.total_product).toBe(0);
    }
  });

  test('should generate realistic shop data', () => {
    const shops = generateShops(registry);
    
    expect(validateShopData(shops)).toBe(true);
    
    for (const shop of shops) {
      expect(shop.shop_name).toBeTruthy();
      expect(shop.email).toMatch(/@springfood\.vn$/);
      expect(shop.phone_number).toMatch(/^0\d{9,11}$/);
      expect(shop.shop_address).toBeTruthy();
      expect(shop.city).toBeTruthy();
      expect(shop.province).toBeTruthy();
      expect(shop.avg_star).toBeGreaterThanOrEqual(0);
      expect(shop.avg_star).toBeLessThanOrEqual(5);
    }
  });

  test('should register all shops in ID registry', () => {
    const shops = generateShops(registry);
    
    for (const shop of shops) {
      expect(registry.exists('shops', shop.shop_id)).toBe(true);
    }
  });

  test('should generate unique shop IDs', () => {
    const shops = generateShops(registry);
    
    const shopIds = shops.map(s => s.shop_id);
    const uniqueIds = new Set(shopIds);
    
    expect(uniqueIds.size).toBe(shops.length);
  });

  test('should generate Vietnamese locations', () => {
    const shops = generateShops(registry);
    
    const vietnameseProvinces = ['Hồ Chí Minh', 'Hà Nội', 'Đà Nẵng', 'Khánh Hòa', 'Thừa Thiên Huế', 'Cần Thơ'];
    
    for (const shop of shops) {
      expect(vietnameseProvinces).toContain(shop.province);
    }
  });

  test('should populate created_at and updated_at timestamps', () => {
    const shops = generateShops(registry);
    
    for (const shop of shops) {
      expect(shop.created_at).toBeInstanceOf(Date);
      expect(shop.updated_at).toBeInstanceOf(Date);
      expect(shop.created_at.getTime()).toBeLessThanOrEqual(shop.updated_at.getTime());
    }
  });
});

describe('Integration Tests - Level 1-3', () => {
  let registry: IDRegistry;

  beforeEach(() => {
    registry = new IDRegistry();
  });

  test('should generate complete Level 1-3 data in correct order', () => {
    // Level 1: Roles
    const roles = generateRoles(registry);
    expect(roles).toHaveLength(5);
    
    // Level 2: Users
    const users = generateUsers(registry);
    expect(users).toHaveLength(10);
    
    // Level 3: Shops
    const shops = generateShops(registry);
    expect(shops.length).toBeGreaterThanOrEqual(25);
    
    // Update SHOP_OWNER users with shop_id
    assignShopsToOwners(users, registry);
    
    // Generate user_has_role junction table
    const userHasRoles = generateUserHasRole(users, registry);
    expect(userHasRoles).toHaveLength(10);
    
    // Validate all foreign keys
    expect(validateUserHasRoleForeignKeys(userHasRoles, registry)).toBe(true);
  });

  test('should maintain referential integrity across all Level 1-3 tables', () => {
    const roles = generateRoles(registry);
    const users = generateUsers(registry);
    const shops = generateShops(registry);
    assignShopsToOwners(users, registry);
    const userHasRoles = generateUserHasRole(users, registry);
    
    // Verify all user_has_role records reference valid users and roles
    for (const uhr of userHasRoles) {
      expect(registry.exists('users', uhr.user_id)).toBe(true);
      expect(registry.exists('roles', uhr.role_name)).toBe(true);
    }
    
    // Verify all SHOP_OWNER users reference valid shops
    const shopOwners = getUsersByRole(users, 'SHOP_OWNER');
    for (const owner of shopOwners) {
      expect(owner.shop_id).toBeTruthy();
      expect(registry.exists('shops', owner.shop_id!)).toBe(true);
    }
  });

  test('should generate data with correct counts for all tables', () => {
    const roles = generateRoles(registry);
    const users = generateUsers(registry);
    const shops = generateShops(registry);
    const userHasRoles = generateUserHasRole(users, registry);
    
    expect(registry.getCount('roles')).toBe(5);
    expect(registry.getCount('users')).toBe(10);
    expect(registry.getCount('shops')).toBeGreaterThanOrEqual(25);
    expect(userHasRoles.length).toBe(10);
  });
});
