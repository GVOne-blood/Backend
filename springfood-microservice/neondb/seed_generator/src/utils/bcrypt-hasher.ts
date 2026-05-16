/**
 * BCrypt Password Hasher Utility
 * 
 * Provides secure password hashing using BCrypt algorithm with cost factor 10.
 * This balances security and performance for seed data generation.
 * 
 * Requirements: 4.8, 10.5
 */

import bcrypt from 'bcryptjs';

/**
 * Hash a plain text password using BCrypt
 * 
 * @param plainText - Plain text password to hash
 * @returns BCrypt hashed password string
 * 
 * @example
 * const hashed = hashPassword('Password123!');
 * // Returns: $2a$10$... (60 character BCrypt hash)
 */
export function hashPassword(plainText: string): string {
  // Cost factor of 10 provides good balance between security and performance
  // Higher values increase security but take longer to compute
  const saltRounds = 10;
  
  // Generate salt and hash synchronously (acceptable for seed data generation)
  const salt = bcrypt.genSaltSync(saltRounds);
  const hash = bcrypt.hashSync(plainText, salt);
  
  return hash;
}

/**
 * Verify a plain text password against a BCrypt hash
 * Useful for testing purposes
 * 
 * @param plainText - Plain text password to verify
 * @param hash - BCrypt hash to compare against
 * @returns true if password matches, false otherwise
 */
export function verifyPassword(plainText: string, hash: string): boolean {
  return bcrypt.compareSync(plainText, hash);
}
