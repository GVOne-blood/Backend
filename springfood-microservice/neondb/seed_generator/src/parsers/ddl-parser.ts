import { Parser } from 'node-sql-parser';
import * as fs from 'fs';
import * as path from 'path';

/**
 * Metadata for a single column in a table
 */
export interface ColumnMetadata {
  columnName: string;
  dataType: string;
  isNullable: boolean;
  isPrimaryKey: boolean;
  defaultValue?: string;
  precision?: number;
  scale?: number;
}

/**
 * Foreign key relationship metadata
 */
export interface ForeignKey {
  columnName: string;
  referencedTable: string;
  referencedColumn: string;
  constraintName?: string;
}

/**
 * Complete metadata for a database table
 */
export interface TableMetadata {
  tableName: string;
  schema: string;
  columns: ColumnMetadata[];
  foreignKeys: ForeignKey[];
  primaryKeys: string[];
}

/**
 * Parses a single DDL file and extracts table metadata
 * 
 * @param filePath - Path to the DDL SQL file
 * @returns TableMetadata object containing table structure and relationships
 * 
 * **Validates: Requirements 1.1, 1.2, 1.3, 1.4, 8.1**
 */
export function parseDDLFile(filePath: string): TableMetadata {
  // Read the SQL file
  const sqlContent = fs.readFileSync(filePath, 'utf-8');
  
  // Extract schema from file path (e.g., springfood_product from path)
  const pathParts = filePath.split(path.sep);
  const schemaFolder = pathParts[pathParts.length - 2]; // e.g., "springfood_product"
  const fileName = path.basename(filePath, '.sql'); // e.g., "products"
  
  // Initialize parser
  const parser = new Parser();
  
  try {
    // Parse the SQL
    const ast: any = parser.astify(sqlContent, { database: 'PostgreSQL' });
    
    // Handle both single statement and array of statements
    const statements = Array.isArray(ast) ? ast : [ast];
    
    // Find the CREATE TABLE statement
    const createTableStmt: any = statements.find(
      (stmt: any) => stmt.type === 'create' && stmt.keyword === 'table'
    );
    
    if (!createTableStmt) {
      throw new Error(`No CREATE TABLE statement found in ${filePath}`);
    }
    
    // Extract table name
    const tableName = typeof createTableStmt.table === 'string' 
      ? createTableStmt.table 
      : createTableStmt.table[0]?.table || fileName;
    
    // Extract columns
    const columns: ColumnMetadata[] = [];
    const primaryKeys: string[] = [];
    const foreignKeys: ForeignKey[] = [];
    
    // Process column definitions
    if (createTableStmt.create_definitions) {
      for (const def of createTableStmt.create_definitions) {
        if (def.resource === 'column') {
          // Extract column metadata
          const columnName = def.column.column;
          const dataType = extractDataType(def.definition);
          const isNullable = !def.nullable || def.nullable.type !== 'not null';
          const isPrimaryKey = hasConstraint(def, 'primary key');
          
          if (isPrimaryKey) {
            primaryKeys.push(columnName);
          }
          
          // Extract precision and scale for numeric types
          let precision: number | undefined;
          let scale: number | undefined;
          if (def.definition.length) {
            precision = def.definition.length;
            scale = def.definition.scale;
          }
          
          columns.push({
            columnName,
            dataType,
            isNullable,
            isPrimaryKey,
            precision,
            scale
          });
          
          // Extract foreign key from column constraint
          const fk = extractForeignKeyFromColumn(def, columnName);
          if (fk) {
            foreignKeys.push(fk);
          }
        } else if (def.resource === 'constraint') {
          // Handle table-level constraints
          if (def.constraint_type === 'primary key') {
            // Add primary key columns
            if (def.definition) {
              for (const col of def.definition) {
                if (col.column) {
                  primaryKeys.push(col.column);
                }
              }
            }
          } else if (def.constraint_type === 'foreign key') {
            // Extract foreign key from table-level constraint
            const fk = extractForeignKeyFromConstraint(def);
            if (fk) {
              foreignKeys.push(fk);
            }
          }
        }
      }
    }
    
    return {
      tableName,
      schema: schemaFolder,
      columns,
      foreignKeys,
      primaryKeys
    };
    
  } catch (error) {
    // If node-sql-parser fails, fall back to regex-based parsing
    console.warn(`Parser failed for ${filePath}, using fallback regex parser:`, error);
    return parseDDLFileWithRegex(filePath, sqlContent, schemaFolder, fileName);
  }
}

