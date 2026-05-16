/**
 * ID Registry Module
 * 
 * Maintains referential integrity by tracking all generated IDs across tables.
 * This ensures foreign keys reference existing primary keys.
 * 
 * Requirements: 1.1, 1.2, 1.3, 1.4, 1.5, 9.1, 9.2, 9.3
 */

export class IDRegistry {
  private registry: Map<string, Map<string, any>>;

  constructor() {
    this.registry = new Map();
  }

  /**
   * Register an ID with associated data for a specific table
   * 
   * @param table - Table name (e.g., 'users', 'shops', 'products')
   * @param id - UUID or string identifier
   * @param data - Associated data for this ID
   */
  register(table: string, id: string, data: any): void {
    if (!this.registry.has(table)) {
      this.registry.set(table, new Map());
    }

    const tableRegistry = this.registry.get(table)!;
    tableRegistry.set(id, data);
  }

  /**
   * Get a random ID from a specific table
   * Useful for generating foreign keys that reference existing records
   * 
   * @param table - Table name
   * @returns Random UUID from the table's registry
   * @throws Error if table doesn't exist or has no IDs
   */
  getRandomId(table: string): string {
    if (!this.registry.has(table)) {
      throw new Error(`Table '${table}' not found in registry`);
    }

    const tableRegistry = this.registry.get(table)!;
    const ids = Array.from(tableRegistry.keys());

    if (ids.length === 0) {
      throw new Error(`No IDs registered for table '${table}'`);
    }

    const randomIndex = Math.floor(Math.random() * ids.length);
    return ids[randomIndex];
  }

  /**
   * Get all IDs for a specific table
   * 
   * @param table - Table name
   * @returns Array of all UUIDs for the table
   */
  getAllIds(table: string): string[] {
    if (!this.registry.has(table)) {
      return [];
    }

    const tableRegistry = this.registry.get(table)!;
    return Array.from(tableRegistry.keys());
  }

  /**
   * Check if an ID exists in a specific table
   * 
   * @param table - Table name
   * @param id - UUID to check
   * @returns true if ID exists, false otherwise
   */
  exists(table: string, id: string): boolean {
    if (!this.registry.has(table)) {
      return false;
    }

    const tableRegistry = this.registry.get(table)!;
    return tableRegistry.has(id);
  }

  /**
   * Get data associated with a specific ID
   * 
   * @param table - Table name
   * @param id - UUID to retrieve data for
   * @returns Associated data or undefined if not found
   */
  getData(table: string, id: string): any | undefined {
    if (!this.registry.has(table)) {
      return undefined;
    }

    const tableRegistry = this.registry.get(table)!;
    return tableRegistry.get(id);
  }

  /**
   * Get the count of registered IDs for a table
   * 
   * @param table - Table name
   * @returns Number of registered IDs
   */
  getCount(table: string): number {
    if (!this.registry.has(table)) {
      return 0;
    }

    const tableRegistry = this.registry.get(table)!;
    return tableRegistry.size;
  }

  /**
   * Clear all registered IDs for a specific table
   * 
   * @param table - Table name
   */
  clear(table: string): void {
    this.registry.delete(table);
  }

  /**
   * Clear all registered IDs across all tables
   */
  clearAll(): void {
    this.registry.clear();
  }

  /**
   * Get all table names in the registry
   * 
   * @returns Array of table names
   */
  getTables(): string[] {
    return Array.from(this.registry.keys());
  }

  /**
   * Get all data for a specific table
   * 
   * @param table - Table name
   * @returns Array of all data records for the table
   */
  getAllData(table: string): any[] {
    if (!this.registry.has(table)) {
      return [];
    }

    const tableRegistry = this.registry.get(table)!;
    return Array.from(tableRegistry.values());
  }
}
