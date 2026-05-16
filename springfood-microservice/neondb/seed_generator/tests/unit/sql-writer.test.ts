/**
 * Unit Tests for SQL Writer Module
 * 
 * Tests INSERT statement formatting, special character escaping,
 * transaction block generation, and JSONB formatting.
 * 
 * Requirements: 8.1, 8.6, 6.7
 */

import { SQLWriter, WriteOptions, DEFAULT_WRITE_OPTIONS } from '../../src/writers/sql-writer';

describe('SQLWriter', () => {
  let writer: SQLWriter;

  beforeEach(() => {
    writer = new SQLWriter();
  });

  describe('formatValue', () => {
    it('should format UUID values correctly', () => {
      const uuid = '123e4567-e89b-12d3-a456-426614174000';
      const result = writer.formatValue(uuid, 'UUID');
      expect(result).toBe(`'${uuid}'`);
    });

    it('should format VARCHAR values with proper escaping', () => {
      const text = "O'Reilly's Book";
      const result = writer.formatValue(text, 'VARCHAR');
      expect(result).toBe("'O''Reilly''s Book'");
    });

    it('should format NUMERIC values with correct precision', () => {
      const price = 125000.50;
      const result = writer.formatValue(price, 'NUMERIC');
      expect(result).toBe('125000.50');
    });

    it('should format INTEGER values without decimals', () => {
      const quantity = 42;
      const result = writer.formatValue(quantity, 'INTEGER');
      expect(result).toBe('42');
    });

    it('should format TIMESTAMP values in ISO format', () => {
      const date = new Date('2024-01-15T10:30:00Z');
      const result = writer.formatValue(date, 'TIMESTAMP');
      expect(result).toBe("'2024-01-15T10:30:00.000Z'");
    });

    it('should format BOOLEAN values as TRUE/FALSE', () => {
      expect(writer.formatValue(true, 'BOOLEAN')).toBe('TRUE');
      expect(writer.formatValue(false, 'BOOLEAN')).toBe('FALSE');
    });

    it('should format JSONB values with proper syntax', () => {
      const jsonArray = '["url1", "url2", "url3"]';
      const result = writer.formatValue(jsonArray, 'JSONB');
      expect(result).toBe("'[\"url1\", \"url2\", \"url3\"]'::jsonb");
    });

    it('should format JSONB objects with proper syntax', () => {
      const jsonObj = { name: 'Test', value: 123 };
      const result = writer.formatValue(jsonObj, 'JSONB');
      expect(result).toBe("'{\"name\":\"Test\",\"value\":123}'::jsonb");
    });

    it('should handle NULL values', () => {
      expect(writer.formatValue(null, 'VARCHAR')).toBe('NULL');
      expect(writer.formatValue(undefined, 'VARCHAR')).toBe('NULL');
    });
  });

  describe('escapeString', () => {
    it('should escape single quotes by doubling them', () => {
      const text = "It's a test";
      const result = writer.escapeString(text);
      expect(result).toBe("It''s a test");
    });

    it('should escape backslashes', () => {
      const text = 'C:\\Users\\Admin';
      const result = writer.escapeString(text);
      expect(result).toBe('C:\\\\Users\\\\Admin');
    });

    it('should handle multiple special characters', () => {
      const text = "O'Reilly's C:\\Path";
      const result = writer.escapeString(text);
      expect(result).toBe("O''Reilly''s C:\\\\Path");
    });

    it('should handle empty strings', () => {
      expect(writer.escapeString('')).toBe('');
    });

    it('should handle null and undefined', () => {
      expect(writer.escapeString(null as any)).toBe('');
      expect(writer.escapeString(undefined as any)).toBe('');
    });
  });

  describe('writeComment', () => {
    it('should format SQL comments correctly', () => {
      const comment = writer.writeComment('This is a test comment');
      expect(comment).toBe('-- This is a test comment');
    });
  });

  describe('writeTransactionBlock', () => {
    it('should wrap statements in BEGIN/COMMIT block', () => {
      const statements = [
        'INSERT INTO test (id) VALUES (1);',
        'INSERT INTO test (id) VALUES (2);'
      ];
      
      const result = writer.writeTransactionBlock(statements);
      
      expect(result).toContain('BEGIN;');
      expect(result).toContain('INSERT INTO test (id) VALUES (1);');
      expect(result).toContain('INSERT INTO test (id) VALUES (2);');
      expect(result).toContain('COMMIT;');
    });

    it('should handle empty statement array', () => {
      const result = writer.writeTransactionBlock([]);
      expect(result).toContain('BEGIN;');
      expect(result).toContain('COMMIT;');
    });
  });

  describe('writeInsertStatements', () => {
    it('should generate INSERT statements with schema-qualified table names', () => {
      const records = [
        { id: '123e4567-e89b-12d3-a456-426614174000', name: 'Test' }
      ];
      
      const options: WriteOptions = {
        includeTransaction: false,
        includeComments: false
      };
      
      const result = writer.writeInsertStatements(
        'springfood_authentication',
        'role',
        records,
        options
      );
      
      expect(result).toContain('INSERT INTO springfood_authentication.role');
      expect(result).toContain('(id, name)');
      expect(result).toContain("'123e4567-e89b-12d3-a456-426614174000'");
      expect(result).toContain("'Test'");
    });

    it('should include comments when requested', () => {
      const records = [
        { id: 1, name: 'Test' }
      ];
      
      const options: WriteOptions = {
        includeTransaction: false,
        includeComments: true
      };
      
      const result = writer.writeInsertStatements(
        'springfood_authentication',
        'role',
        records,
        options
      );
      
      expect(result).toContain('-- Inserting 1 records into springfood_authentication.role');
    });

    it('should add ON CONFLICT clause when specified', () => {
      const records = [
        { id: 1, name: 'Test' }
      ];
      
      const options: WriteOptions = {
        includeTransaction: false,
        includeComments: false,
        onConflict: 'DO NOTHING'
      };
      
      const result = writer.writeInsertStatements(
        'springfood_authentication',
        'role',
        records,
        options
      );
      
      expect(result).toContain('ON CONFLICT DO NOTHING;');
    });

    it('should handle multiple records with proper comma separation', () => {
      const records = [
        { id: 1, name: 'Test1' },
        { id: 2, name: 'Test2' },
        { id: 3, name: 'Test3' }
      ];
      
      const options: WriteOptions = {
        includeTransaction: false,
        includeComments: false
      };
      
      const result = writer.writeInsertStatements(
        'springfood_test',
        'test_table',
        records,
        options
      );
      
      // Should have commas between records but not after the last one
      const lines = result.split('\n').filter(line => line.trim().startsWith('('));
      expect(lines[0]).toContain('),');
      expect(lines[1]).toContain('),');
      expect(lines[2]).toContain(')'); // Last record should not have comma
      expect(lines[2]).not.toContain('),');
    });

    it('should handle various data types correctly', () => {
      const records = [
        {
          user_id: '123e4567-e89b-12d3-a456-426614174000',
          name: 'John Doe',
          age: 30,
          price: 125000.50,
          is_active: true,
          created_at: new Date('2024-01-15T10:30:00Z'),
          metadata: '{"key": "value"}'
        }
      ];
      
      const options: WriteOptions = {
        includeTransaction: false,
        includeComments: false
      };
      
      const result = writer.writeInsertStatements(
        'springfood_test',
        'users',
        records,
        options
      );
      
      expect(result).toContain("'123e4567-e89b-12d3-a456-426614174000'"); // UUID
      expect(result).toContain("'John Doe'"); // VARCHAR
      expect(result).toContain('30'); // INTEGER
      expect(result).toContain('125000.50'); // NUMERIC
      expect(result).toContain('TRUE'); // BOOLEAN
      expect(result).toContain("'2024-01-15T10:30:00.000Z'"); // TIMESTAMP
      expect(result).toContain("'{\"key\": \"value\"}'::jsonb"); // JSONB
    });

    it('should return empty string for empty records array', () => {
      const options: WriteOptions = {
        includeTransaction: false,
        includeComments: false
      };
      
      const result = writer.writeInsertStatements(
        'springfood_test',
        'test_table',
        [],
        options
      );
      
      expect(result).toBe('');
    });
  });

  describe('generateSQLFile', () => {
    it('should generate complete SQL file with multiple tables', () => {
      const tables = [
        {
          table: 'role',
          records: [
            { role_name: 'ADMIN', description: 'Administrator' },
            { role_name: 'USER', description: 'Regular user' }
          ]
        },
        {
          table: 'user',
          records: [
            { user_id: '123e4567-e89b-12d3-a456-426614174000', name: 'John' }
          ]
        }
      ];
      
      const options: WriteOptions = {
        includeTransaction: true,
        includeComments: true,
        onConflict: 'DO NOTHING'
      };
      
      const result = writer.generateSQLFile(
        'springfood_authentication',
        tables,
        options
      );
      
      expect(result).toContain('BEGIN;');
      expect(result).toContain('-- Seed data for schema: springfood_authentication');
      expect(result).toContain('INSERT INTO springfood_authentication.role');
      expect(result).toContain('INSERT INTO springfood_authentication.user');
      expect(result).toContain('COMMIT;');
    });

    it('should skip empty tables', () => {
      const tables = [
        {
          table: 'role',
          records: [
            { role_name: 'ADMIN', description: 'Administrator' }
          ]
        },
        {
          table: 'empty_table',
          records: []
        }
      ];
      
      const options: WriteOptions = {
        includeTransaction: false,
        includeComments: false
      };
      
      const result = writer.generateSQLFile(
        'springfood_test',
        tables,
        options
      );
      
      expect(result).toContain('INSERT INTO springfood_test.role');
      expect(result).not.toContain('empty_table');
    });
  });

  describe('Special character escaping in real-world scenarios', () => {
    it('should handle Vietnamese text with special characters', () => {
      const records = [
        {
          name: "Phở Bò Tái",
          description: "Phở bò tái là món ăn truyền thống của Việt Nam"
        }
      ];
      
      const options: WriteOptions = {
        includeTransaction: false,
        includeComments: false
      };
      
      const result = writer.writeInsertStatements(
        'springfood_product',
        'products',
        records,
        options
      );
      
      expect(result).toContain("'Phở Bò Tái'");
      expect(result).toContain("'Phở bò tái là món ăn truyền thống của Việt Nam'");
    });

    it('should handle product descriptions with quotes', () => {
      const records = [
        {
          name: "Bánh Mì",
          description: "Bánh mì 'đặc biệt' với thịt nguội"
        }
      ];
      
      const options: WriteOptions = {
        includeTransaction: false,
        includeComments: false
      };
      
      const result = writer.writeInsertStatements(
        'springfood_product',
        'products',
        records,
        options
      );
      
      expect(result).toContain("'Bánh mì ''đặc biệt'' với thịt nguội'");
    });

    it('should handle JSONB arrays for product images', () => {
      const records = [
        {
          product_id: '123e4567-e89b-12d3-a456-426614174000',
          images: '["https://example.com/image1.jpg", "https://example.com/image2.jpg"]'
        }
      ];
      
      const options: WriteOptions = {
        includeTransaction: false,
        includeComments: false
      };
      
      const result = writer.writeInsertStatements(
        'springfood_product',
        'products',
        records,
        options
      );
      
      expect(result).toContain("'[\"https://example.com/image1.jpg\", \"https://example.com/image2.jpg\"]'::jsonb");
    });
  });

  describe('NeonDB compatibility', () => {
    it('should use proper PostgreSQL timestamp format', () => {
      const records = [
        {
          created_at: new Date('2024-01-15T10:30:00Z'),
          updated_at: new Date('2024-01-15T10:30:00Z')
        }
      ];
      
      const options: WriteOptions = {
        includeTransaction: false,
        includeComments: false
      };
      
      const result = writer.writeInsertStatements(
        'springfood_test',
        'test_table',
        records,
        options
      );
      
      // Should use ISO 8601 format
      expect(result).toContain("'2024-01-15T10:30:00.000Z'");
    });

    it('should use proper NUMERIC format for prices', () => {
      const records = [
        {
          price: 125000.00,
          discount: 15.50
        }
      ];
      
      const options: WriteOptions = {
        includeTransaction: false,
        includeComments: false
      };
      
      const result = writer.writeInsertStatements(
        'springfood_product',
        'products',
        records,
        options
      );
      
      expect(result).toContain('125000.00');
      expect(result).toContain('15.50');
    });
  });
});
