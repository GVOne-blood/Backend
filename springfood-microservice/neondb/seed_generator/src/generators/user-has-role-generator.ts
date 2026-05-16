/**
 * User Has Role Junction Table Generator
 * 
 * Generates records linking users to their assigned roles.
 * This is a many-to-many relationship table between users and roles.
 * 
 * Requirements: 1.6, 9.6
 */

import { IDRegistry } from '../utils/id-registry';
import { User } from './user-generator';

export interface UserHasRole {
  user_id: string;
  role_name: string;
}

/**
 * Generate user_has_role junction table records
 * Links each user to their assigned role
 * 
 * @param users - Array of user records with role assignments
 * @param registry - ID registry to validate foreign keys
 * @returns Array of user_has_role records
 * 
 * Requirements:
 * - 1.6: Generate data for user_has_role junction table ensuring both user_id and role_name exist
 * - 9.6: Maintain consistent user information across schemas
 */
export function generateUserHasRole(users: User[], registry: IDRegistry): UserHasRole[] {
  const userHasRoles: UserHasRole[] = [];

  for (const user of users) {
    // Validate that user_id exists in registry
    if (!registry.exists('users', user.user_id)) {
      throw new Error(`User ID ${user.user_id} not found in registry`);
    }

    // Validate that role_name exists in registry
    if (!registry.exists('roles', user.role)) {
      throw new Error(`Role ${user.role} not found in registry`);
    }

    const userHasRole: UserHasRole = {
      user_id: user.user_id,
      role_name: user.role
    };

    userHasRoles.push(userHasRole);
  }

  return userHasRoles;
}

/**
 * Validate that all foreign keys exist in registries
 */
export function validateUserHasRoleForeignKeys(
  userHasRoles: UserHasRole[],
  registry: IDRegistry
): boolean {
  for (const uhr of userHasRoles) {
    // Check user_id exists
    if (!registry.exists('users', uhr.user_id)) {
      console.error(`Invalid user_id: ${uhr.user_id}`);
      return false;
    }

    // Check role_name exists
    if (!registry.exists('roles', uhr.role_name)) {
      console.error(`Invalid role_name: ${uhr.role_name}`);
      return false;
    }
  }

  return true;
}

/**
 * Get user_has_role records by role
 */
export function getUserHasRoleByRole(
  userHasRoles: UserHasRole[],
  roleName: string
): UserHasRole[] {
  return userHasRoles.filter(uhr => uhr.role_name === roleName);
}

/**
 * Get user_has_role records by user
 */
export function getUserHasRoleByUser(
  userHasRoles: UserHasRole[],
  userId: string
): UserHasRole[] {
  return userHasRoles.filter(uhr => uhr.user_id === userId);
}

/**
 * Validate that each user has exactly one role
 */
export function validateOneRolePerUser(
  userHasRoles: UserHasRole[],
  userCount: number
): boolean {
  // Count unique user IDs
  const uniqueUserIds = new Set(userHasRoles.map(uhr => uhr.user_id));
  
  // Should have exactly one role per user
  return uniqueUserIds.size === userCount && userHasRoles.length === userCount;
}
