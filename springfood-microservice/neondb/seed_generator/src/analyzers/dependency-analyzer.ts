import { TableMetadata, ForeignKey } from '../parsers/ddl-parser';

/**
 * Represents a node in the dependency graph
 */
export interface TableNode {
  tableName: string;
  schema: string;
  foreignKeys: ForeignKey[];
  inDegree: number; // Number of tables that depend on this table
}

/**
 * Dependency graph structure
 */
export interface DependencyGraph {
  nodes: Map<string, TableNode>; // Key: "schema.tableName"
  edges: Map<string, string[]>; // Key: "schema.tableName", Value: [tables this table depends on]
}

/**
 * Analyzes table dependencies and builds a dependency graph
 * 
 * @param tables - Array of table metadata from DDL parser
 * @returns DependencyGraph with nodes and edges representing table dependencies
 * 
 * **Validates: Requirements 1.1, 1.2, 1.3, 1.4, 7.2, 7.3, 7.4, 7.5, 7.6**
 */
export function analyzeDependencies(tables: TableMetadata[]): DependencyGraph {
  const nodes = new Map<string, TableNode>();
  const edges = new Map<string, string[]>();
  
  // Step 1: Create nodes for all tables
  for (const table of tables) {
    const fullTableName = `${table.schema}.${table.tableName}`;
    
    nodes.set(fullTableName, {
      tableName: table.tableName,
      schema: table.schema,
      foreignKeys: table.foreignKeys,
      inDegree: 0 // Will be calculated in step 2
    });
    
    // Initialize edges array
    edges.set(fullTableName, []);
  }
  
  // Step 2: Build edges based on foreign key relationships
  for (const table of tables) {
    const fullTableName = `${table.schema}.${table.tableName}`;
    const dependencies: string[] = [];
    
    for (const fk of table.foreignKeys) {
      // Find the referenced table
      const referencedTable = findReferencedTable(tables, fk.referencedTable);
      
      if (referencedTable) {
        const referencedFullName = `${referencedTable.schema}.${referencedTable.tableName}`;
        
        // Skip self-referencing foreign keys (e.g., categories.parent_id)
        if (referencedFullName !== fullTableName) {
          dependencies.push(referencedFullName);
        }
      } else {
        console.warn(`Warning: Referenced table "${fk.referencedTable}" not found for FK in ${fullTableName}.${fk.columnName}`);
      }
    }
    
    // Remove duplicates (a table might reference another table multiple times)
    const uniqueDependencies = [...new Set(dependencies)];
    edges.set(fullTableName, uniqueDependencies);
    
    // Set in-degree to the number of dependencies this table has
    const node = nodes.get(fullTableName)!;
    node.inDegree = uniqueDependencies.length;
  }
  
  return { nodes, edges };
}

/**
 * Finds a table by name across all schemas
 * Handles both simple table names and schema-qualified names
 */
function findReferencedTable(tables: TableMetadata[], referencedTableName: string): TableMetadata | undefined {
  // Try exact match first (case-insensitive)
  const exactMatch = tables.find(t => 
    t.tableName.toLowerCase() === referencedTableName.toLowerCase()
  );
  
  if (exactMatch) {
    return exactMatch;
  }
  
  // Try schema-qualified match (e.g., "springfood_authentication.user")
  if (referencedTableName.includes('.')) {
    const [schema, tableName] = referencedTableName.split('.');
    return tables.find(t => 
      t.schema.toLowerCase() === schema.toLowerCase() && 
      t.tableName.toLowerCase() === tableName.toLowerCase()
    );
  }
  
  return undefined;
}

/**
 * Performs topological sort on the dependency graph to determine insertion order
 * Uses Kahn's algorithm for topological sorting
 * 
 * @param graph - Dependency graph from analyzeDependencies
 * @returns Array of table names in dependency order (tables with no dependencies first)
 * 
 * **Validates: Requirements 1.1, 1.2, 1.3, 1.4, 7.2**
 */
