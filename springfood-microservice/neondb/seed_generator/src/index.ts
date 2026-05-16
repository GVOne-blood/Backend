/**
 * SpringFood Seed Data Generator
 * 
 * This is the main entry point for the seed data generation system.
 * It orchestrates the entire process:
 * 1. Parse DDL files from neondb/ folder
 * 2. Analyze dependencies and determine insertion order
 * 3. Generate realistic Vietnamese food e-commerce data
 * 4. Write SQL INSERT files for each schema
 * 5. Create master execution script
 */

import { generateAllSQLFiles } from './generate-sql-files';

console.log('SpringFood Seed Data Generator');
console.log('==============================');
console.log('');

// Run the generator
generateAllSQLFiles()
  .then(() => {
    console.log('✓ All SQL files generated successfully!');
    process.exit(0);
  })
  .catch((error) => {
    console.error('✗ Error generating SQL files:', error);
    process.exit(1);
  });
