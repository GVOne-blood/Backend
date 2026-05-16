/**
 * UUID Generator Utility
 * 
 * Generates UUID v4 identifiers for database records.
 * UUID v4 uses random numbers and provides sufficient uniqueness for seed data.
 * 
 * Requirements: 4.3, 8.2
 */

import { v4 as uuidv4 } from 'uuid';

/**
 * Generate a UUID v4 identifier
 * 
 * @returns UUID v4 string in format: xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx
 * 
 * @example
 * const id = generateUUID();
 * // Returns: "550e8400-e29b-41d4-a716-446655440000"
 */
export function generateUUID(): string {
  return uuidv4();
}

/**
 * Generate multiple UUIDs at once
 * 
 * @param count - Number of UUIDs to generate
 * @returns Array of UUID v4 strings
 * 
 * @example
 * const ids = generateUUIDs(5);
 * // Returns: ["uuid1", "uuid2", "uuid3", "uuid4", "uuid5"]
 */
export function generateUUIDs(count: number): string[] {
  const uuids: string[] = [];
  for (let i = 0; i < count; i++) {
    uuids.push(uuidv4());
  }
  return uuids;
}

/**
 * Validate if a string is a valid UUID v4
 * 
 * @param uuid - String to validate
 * @returns true if valid UUID v4, false otherwise
 */
export function isValidUUID(uuid: string): boolean {
  const uuidV4Regex = /^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i;
  return uuidV4Regex.test(uuid);
}