export function topologicalSort(graph: DependencyGraph): string[] {
  const result: string[] = [];
  const inDegrees = new Map<string, number>();
  const queue: string[] = [];
  
  // Initialize in-degrees
  for (const [tableName, node] of graph.nodes) {
    inDegrees.set(tableName, node.inDegree);
    
    // Add tables with no dependencies to the queue
    if (node.inDegree === 0) {
      queue.push(tableName);
    }
  }
  
  // Process queue
  while (queue.length > 0) {
    // Sort queue to ensure deterministic order (alphabetically)
    queue.sort();
    
    const current = queue.shift()!;
    result.push(current);
    
    // For each table that the current table depends on, we've now "processed" that dependency
    // So we need to decrease the in-degree of tables that depend on the current table
    // In other words, find all tables where current is in their dependencies list
    for (const [tableName, dependencies] of graph.edges) {
      if (dependencies.includes(current)) {
        // This table depends on current, so decrease its in-degree
        const currentInDegree = inDegrees.get(tableName)!;
        inDegrees.set(tableName, currentInDegree - 1);
        
        // If in-degree becomes 0, add to queue
        if (currentInDegree - 1 === 0) {
          queue.push(tableName);
        }
      }
    }
  }
  
  // Check if all nodes were processed (no cycles)
  if (result.length !== graph.nodes.size) {
    throw new Error(
      `Topological sort failed: Cycle detected in dependency graph. ` +
      `Processed ${result.length} tables out of ${graph.nodes.size}.`
    );
  }
  
  return result;
}

/**
 * Detects cycles in the dependency graph using depth-first search
 * 
 * @param graph - Dependency graph from analyzeDependencies
 * @returns Array of cycles, where each cycle is an array of table names forming a circular dependency
 * 
 * **Validates: Requirements 1.1, 7.2**
 */
export function detectCycles(graph: DependencyGraph): string[][] {
  const cycles: string[][] = [];
  const visited = new Set<string>();
  const recursionStack = new Set<string>();
  const currentPath: string[] = [];
  
  /**
   * DFS helper function to detect cycles
   */
  function dfs(tableName: string): boolean {
    visited.add(tableName);
    recursionStack.add(tableName);
    currentPath.push(tableName);
    
    const dependencies = graph.edges.get(tableName) || [];
    
    for (const dependency of dependencies) {
      if (!visited.has(dependency)) {
        // Visit unvisited node
        if (dfs(dependency)) {
          return true; // Cycle found
        }
      } else if (recursionStack.has(dependency)) {
        // Back edge found - cycle detected
        const cycleStartIndex = currentPath.indexOf(dependency);
        const cycle = currentPath.slice(cycleStartIndex);
        cycle.push(dependency); // Complete the cycle
        cycles.push(cycle);
        return true;
      }
    }
    
    // Backtrack
    recursionStack.delete(tableName);
    currentPath.pop();
    return false;
  }
  
  // Run DFS from each unvisited node
  for (const tableName of graph.nodes.keys()) {
    if (!visited.has(tableName)) {
      dfs(tableName);
    }
  }
  
  return cycles;
}

/**
 * Analyzes the dependency graph and outputs a hierarchical level structure
 * 
 * Level 1: Tables with no dependencies
 * Level 2: Tables that only depend on Level 1 tables
 * Level 3: Tables that depend on Level 1 or Level 2 tables
 * ... and so on
 * 
 * @param graph - Dependency graph from analyzeDependencies
 * @returns Map of level number to array of table names at that level
 * 
 * **Validates: Requirements 7.2, 7.3, 7.4, 7.5, 7.6**
 */
export function analyzeDependencyLevels(graph: DependencyGraph): Map<number, string[]> {
  const levels = new Map<number, string[]>();
  const tableLevels = new Map<string, number>();
  
  // Get topologically sorted tables
  const sortedTables = topologicalSort(graph);
  
  // Assign levels
  for (const tableName of sortedTables) {
    const dependencies = graph.edges.get(tableName) || [];
    
    if (dependencies.length === 0) {
      // No dependencies - Level 1
      tableLevels.set(tableName, 1);
    } else {
      // Find the maximum level of all dependencies
      let maxDependencyLevel = 0;
      for (const dependency of dependencies) {
        const depLevel = tableLevels.get(dependency) || 0;
        maxDependencyLevel = Math.max(maxDependencyLevel, depLevel);
      }
      
      // This table is one level higher than its highest dependency
      tableLevels.set(tableName, maxDependencyLevel + 1);
    }
  }
  
  // Group tables by level
  for (const [tableName, level] of tableLevels) {
    if (!levels.has(level)) {
      levels.set(level, []);
    }
    levels.get(level)!.push(tableName);
  }
  
  // Sort tables within each level alphabetically
  for (const [level, tables] of levels) {
    tables.sort();
  }
  
  return levels;
}

