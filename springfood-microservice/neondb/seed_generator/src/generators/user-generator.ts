/**
 * User Generator Module
 * 
 * Generates exactly 10 predefined users with specific role distribution:
 * - 1 admin
 * - 2 shop_owner
 * - 3 staff
 * - 4 customer
 * 
 * Requirements: 2.8, 3.4, 4.1, 4.2, 4.3, 4.8, 10.1, 10.2, 10.3, 10.4, 10.5, 10.7, 10.8, 12.7
 */

import { IDRegistry } from '../utils/id-registry';
import { generateUUID } from '../utils/uuid-generator';
import { hashPassword } from '../utils/bcrypt-hasher';

export interface User {
  user_id: string;
  first_name: string;
  last_name: string;
  email: string;
  phone: string;
  password: string; // BCrypt hashed
  avatar: string;
  shop_id: string | null; // Only for SHOP_OWNER role
  status: string;
  email_verified: boolean;
  phone_verified: boolean;
  created_at: Date;
  updated_at: Date;
  role: string; // Temporary field for role assignment (not in DB schema)
}

/**
 * Vietnamese name templates
 */
const VIETNAMESE_FIRST_NAMES = {
  male: ['Văn', 'Đức', 'Minh', 'Hoàng', 'Quang', 'Tuấn', 'Hải', 'Thành', 'Dũng', 'Khoa'],
  female: ['Thị', 'Thu', 'Hương', 'Lan', 'Mai', 'Linh', 'Nga', 'Hà', 'Trang', 'Phương']
};

const VIETNAMESE_LAST_NAMES = ['Nguyễn', 'Trần', 'Lê', 'Phạm', 'Hoàng', 'Vũ', 'Võ', 'Đặng', 'Bùi', 'Đỗ'];

/**
 * Predefined user templates with specific roles
 */
const USER_TEMPLATES: Array<{
  firstName: string;
  lastName: string;
  role: string;
  gender: 'male' | 'female';
}> = [
  // 1 Admin
  {
    firstName: 'Văn',
    lastName: 'Nguyễn',
    role: 'ADMIN',
    gender: 'male'
  },
  // 2 Shop Owners
  {
    firstName: 'Đức',
    lastName: 'Trần',
    role: 'SHOP_OWNER',
    gender: 'male'
  },
  {
    firstName: 'Thu',
    lastName: 'Lê',
    role: 'SHOP_OWNER',
    gender: 'female'
  },
  // 3 Staff
  {
    firstName: 'Minh',
    lastName: 'Phạm',
    role: 'STAFF',
    gender: 'male'
  },
  {
    firstName: 'Hương',
    lastName: 'Hoàng',
    role: 'STAFF',
    gender: 'female'
  },
  {
    firstName: 'Quang',
    lastName: 'Vũ',
    role: 'STAFF',
    gender: 'male'
  },
  // 4 Customers
  {
    firstName: 'Lan',
    lastName: 'Võ',
    role: 'CUSTOMER',
    gender: 'female'
  },
  {
    firstName: 'Tuấn',
    lastName: 'Đặng',
    role: 'CUSTOMER',
    gender: 'male'
  },
  {
    firstName: 'Mai',
    lastName: 'Bùi',
    role: 'CUSTOMER',
    gender: 'female'
  },
  {
    firstName: 'Hải',
    lastName: 'Đỗ',
    role: 'CUSTOMER',
    gender: 'male'
  }
];

/**
 * Generate realistic Vietnamese phone number
 * Format: 0xxx-xxx-xxx
 */
function generatePhoneNumber(): string {
  const prefixes = ['090', '091', '093', '094', '097', '098', '086', '096', '032', '033', '034', '035', '036', '037', '038', '039'];
  const prefix = prefixes[Math.floor(Math.random() * prefixes.length)];
  const middle = Math.floor(Math.random() * 900 + 100); // 100-999
  const last = Math.floor(Math.random() * 9000 + 1000); // 1000-9999
  
  return `${prefix}${middle}${last}`;
}

/**
 * Generate avatar URL based on gender
 */
function generateAvatarURL(gender: 'male' | 'female', index: number): string {
  const baseURL = 'https://i.pravatar.cc/300';
  // Use different seeds for different users
  const seed = gender === 'male' ? index * 2 : index * 2 + 1;
  return `${baseURL}?img=${seed}`;
}

/**
 * Generate exactly 10 predefined users with specific role distribution
 * 
 * @param registry - ID registry to register user IDs
 * @returns Array of 10 user records
 * 
 * Requirements:
 * - 2.8: Generate user data with Vietnamese names and realistic phone numbers
 * - 3.4: Generate exactly 10 records for authentication.user table
 * - 4.1: Populate all non-nullable columns with valid data
 * - 4.2: Populate nullable columns with realistic data
 * - 4.3: Generate data with correct data types
 * - 4.8: Generate BCrypt hashed passwords
 * - 10.1: Create exactly 1 user with ADMIN role
 * - 10.2: Create exactly 2 users with SHOP_OWNER role
 * - 10.3: Create exactly 3 users with STAFF role
 * - 10.4: Create exactly 4 users with CUSTOMER role
 * - 10.5: Generate BCrypt hashed passwords using "Password123!"
 * - 10.7: Assign realistic Vietnamese names to all users
 * - 10.8: Set email_verified and phone_verified to true
 * - 12.7: Populate shop_id field for SHOP_OWNER users (will be updated later)
 */
