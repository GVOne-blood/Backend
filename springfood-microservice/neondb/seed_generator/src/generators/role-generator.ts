/**
 * Role Generator Module
 * 
 * Generates exactly 5 fixed roles for the authentication system:
 * - CUSTOMER
 * - SHOP_OWNER
 * - ADMIN
 * - STAFF
 * - DELIVER
 * 
 * Requirements: 3.3, 10.1, 10.2, 10.3, 10.4
 */

import { IDRegistry } from '../utils/id-registry';

export interface Role {
  role_name: string;
  description: string;
  created_at: Date;
  updated_at: Date;
}

/**
 * Fixed role definitions
 */
const ROLE_DEFINITIONS: Array<{ name: string; description: string }> = [
  {
    name: 'CUSTOMER',
    description: 'Khách hàng đặt món ăn trên nền tảng'
  },
  {
    name: 'SHOP_OWNER',
    description: 'Chủ cửa hàng quản lý sản phẩm và đơn hàng'
  },
  {
    name: 'ADMIN',
    description: 'Quản trị viên hệ thống với quyền truy cập đầy đủ'
  },
  {
    name: 'STAFF',
    description: 'Nhân viên hỗ trợ khách hàng và xử lý đơn hàng'
  },
  {
    name: 'DELIVER',
    description: 'Nhân viên giao hàng vận chuyển đơn hàng'
  }
];

/**
 * Generate exactly 5 fixed roles
 * 
 * @param registry - ID registry to register role names
 * @returns Array of 5 role records
 * 
 * Requirements:
 * - 3.3: Generate exactly 5 records for authentication.role table
 * - 10.1: Create exactly 1 user with ADMIN role
 * - 10.2: Create exactly 2 users with SHOP_OWNER role
 * - 10.3: Create exactly 3 users with STAFF role
 * - 10.4: Create exactly 4 users with CUSTOMER role
 */
export function generateRoles(registry: IDRegistry): Role[] {
  const now = new Date();
  const roles: Role[] = [];

  for (const roleDef of ROLE_DEFINITIONS) {
    const role: Role = {
      role_name: roleDef.name,
      description: roleDef.description,
      created_at: now,
      updated_at: now
    };

    roles.push(role);

    // Register role name in ID registry for foreign key references
    registry.register('roles', roleDef.name, role);
  }

  return roles;
}

/**
 * Get all role names
 */
export function getAllRoleNames(): string[] {
  return ROLE_DEFINITIONS.map(r => r.name);
}

/**
 * Validate that exactly 5 roles were generated
 */
export function validateRoleCount(roles: Role[]): boolean {
  return roles.length === 5;
}

/**
 * Validate that all required roles exist
 */
export function validateRequiredRoles(roles: Role[]): boolean {
  const requiredRoles = ['CUSTOMER', 'SHOP_OWNER', 'ADMIN', 'STAFF', 'DELIVER'];
  const roleNames = roles.map(r => r.role_name);
  
  return requiredRoles.every(required => roleNames.includes(required));
}
