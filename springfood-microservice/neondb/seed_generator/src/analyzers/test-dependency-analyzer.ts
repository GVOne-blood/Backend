/**
 * Test script for dependency analyzer
 * 
 * This script tests the dependency analyzer with the actual DDL files
 * from the neondb directory.
 */

import * as path from 'path';
import { parseAllDDLFiles } from '../parsers/ddl-parser';
import {
  analyzeDependencies,
  topologicalSort,
  detectCycles,
  analyzeDependencyLevels,
  printDependencyHierarchy,
  validateExpectedHierarchy
} from './dependency-analyzer';

function main() {
  console.log('=== Testing Dependency Analyzer ===\n');
  
  // Path to DDL files
  const neondbPath = path.join(__dirname, '../../../');
  
  console.log(`Parsing DDL files from: ${neondbPath}\n`);
  
  try {
    // Step 1: Parse all DDL files
    console.log('Step 1: Parsing DDL files...');
    const tables = parseAllDDLFiles(neondbPath);
    console.log(`✅ Parsed ${tables.length} tables\n`);
    
    // List all tables
    console.log('Tables found:');
    for (const table of tables) {
      const fkCount = table.foreignKeys.length;
      console.log(`  - ${table.schema}.${table.tableName} (${fkCount} foreign keys)`);
    }
    console.log();
    
    // Step 2: Analyze dependencies
    console.log('Step 2: Analyzing dependencies...');
    const graph = analyzeDependencies(tables);
    console.log(`✅ Built dependency graph with ${graph.nodes.size} nodes\n`);
    
    // Step 3: Check for unresolved foreign keys
    console.log('Step 3: Checking for unresolved foreign keys...');
    let unresolvedCount = 0;
    for (const [tableName, node] of graph.nodes) {
      const dependencies = graph.edges.get(tableName) || [];
      for (const dep of dependencies) {
        if (!graph.nodes.has(dep)) {
          console.log(`  ⚠️  ${tableName} references ${dep} (not found)`);
          unresolvedCount++;
        }
      }
    }
    if (unresolvedCount === 0) {
      console.log('✅ All foreign keys resolved\n');
    } else {
      console.log(`⚠️  Found ${unresolvedCount} unresolved foreign key references\n`);
    }
    
    // Step 4: Detect cycles
    console.log('Step 4: Detecting cycles...');
    const cycles = detectCycles(graph);
    if (cycles.length === 0) {
      console.log('✅ No cycles detected (good!)\n');
    } else {
      console.log(`❌ Found ${cycles.length} cycle(s):`);
      for (let i = 0; i < cycles.length; i++) {
        console.log(`  Cycle ${i + 1}: ${cycles[i].join(' → ')}`);
      }
      console.log();
    }
    
    // Step 5: Topological sort
    console.log('Step 5: Performing topological sort...');
    const sortedTables = topologicalSort(graph);
    console.log(`✅ Sorted ${sortedTables.length} tables in dependency order\n`);
    
    console.log('Insertion order:');
    for (let i = 0; i < sortedTables.length; i++) {
      console.log(`  ${i + 1}. ${sortedTables[i]}`);
    }
    console.log();
    
    // Step 6: Analyze dependency levels
    console.log('Step 6: Analyzing dependency levels...');
    const levels = analyzeDependencyLevels(graph);
    console.log(`✅ Identified ${levels.size} dependency levels\n`);
    
    // Step 7: Print hierarchy
    printDependencyHierarchy(graph);
    
    // Step 8: Validate expected hierarchy
    const isValid = validateExpectedHierarchy(graph);
    
    if (isValid) {
      console.log('✅ Dependency hierarchy matches expected structure!');
    } else {
      console.log('⚠️  Dependency hierarchy differs from expected structure.');
      console.log('This may be due to schema changes or missing DDL files.');
    }
    
    console.log('\n=== Test Complete ===');
    
  } catch (error) {
    console.error('❌ Error during testing:', error);
    process.exit(1);
  }
}

// Run the test
main();