/**
 * Fallback regex-based parser for DDL files when node-sql-parser fails
 */
function parseDDLFileWithRegex(
  filePath: string,
  sqlContent: string,
  schema: string,
  fileName: string
): TableMetadata {
  const columns: ColumnMetadata[] = [];
  const primaryKeys: string[] = [];
  const foreignKeys: ForeignKey[] = [];
  
  // Extract table name from CREATE TABLE statement
  const tableNameMatch = sqlContent.match(/create\s+table\s+(?:"?(\w+)"?)/i);
  const tableName = tableNameMatch ? tableNameMatch[1] : fileName;
  
  // Extract column definitions
  const createTableMatch = sqlContent.match(/create\s+table[^(]*\(([\s\S]*?)\);/i);
  if (!createTableMatch) {
    throw new Error(`Could not parse CREATE TABLE statement in ${filePath}`);
  }
  
  const columnSection = createTableMatch[1];
  
  // Split by commas, but be careful with commas inside parentheses
  const columnDefs = splitByCommaOutsideParens(columnSection);
  
  for (let columnDef of columnDefs) {
    columnDef = columnDef.trim();
    
    // Skip empty lines
    if (!columnDef) {
      continue;
    }
    
    // Skip ALTER TABLE and other non-column lines
    if (columnDef.toLowerCase().startsWith('alter') || columnDef.toLowerCase().startsWith('owner')) {
      continue;
    }
    
    // Check if it's a column definition (starts with column name)
    // Handle both single-line and multi-line formats with whitespace
    // Remove all newlines and extra whitespace first
    const normalizedDef = columnDef.replace(/\s+/g, ' ').trim();
    const columnMatch = normalizedDef.match(/^"?(\w+)"?\s+(\w+(?:\([^)]+\))?)/i);
    if (columnMatch) {
      const columnName = columnMatch[1];
      const dataTypeRaw = columnMatch[2];
      
      // Parse data type
      const dataType = parseDataType(dataTypeRaw);
      
      // Check for NOT NULL (can be on same line or next line)
      const isNullable = !normalizedDef.toLowerCase().includes('not null');
      
      // Check for PRIMARY KEY (can be on same line or next line)
      const isPrimaryKey = normalizedDef.toLowerCase().includes('primary key');
      
      if (isPrimaryKey) {
        primaryKeys.push(columnName);
      }
      
      // Extract precision and scale
      const precisionMatch = dataTypeRaw.match(/\((\d+)(?:,\s*(\d+))?\)/);
      let precision: number | undefined;
      let scale: number | undefined;
      if (precisionMatch) {
        precision = parseInt(precisionMatch[1]);
        if (precisionMatch[2]) {
          scale = parseInt(precisionMatch[2]);
        }
      }
      
      columns.push({
        columnName,
        dataType,
        isNullable,
        isPrimaryKey,
        precision,
        scale
      });
      
      // Extract foreign key from constraint clause
      // Pattern: constraint <name> references <table> or references <table>(<column>)
      const fkMatch = normalizedDef.match(/(?:constraint\s+\w+\s+)?references\s+"?(\w+)"?(?:\s*\((\w+)\))?/i);
      if (fkMatch) {
        foreignKeys.push({
          columnName,
          referencedTable: fkMatch[1],
          referencedColumn: fkMatch[2] || columnName, // Default to same column name if not specified
          constraintName: normalizedDef.match(/constraint\s+(\w+)/i)?.[1]
        });
      }
    }
  }
  
  return {
    tableName,
    schema,
    columns,
    foreignKeys,
    primaryKeys
  };
}

/**
 * Splits a string by commas that are outside of parentheses
 */
function splitByCommaOutsideParens(str: string): string[] {
  const result: string[] = [];
  let current = '';
  let parenDepth = 0;
  
  for (let i = 0; i < str.length; i++) {
    const char = str[i];
    
    if (char === '(') {
      parenDepth++;
      current += char;
    } else if (char === ')') {
      parenDepth--;
      current += char;
    } else if (char === ',' && parenDepth === 0) {
      result.push(current.trim());
      current = '';
    } else {
      current += char;
    }
  }
  
  if (current.trim()) {
    result.push(current.trim());
  }
  
  return result;
}