/**
 * Prints the dependency hierarchy in a human-readable format
 * 
 * @param graph - Dependency graph from analyzeDependencies
 */
export function printDependencyHierarchy(graph: DependencyGraph): void {
  const levels = analyzeDependencyLevels(graph);
  
  console.log('\n=== Dependency Hierarchy ===\n');
  
  for (const [level, tables] of Array.from(levels.entries()).sort((a, b) => a[0] - b[0])) {
    console.log(`Level ${level} (${tables.length} tables):`);
    for (const table of tables) {
      const dependencies = graph.edges.get(table) || [];
      if (dependencies.length > 0) {
        console.log(`  - ${table} → depends on: [${dependencies.join(', ')}]`);
      } else {
        console.log(`  - ${table} (no dependencies)`);
      }
    }
    console.log();
  }
  
  console.log(`Total levels: ${levels.size}`);
  console.log(`Total tables: ${graph.nodes.size}`);
}

/**
 * Validates that the dependency graph matches the expected 7-level hierarchy
 * as documented in the design
 * 
 * @param graph - Dependency graph from analyzeDependencies
 * @returns true if the hierarchy matches expectations, false otherwise
 */
export function validateExpectedHierarchy(graph: DependencyGraph): boolean {
  const levels = analyzeDependencyLevels(graph);
  
  // Expected hierarchy from design document
  const expectedLevels = {
    1: ['springfood_authentication.role', 'springfood_product.category_group'],
    2: ['springfood_authentication.user', 'springfood_product.categories'],
    3: ['springfood_authentication.user_has_role', 'springfood_authentication.address', 'springfood_shop.shops'],
    4: ['springfood_product.products', 'springfood_shop.shop_members', 'springfood_shop.shop_wallets'],
    5: [
      'springfood_product.product_categories',
      'springfood_product.product_images',
      'springfood_product.feedbacks',
      'springfood_product.sales',
      'springfood_order.orders'
    ],
    6: [
      'springfood_product.product_sales',
      'springfood_order.order_items',
      'springfood_payment.payment_transactions',
      'springfood_chat.conversation',
      'springfood_notification.notifications'
    ],
    7: [
      'springfood_chat.conversation_participant',
      'springfood_chat.message',
      'springfood_media.media_file'
    ]
  };
  
  let isValid = true;
  
  console.log('\n=== Validating Expected Hierarchy ===\n');
  
  for (const [expectedLevel, expectedTables] of Object.entries(expectedLevels)) {
    const level = parseInt(expectedLevel);
    const actualTables = levels.get(level) || [];
    
    console.log(`Level ${level}:`);
    console.log(`  Expected: ${expectedTables.length} tables`);
    console.log(`  Actual: ${actualTables.length} tables`);
    
    // Check for missing tables
    const missingTables = expectedTables.filter(t => !actualTables.includes(t));
    if (missingTables.length > 0) {
      console.log(`  ❌ Missing tables: ${missingTables.join(', ')}`);
      isValid = false;
    }
    
    // Check for unexpected tables
    const unexpectedTables = actualTables.filter(t => !expectedTables.includes(t));
    if (unexpectedTables.length > 0) {
      console.log(`  ⚠️  Unexpected tables: ${unexpectedTables.join(', ')}`);
      // Don't mark as invalid - schema might have evolved
    }
    
    if (missingTables.length === 0 && unexpectedTables.length === 0) {
      console.log(`  ✅ Level ${level} matches expected hierarchy`);
    }
    
    console.log();
  }
  
  return isValid;
}
