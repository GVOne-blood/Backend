#!/bin/bash

# Test SQL execution script
# This script tests if the generated SQL files can be executed successfully

echo "Testing SQL file execution..."
echo ""

# Load environment variables
if [ -f ../../.env ]; then
    export $(cat ../../.env | grep -v '^#' | xargs)
fi

# Check if DATABASE_URL is set
if [ -z "$DATABASE_URL" ]; then
    echo "ERROR: DATABASE_URL is not set"
    echo "Please set DATABASE_URL in .env file"
    exit 1
fi

echo "Database URL: ${DATABASE_URL:0:30}..."
echo ""

# Test 03_springfood_product_seed_data.sql
echo "Testing 03_springfood_product_seed_data.sql..."
psql "$DATABASE_URL" -f ../seed_data/03_springfood_product_seed_data.sql

if [ $? -eq 0 ]; then
    echo "✓ SQL file executed successfully!"
else
    echo "✗ SQL file execution failed!"
    exit 1
fi

echo ""
echo "All tests passed!"
