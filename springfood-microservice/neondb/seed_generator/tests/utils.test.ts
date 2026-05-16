/**
 * Unit Tests for Utility Functions
 * 
 * Tests BCrypt password hashing and UUID generation utilities.
 * Requirements: 4.8, 8.2
 */

import { hashPassword, verifyPassword } from '../src/utils/bcrypt-hasher';
import { generateUUID, generateUUIDs, isValidUUID } from '../src/utils/uuid-generator';

describe('BCrypt Password Hasher', () => {
  describe('hashPassword', () => {
    it('should produce a valid BCrypt hash', () => {
      const plainText = 'Password123!';
      const hash = hashPassword(plainText);

      // BCrypt hash format: $2a$10$... (60 characters total)
      expect(hash).toMatch(/^\$2[aby]\$\d{2}\$.{53}$/);
      expect(hash.length).toBe(60);
    });

    it('should produce different hashes for the same password (due to salt)', () => {
      const plainText = 'Password123!';
      const hash1 = hashPassword(plainText);
      const hash2 = hashPassword(plainText);

      // Hashes should be different due to random salt
      expect(hash1).not.toBe(hash2);
    });

    it('should hash different passwords to different hashes', () => {
      const hash1 = hashPassword('Password1');
      const hash2 = hashPassword('Password2');

      expect(hash1).not.toBe(hash2);
    });

    it('should handle empty string', () => {
      const hash = hashPassword('');

      expect(hash).toMatch(/^\$2[aby]\$\d{2}\$.{53}$/);
      expect(hash.length).toBe(60);
    });

    it('should handle special characters', () => {
      const plainText = 'P@ssw0rd!#$%^&*()';
      const hash = hashPassword(plainText);

      expect(hash).toMatch(/^\$2[aby]\$\d{2}\$.{53}$/);
      expect(hash.length).toBe(60);
    });

    it('should handle Vietnamese characters', () => {
      const plainText = 'Mật_Khẩu_Việt_Nam_123!';
      const hash = hashPassword(plainText);

      expect(hash).toMatch(/^\$2[aby]\$\d{2}\$.{53}$/);
      expect(hash.length).toBe(60);
    });
  });

  describe('verifyPassword', () => {
    it('should verify correct password', () => {
      const plainText = 'Password123!';
      const hash = hashPassword(plainText);

      expect(verifyPassword(plainText, hash)).toBe(true);
    });

    it('should reject incorrect password', () => {
      const plainText = 'Password123!';
      const hash = hashPassword(plainText);

      expect(verifyPassword('WrongPassword', hash)).toBe(false);
    });

    it('should be case-sensitive', () => {
      const plainText = 'Password123!';
      const hash = hashPassword(plainText);

      expect(verifyPassword('password123!', hash)).toBe(false);
      expect(verifyPassword('PASSWORD123!', hash)).toBe(false);
    });

    it('should handle empty string verification', () => {
      const hash = hashPassword('');

      expect(verifyPassword('', hash)).toBe(true);
      expect(verifyPassword('nonempty', hash)).toBe(false);
    });
  });
});

describe('UUID Generator', () => {
  describe('generateUUID', () => {
    it('should generate a valid UUID v4', () => {
      const uuid = generateUUID();

      // UUID v4 format: xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx
      // where y is one of [8, 9, a, b]
      expect(uuid).toMatch(/^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i);
    });

    it('should generate unique UUIDs', () => {
      const uuid1 = generateUUID();
      const uuid2 = generateUUID();
      const uuid3 = generateUUID();

      expect(uuid1).not.toBe(uuid2);
      expect(uuid2).not.toBe(uuid3);
      expect(uuid1).not.toBe(uuid3);
    });

    it('should generate UUIDs with correct version (4)', () => {
      const uuid = generateUUID();
      const versionChar = uuid.charAt(14); // 15th character (0-indexed)

      expect(versionChar).toBe('4');
    });

    it('should generate UUIDs with correct variant', () => {
      const uuid = generateUUID();
      const variantChar = uuid.charAt(19); // 20th character (0-indexed)

      // Variant should be one of [8, 9, a, b]
      expect(['8', '9', 'a', 'b']).toContain(variantChar.toLowerCase());
    });

    it('should generate 100 unique UUIDs', () => {
      const uuids = new Set<string>();

      for (let i = 0; i < 100; i++) {
        uuids.add(generateUUID());
      }

      expect(uuids.size).toBe(100);
    });
  });

  describe('generateUUIDs', () => {
    it('should generate specified number of UUIDs', () => {
      const uuids = generateUUIDs(5);

      expect(uuids).toHaveLength(5);
    });

    it('should generate all valid UUID v4s', () => {
      const uuids = generateUUIDs(10);

      uuids.forEach(uuid => {
        expect(uuid).toMatch(/^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i);
      });
    });

    it('should generate unique UUIDs', () => {
      const uuids = generateUUIDs(20);
      const uniqueUuids = new Set(uuids);

      expect(uniqueUuids.size).toBe(20);
    });

    it('should handle count of 0', () => {
      const uuids = generateUUIDs(0);

      expect(uuids).toEqual([]);
    });

    it('should handle count of 1', () => {
      const uuids = generateUUIDs(1);

      expect(uuids).toHaveLength(1);
      expect(isValidUUID(uuids[0])).toBe(true);
    });

    it('should handle large count', () => {
      const uuids = generateUUIDs(1000);
      const uniqueUuids = new Set(uuids);

      expect(uuids).toHaveLength(1000);
      expect(uniqueUuids.size).toBe(1000);
    });
  });

  describe('isValidUUID', () => {
    it('should validate correct UUID v4', () => {
      const uuid = generateUUID();

      expect(isValidUUID(uuid)).toBe(true);
    });

    it('should validate multiple correct UUIDs', () => {
      const validUuids = [
        '550e8400-e29b-41d4-a716-446655440000',
        '6ba7b810-9dad-41d1-80b4-00c04fd430c8',
        'f47ac10b-58cc-4372-a567-0e02b2c3d479',
        '123e4567-e89b-42d3-a456-426614174000'
      ];

      validUuids.forEach(uuid => {
        expect(isValidUUID(uuid)).toBe(true);
      });
    });

    it('should reject invalid UUID formats', () => {
      const invalidUuids = [
        'not-a-uuid',
        '550e8400-e29b-41d4-a716',
        '550e8400-e29b-41d4-a716-446655440000-extra',
        '550e8400e29b41d4a716446655440000', // Missing hyphens
        '550e8400-e29b-31d4-a716-446655440000', // Wrong version (3 instead of 4)
        '550e8400-e29b-41d4-c716-446655440000', // Wrong variant (c instead of 8/9/a/b)
        '',
        'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'
      ];

      invalidUuids.forEach(uuid => {
        expect(isValidUUID(uuid)).toBe(false);
      });
    });

    it('should be case-insensitive', () => {
      const uuid = '550E8400-E29B-41D4-A716-446655440000';

      expect(isValidUUID(uuid)).toBe(true);
      expect(isValidUUID(uuid.toLowerCase())).toBe(true);
    });

    it('should reject UUIDs with invalid characters', () => {
      const invalidUuids = [
        '550e8400-e29b-41d4-a716-44665544000g', // 'g' is not hex
        '550e8400-e29b-41d4-a716-44665544000z', // 'z' is not hex
        '550e8400-e29b-41d4-a716-44665544000!', // Special character
      ];

      invalidUuids.forEach(uuid => {
        expect(isValidUUID(uuid)).toBe(false);
      });
    });
  });
});
