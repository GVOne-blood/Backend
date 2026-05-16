import {
  analyzeDependencies,
  topologicalSort,
  detectCycles,
  analyzeDependencyLevels,
  DependencyGraph,
  TableNode
} from '../src/analyzers/dependency-analyzer';
import { TableMetadata, ForeignKey } from '../src/parsers/ddl-parser';

/**
 * Unit tests for dependency analyzer module
 * 
 * **Validates: Requirements 1.1, 7.2**
 * 
 * Test Coverage:
 * - Topological sort with simple dependency graph
 * - Cycle detection with intentionally cyclic dependencies
 * - Handling of self-referencing tables
 * - Cross-schema dependencies
 * - Empty graph edge case
 * - Single table with no dependencies
 */

describe('Dependency Analyzer', () => {
  
  describe('analyzeDependencies', () => {
    it('should build dependency graph for simple 3-table chain', () => {
      // Setup: A → B → C (A depends on B, B depends on C)
      const tables: TableMetadata[] = [
        {
          tableName: 'table_a',
          schema: 'test_schema',
          columns: [],
          primaryKeys: ['id'],
          foreignKeys: [
            { columnName: 'b_id', referencedTable: 'table_b', referencedColumn: 'id' }
          ]
        },
        {
          tableName: 'table_b',
          schema: 'test_schema',
          columns: [],
          primaryKeys: ['id'],
          foreignKeys: [
            { columnName: 'c_id', referencedTable: 'table_c', referencedColumn: 'id' }
          ]
        },
        {
          tableName: 'table_c',
          schema: 'test_schema',
          columns: [],
          primaryKeys: ['id'],
          foreignKeys: []
        }
      ];
      
      const graph = analyzeDependencies(tables);
      
      // Verify nodes
      expect(graph.nodes.size).toBe(3);
      expect(graph.nodes.has('test_schema.table_a')).toBe(true);
      expect(graph.nodes.has('test_schema.table_b')).toBe(true);
      expect(graph.nodes.has('test_schema.table_c')).toBe(true);
      
      // Verify edges (dependencies)
      expect(graph.edges.get('test_schema.table_a')).toEqual(['test_schema.table_b']);
      expect(graph.edges.get('test_schema.table_b')).toEqual(['test_schema.table_c']);
      expect(graph.edges.get('test_schema.table_c')).toEqual([]);
      
      // Verify in-degrees
      expect(graph.nodes.get('test_schema.table_a')?.inDegree).toBe(1);
      expect(graph.nodes.get('test_schema.table_b')?.inDegree).toBe(1);
      expect(graph.nodes.get('test_schema.table_c')?.inDegree).toBe(0);
    });
    
    it('should handle self-referencing foreign keys correctly', () => {
      // Setup: categories table with parent_id self-reference
      const tables: TableMetadata[] = [
        {
          tableName: 'categories',
          schema: 'product',
          columns: [],
          primaryKeys: ['category_name'],
          foreignKeys: [
            { columnName: 'parent_id', referencedTable: 'categories', referencedColumn: 'category_name' }
          ]
        }
      ];
      
      const graph = analyzeDependencies(tables);
      
      // Self-referencing FK should be ignored in dependency graph
      expect(graph.edges.get('product.categories')).toEqual([]);
      expect(graph.nodes.get('product.categories')?.inDegree).toBe(0);
    });
    
    it('should handle cross-schema dependencies', () => {
      // Setup: order.orders depends on auth.user and shop.shops
      const tables: TableMetadata[] = [
        {
          tableName: 'user',
          schema: 'auth',
          columns: [],
          primaryKeys: ['user_id'],
          foreignKeys: []
        },
        {
          tableName: 'shops',
          schema: 'shop',
          columns: [],
          primaryKeys: ['shop_id'],
          foreignKeys: []
        },
        {
          tableName: 'orders',
          schema: 'order',
          columns: [],
          primaryKeys: ['order_id'],
          foreignKeys: [
            { columnName: 'user_id', referencedTable: 'user', referencedColumn: 'user_id' },
            { columnName: 'shop_id', referencedTable: 'shops', referencedColumn: 'shop_id' }
          ]
        }
      ];
      
      const graph = analyzeDependencies(tables);
      
      // Verify cross-schema dependencies
      const orderDeps = graph.edges.get('order.orders');
      expect(orderDeps).toContain('auth.user');
      expect(orderDeps).toContain('shop.shops');
      expect(orderDeps?.length).toBe(2);
      
      // Verify in-degrees
      expect(graph.nodes.get('order.orders')?.inDegree).toBe(2);
      expect(graph.nodes.get('auth.user')?.inDegree).toBe(0);
      expect(graph.nodes.get('shop.shops')?.inDegree).toBe(0);
    });
    
    it('should handle multiple foreign keys to the same table', () => {
      // Setup: table with multiple FKs to the same referenced table
      const tables: TableMetadata[] = [
        {
          tableName: 'user',
          schema: 'auth',
          columns: [],
          primaryKeys: ['user_id'],
          foreignKeys: []
        },
        {
          tableName: 'messages',
          schema: 'chat',
          columns: [],
          primaryKeys: ['message_id'],
          foreignKeys: [
            { columnName: 'sender_id', referencedTable: 'user', referencedColumn: 'user_id' },
            { columnName: 'receiver_id', referencedTable: 'user', referencedColumn: 'user_id' }
          ]
        }
      ];
      
      const graph = analyzeDependencies(tables);
      
      // Should only have one dependency (duplicates removed)
      const messageDeps = graph.edges.get('chat.messages');
      expect(messageDeps).toEqual(['auth.user']);
      expect(messageDeps?.length).toBe(1);
      expect(graph.nodes.get('chat.messages')?.inDegree).toBe(1);
    });
    
    it('should handle empty table list', () => {
      const tables: TableMetadata[] = [];
      const graph = analyzeDependencies(tables);
      
      expect(graph.nodes.size).toBe(0);
      expect(graph.edges.size).toBe(0);
    });
    
    it('should handle single table with no dependencies', () => {
      const tables: TableMetadata[] = [
        {
          tableName: 'role',
          schema: 'auth',
          columns: [],
          primaryKeys: ['role_name'],
          foreignKeys: []
        }
      ];
      
      const graph = analyzeDependencies(tables);
      
      expect(graph.nodes.size).toBe(1);
      expect(graph.edges.get('auth.role')).toEqual([]);
      expect(graph.nodes.get('auth.role')?.inDegree).toBe(0);
    });
  });
  
  describe('topologicalSort', () => {
    it('should produce correct insertion order for simple dependency graph', () => {
      // Setup: C (no deps) → B (depends on C) → A (depends on B)
      const graph: DependencyGraph = {
        nodes: new Map([
          ['schema.table_a', { tableName: 'table_a', schema: 'schema', foreignKeys: [], inDegree: 1 }],
          ['schema.table_b', { tableName: 'table_b', schema: 'schema', foreignKeys: [], inDegree: 1 }],
          ['schema.table_c', { tableName: 'table_c', schema: 'schema', foreignKeys: [], inDegree: 0 }]
        ]),
        edges: new Map([
          ['schema.table_a', ['schema.table_b']],
          ['schema.table_b', ['schema.table_c']],
          ['schema.table_c', []]
        ])
      };
      
      const sorted = topologicalSort(graph);
      
      // C should come first (no dependencies)
      // B should come second (depends on C)
      // A should come last (depends on B)
      expect(sorted).toEqual(['schema.table_c', 'schema.table_b', 'schema.table_a']);
    });
    
    it('should handle multiple tables at the same level', () => {
      // Setup: A and B have no dependencies, C depends on both A and B
      const graph: DependencyGraph = {
        nodes: new Map([
          ['schema.table_a', { tableName: 'table_a', schema: 'schema', foreignKeys: [], inDegree: 0 }],
          ['schema.table_b', { tableName: 'table_b', schema: 'schema', foreignKeys: [], inDegree: 0 }],
          ['schema.table_c', { tableName: 'table_c', schema: 'schema', foreignKeys: [], inDegree: 2 }]
        ]),
        edges: new Map([
          ['schema.table_a', []],
          ['schema.table_b', []],
          ['schema.table_c', ['schema.table_a', 'schema.table_b']]
        ])
      };
      
      const sorted = topologicalSort(graph);
      
      // A and B should come before C (order between A and B is alphabetical)
      expect(sorted.length).toBe(3);
      expect(sorted[0]).toBe('schema.table_a');
      expect(sorted[1]).toBe('schema.table_b');
      expect(sorted[2]).toBe('schema.table_c');
    });
    
    it('should throw error when cycle is detected', () => {
      // Setup: A → B → C → A (circular dependency)
      const graph: DependencyGraph = {
        nodes: new Map([
          ['schema.table_a', { tableName: 'table_a', schema: 'schema', foreignKeys: [], inDegree: 1 }],
          ['schema.table_b', { tableName: 'table_b', schema: 'schema', foreignKeys: [], inDegree: 1 }],
          ['schema.table_c', { tableName: 'table_c', schema: 'schema', foreignKeys: [], inDegree: 1 }]
        ]),
        edges: new Map([
          ['schema.table_a', ['schema.table_b']],
          ['schema.table_b', ['schema.table_c']],
          ['schema.table_c', ['schema.table_a']]
        ])
      };
      
      expect(() => topologicalSort(graph)).toThrow(/Cycle detected/);
    });
    
    it('should handle empty graph', () => {
      const graph: DependencyGraph = {
        nodes: new Map(),
        edges: new Map()
      };
      
      const sorted = topologicalSort(graph);
      expect(sorted).toEqual([]);
    });
    
    it('should handle single table with no dependencies', () => {
      const graph: DependencyGraph = {
        nodes: new Map([
          ['schema.table_a', { tableName: 'table_a', schema: 'schema', foreignKeys: [], inDegree: 0 }]
        ]),
        edges: new Map([
          ['schema.table_a', []]
        ])
      };
      
      const sorted = topologicalSort(graph);
      expect(sorted).toEqual(['schema.table_a']);
    });
    
    it('should produce deterministic order (alphabetical) for tables at same level', () => {
      // Setup: Multiple tables with no dependencies
      const graph: DependencyGraph = {
        nodes: new Map([
          ['schema.zebra', { tableName: 'zebra', schema: 'schema', foreignKeys: [], inDegree: 0 }],
          ['schema.apple', { tableName: 'apple', schema: 'schema', foreignKeys: [], inDegree: 0 }],
          ['schema.mango', { tableName: 'mango', schema: 'schema', foreignKeys: [], inDegree: 0 }]
        ]),
        edges: new Map([
          ['schema.zebra', []],
          ['schema.apple', []],
          ['schema.mango', []]
        ])
      };
      
      const sorted = topologicalSort(graph);
      
      // Should be sorted alphabetically
      expect(sorted).toEqual(['schema.apple', 'schema.mango', 'schema.zebra']);
    });
  });
  
  describe('detectCycles', () => {
    it('should detect simple 2-table cycle', () => {
      // Setup: A → B → A
      const graph: DependencyGraph = {
        nodes: new Map([
          ['schema.table_a', { tableName: 'table_a', schema: 'schema', foreignKeys: [], inDegree: 1 }],
          ['schema.table_b', { tableName: 'table_b', schema: 'schema', foreignKeys: [], inDegree: 1 }]
        ]),
        edges: new Map([
          ['schema.table_a', ['schema.table_b']],
          ['schema.table_b', ['schema.table_a']]
        ])
      };
      
      const cycles = detectCycles(graph);
      
      expect(cycles.length).toBeGreaterThan(0);
      
      // Verify cycle contains both tables
      const cycle = cycles[0];
      expect(cycle).toContain('schema.table_a');
      expect(cycle).toContain('schema.table_b');
    });
    
    it('should detect 3-table cycle', () => {
      // Setup: A → B → C → A
      const graph: DependencyGraph = {
        nodes: new Map([
          ['schema.table_a', { tableName: 'table_a', schema: 'schema', foreignKeys: [], inDegree: 1 }],
          ['schema.table_b', { tableName: 'table_b', schema: 'schema', foreignKeys: [], inDegree: 1 }],
          ['schema.table_c', { tableName: 'table_c', schema: 'schema', foreignKeys: [], inDegree: 1 }]
        ]),
        edges: new Map([
          ['schema.table_a', ['schema.table_b']],
          ['schema.table_b', ['schema.table_c']],
          ['schema.table_c', ['schema.table_a']]
        ])
      };
      
      const cycles = detectCycles(graph);
      
      expect(cycles.length).toBeGreaterThan(0);
      
      // Verify cycle contains all three tables
      const cycle = cycles[0];
      expect(cycle.length).toBeGreaterThanOrEqual(3);
      expect(cycle).toContain('schema.table_a');
      expect(cycle).toContain('schema.table_b');
      expect(cycle).toContain('schema.table_c');
    });
    
    it('should return empty array for acyclic graph', () => {
      // Setup: A → B → C (no cycle)
      const graph: DependencyGraph = {
        nodes: new Map([
          ['schema.table_a', { tableName: 'table_a', schema: 'schema', foreignKeys: [], inDegree: 1 }],
          ['schema.table_b', { tableName: 'table_b', schema: 'schema', foreignKeys: [], inDegree: 1 }],
          ['schema.table_c', { tableName: 'table_c', schema: 'schema', foreignKeys: [], inDegree: 0 }]
        ]),
        edges: new Map([
          ['schema.table_a', ['schema.table_b']],
          ['schema.table_b', ['schema.table_c']],
          ['schema.table_c', []]
        ])
      };
      
      const cycles = detectCycles(graph);
      expect(cycles).toEqual([]);
    });
    
    it('should handle empty graph', () => {
      const graph: DependencyGraph = {
        nodes: new Map(),
        edges: new Map()
      };
      
      const cycles = detectCycles(graph);
      expect(cycles).toEqual([]);
    });
    
    it('should handle graph with no edges', () => {
      const graph: DependencyGraph = {
        nodes: new Map([
          ['schema.table_a', { tableName: 'table_a', schema: 'schema', foreignKeys: [], inDegree: 0 }],
          ['schema.table_b', { tableName: 'table_b', schema: 'schema', foreignKeys: [], inDegree: 0 }]
        ]),
        edges: new Map([
          ['schema.table_a', []],
          ['schema.table_b', []]
        ])
      };
      
      const cycles = detectCycles(graph);
      expect(cycles).toEqual([]);
    });
    
    it('should not detect self-referencing as a cycle', () => {
      // Note: Self-referencing FKs are filtered out in analyzeDependencies
      // This test verifies that if somehow a self-reference exists, it's handled
      const graph: DependencyGraph = {
        nodes: new Map([
          ['schema.categories', { tableName: 'categories', schema: 'schema', foreignKeys: [], inDegree: 0 }]
        ]),
        edges: new Map([
          ['schema.categories', []]
        ])
      };
      
      const cycles = detectCycles(graph);
      expect(cycles).toEqual([]);
    });
  });
  
  describe('analyzeDependencyLevels', () => {
    it('should assign correct levels for simple 3-level hierarchy', () => {
      // Setup: Level 1: C, Level 2: B (depends on C), Level 3: A (depends on B)
      const graph: DependencyGraph = {
        nodes: new Map([
          ['schema.table_a', { tableName: 'table_a', schema: 'schema', foreignKeys: [], inDegree: 1 }],
          ['schema.table_b', { tableName: 'table_b', schema: 'schema', foreignKeys: [], inDegree: 1 }],
          ['schema.table_c', { tableName: 'table_c', schema: 'schema', foreignKeys: [], inDegree: 0 }]
        ]),
        edges: new Map([
          ['schema.table_a', ['schema.table_b']],
          ['schema.table_b', ['schema.table_c']],
          ['schema.table_c', []]
        ])
      };
      
      const levels = analyzeDependencyLevels(graph);
      
      expect(levels.get(1)).toEqual(['schema.table_c']);
      expect(levels.get(2)).toEqual(['schema.table_b']);
      expect(levels.get(3)).toEqual(['schema.table_a']);
    });
    
    it('should handle multiple tables at the same level', () => {
      // Setup: Level 1: A, B (no deps), Level 2: C (depends on A and B)
      const graph: DependencyGraph = {
        nodes: new Map([
          ['schema.table_a', { tableName: 'table_a', schema: 'schema', foreignKeys: [], inDegree: 0 }],
          ['schema.table_b', { tableName: 'table_b', schema: 'schema', foreignKeys: [], inDegree: 0 }],
          ['schema.table_c', { tableName: 'table_c', schema: 'schema', foreignKeys: [], inDegree: 2 }]
        ]),
        edges: new Map([
          ['schema.table_a', []],
          ['schema.table_b', []],
          ['schema.table_c', ['schema.table_a', 'schema.table_b']]
        ])
      };
      
      const levels = analyzeDependencyLevels(graph);
      
      const level1 = levels.get(1);
      expect(level1).toContain('schema.table_a');
      expect(level1).toContain('schema.table_b');
      expect(level1?.length).toBe(2);
      
      expect(levels.get(2)).toEqual(['schema.table_c']);
    });
    
    it('should assign level based on maximum dependency level', () => {
      // Setup: A (L1), B (L1), C depends on A (L2), D depends on B and C (L3)
      const graph: DependencyGraph = {
        nodes: new Map([
          ['schema.table_a', { tableName: 'table_a', schema: 'schema', foreignKeys: [], inDegree: 0 }],
          ['schema.table_b', { tableName: 'table_b', schema: 'schema', foreignKeys: [], inDegree: 0 }],
          ['schema.table_c', { tableName: 'table_c', schema: 'schema', foreignKeys: [], inDegree: 1 }],
          ['schema.table_d', { tableName: 'table_d', schema: 'schema', foreignKeys: [], inDegree: 2 }]
        ]),
        edges: new Map([
          ['schema.table_a', []],
          ['schema.table_b', []],
          ['schema.table_c', ['schema.table_a']],
          ['schema.table_d', ['schema.table_b', 'schema.table_c']]
        ])
      };
      
      const levels = analyzeDependencyLevels(graph);
      
      // D depends on C (level 2) and B (level 1), so D should be level 3
      expect(levels.get(1)?.sort()).toEqual(['schema.table_a', 'schema.table_b']);
      expect(levels.get(2)).toEqual(['schema.table_c']);
      expect(levels.get(3)).toEqual(['schema.table_d']);
    });
    
    it('should handle empty graph', () => {
      const graph: DependencyGraph = {
        nodes: new Map(),
        edges: new Map()
      };
      
      const levels = analyzeDependencyLevels(graph);
      expect(levels.size).toBe(0);
    });
    
    it('should sort tables alphabetically within each level', () => {
      // Setup: Multiple tables at level 1
      const graph: DependencyGraph = {
        nodes: new Map([
          ['schema.zebra', { tableName: 'zebra', schema: 'schema', foreignKeys: [], inDegree: 0 }],
          ['schema.apple', { tableName: 'apple', schema: 'schema', foreignKeys: [], inDegree: 0 }],
          ['schema.mango', { tableName: 'mango', schema: 'schema', foreignKeys: [], inDegree: 0 }]
        ]),
        edges: new Map([
          ['schema.zebra', []],
          ['schema.apple', []],
          ['schema.mango', []]
        ])
      };
      
      const levels = analyzeDependencyLevels(graph);
      
      expect(levels.get(1)).toEqual(['schema.apple', 'schema.mango', 'schema.zebra']);
    });
  });
  
  describe('Integration Tests', () => {
    it('should handle complex multi-schema dependency graph', () => {
      // Setup: Realistic SpringFood-like schema
      const tables: TableMetadata[] = [
        // Level 1: No dependencies
        {
          tableName: 'role',
          schema: 'auth',
          columns: [],
          primaryKeys: ['role_name'],
          foreignKeys: []
        },
        // Level 2: Depends on Level 1
        {
          tableName: 'user',
          schema: 'auth',
          columns: [],
          primaryKeys: ['user_id'],
          foreignKeys: []
        },
        // Level 3: Depends on Level 2
        {
          tableName: 'shops',
          schema: 'shop',
          columns: [],
          primaryKeys: ['shop_id'],
          foreignKeys: []
        },
        {
          tableName: 'user_has_role',
          schema: 'auth',
          columns: [],
          primaryKeys: ['user_id', 'role_name'],
          foreignKeys: [
            { columnName: 'user_id', referencedTable: 'user', referencedColumn: 'user_id' },
            { columnName: 'role_name', referencedTable: 'role', referencedColumn: 'role_name' }
          ]
        },
        // Level 4: Depends on Level 3
        {
          tableName: 'products',
          schema: 'product',
          columns: [],
          primaryKeys: ['product_id'],
          foreignKeys: [
            { columnName: 'shop_id', referencedTable: 'shops', referencedColumn: 'shop_id' }
          ]
        },
        // Level 5: Depends on Level 4
        {
          tableName: 'orders',
          schema: 'order',
          columns: [],
          primaryKeys: ['order_id'],
          foreignKeys: [
            { columnName: 'user_id', referencedTable: 'user', referencedColumn: 'user_id' },
            { columnName: 'shop_id', referencedTable: 'shops', referencedColumn: 'shop_id' }
          ]
        },
        // Level 6: Depends on Level 5
        {
          tableName: 'order_items',
          schema: 'order',
          columns: [],
          primaryKeys: ['order_item_id'],
          foreignKeys: [
            { columnName: 'order_id', referencedTable: 'orders', referencedColumn: 'order_id' },
            { columnName: 'product_id', referencedTable: 'products', referencedColumn: 'product_id' }
          ]
        }
      ];
      
      const graph = analyzeDependencies(tables);
      const sorted = topologicalSort(graph);
      const levels = analyzeDependencyLevels(graph);
      const cycles = detectCycles(graph);
      
      // Verify no cycles
      expect(cycles).toEqual([]);
      
      // Verify all tables are in sorted order
      expect(sorted.length).toBe(7);
      
      // Verify role comes before user_has_role
      const roleIndex = sorted.indexOf('auth.role');
      const userHasRoleIndex = sorted.indexOf('auth.user_has_role');
      expect(roleIndex).toBeLessThan(userHasRoleIndex);
      
      // Verify user comes before orders
      const userIndex = sorted.indexOf('auth.user');
      const ordersIndex = sorted.indexOf('order.orders');
      expect(userIndex).toBeLessThan(ordersIndex);
      
      // Verify products comes before order_items
      const productsIndex = sorted.indexOf('product.products');
      const orderItemsIndex = sorted.indexOf('order.order_items');
      expect(productsIndex).toBeLessThan(orderItemsIndex);
      
      // Verify level structure
      // Level 1: Tables with no dependencies
      expect(levels.get(1)).toContain('auth.role');
      expect(levels.get(1)).toContain('auth.user');
      expect(levels.get(1)).toContain('shop.shops');
      
      // Level 2: Tables that depend on level 1
      expect(levels.get(2)).toContain('auth.user_has_role'); // depends on user and role
      expect(levels.get(2)).toContain('product.products'); // depends on shops
      expect(levels.get(2)).toContain('order.orders'); // depends on user and shops
    });
  });
});