export function generateUsers(registry: IDRegistry): User[] {
  const now = new Date();
  const users: User[] = [];
  
  // Hash the password once (same for all users)
  const hashedPassword = hashPassword('Password123!');
  
  // Role counters for email generation
  const roleCounts: Record<string, number> = {
    ADMIN: 0,
    SHOP_OWNER: 0,
    STAFF: 0,
    CUSTOMER: 0
  };

  for (let i = 0; i < USER_TEMPLATES.length; i++) {
    const template = USER_TEMPLATES[i];
    const userId = generateUUID();
    
    // Increment role counter
    roleCounts[template.role]++;
    
    // Generate email: {role}{number}@springfood.vn
    const rolePrefix = template.role.toLowerCase();
    const email = `${rolePrefix}${roleCounts[template.role]}@springfood.vn`;
    
    const user: User = {
      user_id: userId,
      first_name: template.firstName,
      last_name: template.lastName,
      email: email,
      phone: generatePhoneNumber(),
      password: hashedPassword,
      avatar: generateAvatarURL(template.gender, i),
      shop_id: null, // Will be populated after shops are generated for SHOP_OWNER users
      status: 'ACTIVE',
      email_verified: true,
      phone_verified: true,
      created_at: now,
      updated_at: now,
      role: template.role // Temporary field for role assignment
    };

    users.push(user);

    // Register user ID in registry
    registry.register('users', userId, user);
  }

  return users;
}

/**
 * Update SHOP_OWNER users with shop_id references
 * This should be called after shops are generated
 * 
 * @param users - Array of user records
 * @param registry - ID registry containing shop IDs
 * 
 * Requirements:
 * - 12.7: Populate shop_id field in authentication.user table for SHOP_OWNER users
 * - 12.8: Ensure shop_id in user table matches existing shop_id in shops table
 */
export function assignShopsToOwners(users: User[], registry: IDRegistry): void {
  const shopIds = registry.getAllIds('shops');
  
  if (shopIds.length === 0) {
    throw new Error('No shops found in registry. Generate shops before assigning to owners.');
  }

  // Get all SHOP_OWNER users
  const shopOwners = users.filter(u => u.role === 'SHOP_OWNER');
  
  if (shopOwners.length > shopIds.length) {
    throw new Error(`Not enough shops (${shopIds.length}) for shop owners (${shopOwners.length})`);
  }

  // Assign each shop owner to a unique shop
  for (let i = 0; i < shopOwners.length; i++) {
    shopOwners[i].shop_id = shopIds[i];
    
    // Update the user in the registry
    registry.register('users', shopOwners[i].user_id, shopOwners[i]);
  }
}

/**
 * Get users by role
 */
export function getUsersByRole(users: User[], role: string): User[] {
  return users.filter(u => u.role === role);
}

/**
 * Validate user role distribution
 */
export function validateUserRoleDistribution(users: User[]): boolean {
  const adminCount = users.filter(u => u.role === 'ADMIN').length;
  const shopOwnerCount = users.filter(u => u.role === 'SHOP_OWNER').length;
  const staffCount = users.filter(u => u.role === 'STAFF').length;
  const customerCount = users.filter(u => u.role === 'CUSTOMER').length;
  
  return (
    adminCount === 1 &&
    shopOwnerCount === 2 &&
    staffCount === 3 &&
    customerCount === 4
  );
}

/**
 * Validate that all users have BCrypt hashed passwords
 */
export function validatePasswordHashing(users: User[]): boolean {
  // BCrypt hashes start with $2a$, $2b$, or $2y$ and are 60 characters long
  const bcryptRegex = /^\$2[aby]\$\d{2}\$.{53}$/;
  
  return users.every(u => bcryptRegex.test(u.password));
}

/**
 * Get user credentials for documentation
 * Returns plain text password and user details for README
 */
export function getUserCredentials(): Array<{ email: string; password: string; role: string }> {
  const credentials: Array<{ email: string; password: string; role: string }> = [];
  
  const roleCounts: Record<string, number> = {
    ADMIN: 0,
    SHOP_OWNER: 0,
    STAFF: 0,
    CUSTOMER: 0
  };

  for (const template of USER_TEMPLATES) {
    roleCounts[template.role]++;
    const rolePrefix = template.role.toLowerCase();
    const email = `${rolePrefix}${roleCounts[template.role]}@springfood.vn`;
    
    credentials.push({
      email: email,
      password: 'Password123!',
      role: template.role
    });
  }

  return credentials;
}
