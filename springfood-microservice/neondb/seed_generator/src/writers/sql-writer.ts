/**
 * SQL Writer Module
 * 
 * Writes INSERT statements with proper formatting for NeonDB PostgreSQL.
 * Handles various data types: UUID, VARCHAR, NUMERIC, TIMESTAMP, BOOLEAN, JSONB.
 * 
 * Requirements: 4.3, 6.4, 6.5, 8.1, 8.2, 8.3, 8.4, 8.5, 8.6, 6.7, 6.8
 */

export interface WriteOptions {
  includeTransaction: boolean;
  includeComments: boolean;
  onConflict?: 'DO NOTHING' | 'DO UPDATE';
  batchSize?: number;
}

export class SQLWriter {
  /**
   * Write INSERT statements for a table
   * 
   * @param schema - Schema name (e.g., 'springfood_authentication')
   * @param table - Table name (e.g., 'user')
   * @param records - Array of records to insert
   * @param options - Write options
   * @returns SQL INSERT statements as string
   * 
   * Requirements:
   * - 6.4: Include schema qualification in INSERT statements
   * - 8.1: Use PostgreSQL syntax compatible with NeonDB
   * - 8.6: Use proper escaping for special characters
   */
  writeInsertStatements(
    schema: string,
    table: string,
    records: any[],
    options: WriteOptions
  ): string {
    if (records.length === 0) {
      return '';
    }

    const lines: string[] = [];

    // Add comment if requested
    if (options.includeComments) {
      lines.push(this.writeComment(`Inserting ${records.length} records into ${schema}.${table}`));
      lines.push('');
    }

    // Get column names from first record
    const columns = Object.keys(records[0]);
    const columnList = columns.join(', ');

    // Build INSERT statement
    const insertPrefix = `INSERT INTO ${schema}.${table} (${columnList})`;
    lines.push(insertPrefix);
    lines.push('VALUES');

    // Add values for each record
    const batchSize = options.batchSize || records.length;
    
    for (let i = 0; i < records.length; i += batchSize) {
      const batch = records.slice(i, Math.min(i + batchSize, records.length));
      
      for (let j = 0; j < batch.length; j++) {
        const record = batch[j];
        const values = columns.map(col => this.formatValue(record[col], this.inferDataType(record[col])));
        const valuesStr = `  (${values.join(', ')})`;
        
        // Add comma if not the last record
        if (i + j < records.length - 1) {
          lines.push(valuesStr + ',');
        } else {
          lines.push(valuesStr);
        }
      }
    }

    // Add ON CONFLICT clause if specified
    if (options.onConflict) {
      lines.push(`ON CONFLICT ${options.onConflict};`);
    } else {
      lines.push(';');
    }

    lines.push('');

    return lines.join('\n');
  }

  /**
   * Wrap statements in a transaction block
   * 
   * @param statements - Array of SQL statements
   * @returns SQL with BEGIN/COMMIT block
   * 
   * Requirements:
   * - 6.7: Use transaction blocks (BEGIN/COMMIT)
   */
  writeTransactionBlock(statements: string[]): string {
    const lines: string[] = [];
    
    lines.push('BEGIN;');
    lines.push('');
    
    for (const statement of statements) {
      lines.push(statement);
    }
    
    lines.push('COMMIT;');
    lines.push('');
    
    return lines.join('\n');
  }

  /**
   * Write a SQL comment
   * 
   * @param text - Comment text
   * @returns SQL comment line
   */
  writeComment(text: string): string {
    return `-- ${text}`;
  }