/**
 * Extracts data type from AST definition
 */
function extractDataType(definition: any): string {
  if (typeof definition === 'string') {
    return definition.toUpperCase();
  }
  
  if (definition.dataType) {
    let type = definition.dataType.toUpperCase();
    
    // Add length/precision if present
    if (definition.length) {
      if (definition.scale !== undefined) {
        type += `(${definition.length},${definition.scale})`;
      } else {
        type += `(${definition.length})`;
      }
    }
    
    return type;
  }
  
  return 'UNKNOWN';
}

/**
 * Parses data type string to normalized format
 */
function parseDataType(dataTypeRaw: string): string {
  // Extract base type and parameters
  const match = dataTypeRaw.match(/^(\w+)(?:\(([^)]+)\))?/i);
  if (!match) {
    return dataTypeRaw.toUpperCase();
  }
  
  const baseType = match[1].toUpperCase();
  const params = match[2];
  
  if (params) {
    return `${baseType}(${params})`;
  }
  
  return baseType;
}

/**
 * Checks if a column definition has a specific constraint
 */
function hasConstraint(columnDef: any, constraintType: string): boolean {
  if (!columnDef.unique_or_primary) {
    return false;
  }
  
  return columnDef.unique_or_primary.toLowerCase() === constraintType.toLowerCase();
}

/**
 * Extracts foreign key information from a column definition
 */
function extractForeignKeyFromColumn(columnDef: any, columnName: string): ForeignKey | null {
  if (!columnDef.reference_definition) {
    return null;
  }
  
  const refDef = columnDef.reference_definition;
  const referencedTable = typeof refDef.table === 'string' 
    ? refDef.table 
    : refDef.table[0]?.table;
  
  // Extract referenced column (defaults to same column name if not specified)
  let referencedColumn = columnName;
  if (refDef.definition && refDef.definition.length > 0) {
    referencedColumn = refDef.definition[0].column;
  }
  
  // Extract constraint name
  const constraintName = columnDef.constraint;
  
  return {
    columnName,
    referencedTable,
    referencedColumn,
    constraintName
  };
}

/**
 * Extracts foreign key information from a table-level constraint
 */
function extractForeignKeyFromConstraint(constraintDef: any): ForeignKey | null {
  if (constraintDef.constraint_type !== 'foreign key') {
    return null;
  }
  
  // Extract column name
  const columnName = constraintDef.definition && constraintDef.definition.length > 0
    ? constraintDef.definition[0].column
    : '';
  
  // Extract referenced table and column
  const refDef = constraintDef.reference_definition;
  if (!refDef) {
    return null;
  }
  
  const referencedTable = typeof refDef.table === 'string' 
    ? refDef.table 
    : refDef.table[0]?.table;
  
  const referencedColumn = refDef.definition && refDef.definition.length > 0
    ? refDef.definition[0].column
    : columnName;
  
  const constraintName = constraintDef.constraint;
  
  return {
    columnName,
    referencedTable,
    referencedColumn,
    constraintName
  };
}

/**
 * Parses all DDL files from a directory and returns table metadata for all tables
 * 
 * @param directory - Path to the directory containing DDL files (e.g., "neondb/")
 * @returns Array of TableMetadata objects for all tables
 * 
 * **Validates: Requirements 1.1, 1.2, 1.3, 1.4, 8.1**
 */
export function parseAllDDLFiles(directory: string): TableMetadata[] {
  const tables: TableMetadata[] = [];
  
  // Get all schema directories
  const schemaFolders = fs.readdirSync(directory, { withFileTypes: true })
    .filter(dirent => dirent.isDirectory() && dirent.name.startsWith('springfood_'))
    .map(dirent => dirent.name);
  
  // Parse DDL files from each schema
  for (const schemaFolder of schemaFolders) {
    const schemaPath = path.join(directory, schemaFolder);
    const sqlFiles = fs.readdirSync(schemaPath)
      .filter(file => file.endsWith('.sql'));
    
    for (const sqlFile of sqlFiles) {
      const filePath = path.join(schemaPath, sqlFile);
      try {
        const tableMetadata = parseDDLFile(filePath);
        tables.push(tableMetadata);
      } catch (error) {
        console.error(`Error parsing ${filePath}:`, error);
        // Continue with other files
      }
    }
  }
  
  return tables;
}