  /**
   * Escape special characters in strings
   * 
   * @param value - String value to escape
   * @returns Escaped string
   * 
   * Requirements:
   * - 8.6: Use proper escaping for special characters (single quotes, backslashes)
   */
  escapeString(value: string): string {
    if (value === null || value === undefined) {
      return '';
    }

    // Escape single quotes by doubling them (PostgreSQL standard)
    let escaped = value.replace(/'/g, "''");
    
    // Escape backslashes
    escaped = escaped.replace(/\\/g, '\\\\');
    
    return escaped;
  }

  /**
   * Format a value based on its data type
   * 
   * @param value - Value to format
   * @param dataType - Data type (UUID, VARCHAR, NUMERIC, TIMESTAMP, BOOLEAN, JSONB)
   * @returns Formatted SQL value
   * 
   * Requirements:
   * - 4.3: Handle various data types
   * - 8.2: Use gen_random_uuid() for UUID generation
   * - 8.3: Use proper timestamp with time zone format
   * - 8.4: Use proper JSONB syntax
   * - 8.5: Avoid using PostgreSQL extensions not available in NeonDB
   */
  formatValue(value: any, dataType: string): string {
    // Handle NULL values
    if (value === null || value === undefined) {
      return 'NULL';
    }

    switch (dataType.toUpperCase()) {
      case 'UUID':
        // UUIDs are already generated, just wrap in quotes
        return `'${value}'`;

      case 'VARCHAR':
      case 'TEXT':
      case 'STRING':
        return `'${this.escapeString(String(value))}'`;

      case 'NUMERIC':
      case 'DECIMAL':
      case 'NUMBER':
        // Format numeric values with proper precision
        if (typeof value === 'number') {
          // Always format with 2 decimal places for consistency
          return Number(value).toFixed(2);
        }
        // If it's a string, try to parse and format
        const numValue = parseFloat(String(value));
        if (!isNaN(numValue)) {
          return numValue.toFixed(2);
        }
        return String(value);

      case 'INTEGER':
      case 'INT':
      case 'BIGINT':
        return String(Math.floor(Number(value)));

      case 'TIMESTAMP':
      case 'TIMESTAMPTZ':
      case 'DATE':
      case 'DATETIME':
        // Format timestamp for PostgreSQL
        if (value instanceof Date) {
          return `'${value.toISOString()}'`;
        }
        return `'${new Date(value).toISOString()}'`;

      case 'BOOLEAN':
      case 'BOOL':
        return value ? 'TRUE' : 'FALSE';

      case 'JSONB':
      case 'JSON':
        // Format JSONB with proper syntax
        if (typeof value === 'string') {
          // Already a JSON string, escape and cast
          return `'${this.escapeString(value)}'::jsonb`;
        } else {
          // Convert object to JSON string
          return `'${this.escapeString(JSON.stringify(value))}'::jsonb`;
        }

      default:
        // Default: treat as string
        return `'${this.escapeString(String(value))}'`;
    }
  }

  /**
   * Infer data type from value
   * 
   * @param value - Value to infer type from
   * @returns Inferred data type
   */
  private inferDataType(value: any): string {
    if (value === null || value === undefined) {
      return 'NULL';
    }

    if (typeof value === 'boolean') {
      return 'BOOLEAN';
    }

    if (typeof value === 'number') {
      // Always treat numbers as NUMERIC to preserve decimal places
      // This ensures prices like 125000.00 are formatted correctly
      return 'NUMERIC';
    }

    if (value instanceof Date) {
      return 'TIMESTAMP';
    }

    if (typeof value === 'string') {
      // Check if it's a UUID (format: xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx)
      const uuidRegex = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;
      if (uuidRegex.test(value)) {
        return 'UUID';
      }

      // Check if it's a JSON string (starts with { or [)
      if ((value.startsWith('{') && value.endsWith('}')) || 
          (value.startsWith('[') && value.endsWith(']'))) {
        try {
          JSON.parse(value);
          return 'JSONB';
        } catch {
          // Not valid JSON, treat as string
        }
      }

      // Check if it's a timestamp string
      const timestampRegex = /^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}/;
      if (timestampRegex.test(value)) {
        return 'TIMESTAMP';
      }

      return 'VARCHAR';
    }

    if (typeof value === 'object') {
      // Assume it's a JSON object
      return 'JSONB';
    }

    return 'VARCHAR';
  }

  /**
   * Generate a complete SQL file with transaction block and comments
   * 
   * @param schema - Schema name
   * @param tables - Array of table data { table: string, records: any[] }
   * @param options - Write options
   * @returns Complete SQL file content
   */
  generateSQLFile(
    schema: string,
    tables: Array<{ table: string; records: any[] }>,
    options: WriteOptions
  ): string {
    const statements: string[] = [];

    // Add file header comment
    if (options.includeComments) {
      statements.push(this.writeComment(`Seed data for schema: ${schema}`));
      statements.push(this.writeComment(`Generated at: ${new Date().toISOString()}`));
      statements.push(this.writeComment(`Total tables: ${tables.length}`));
      statements.push('');
    }

    // Generate INSERT statements for each table
    for (const tableData of tables) {
      if (tableData.records.length > 0) {
        const insertStatement = this.writeInsertStatements(
          schema,
          tableData.table,
          tableData.records,
          options
        );
        statements.push(insertStatement);
      }
    }

    // Wrap in transaction if requested
    if (options.includeTransaction) {
      return this.writeTransactionBlock(statements);
    }

    return statements.join('\n');
  }
}

/**
 * Create a new SQL writer instance
 */
export function createSQLWriter(): SQLWriter {
  return new SQLWriter();
}

/**
 * Default write options
 */
export const DEFAULT_WRITE_OPTIONS: WriteOptions = {
  includeTransaction: true,
  includeComments: true,
  onConflict: 'DO NOTHING',
  batchSize: 1000
};
