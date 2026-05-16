#!/bin/bash
# ============================================================================
# SpringFood Database Seed Data - Linux/Mac Execution Script
# ============================================================================
# This script executes all seed data SQL files on NeonDB using psql client
#
# Prerequisites:
#   - PostgreSQL client (psql) installed
#   - NeonDB connection credentials
#
# Usage:
#   chmod +x run_seeds.sh
#   ./run_seeds.sh
#
# Environment Variables (optional):
#   NEON_HOST     - NeonDB host (default: prompt)
#   NEON_USER     - NeonDB username (default: prompt)
#   NEON_DB       - NeonDB database name (default: prompt)
#   NEON_PASSWORD - NeonDB password (default: prompt)
#   PGPASSWORD    - PostgreSQL password (alternative to NEON_PASSWORD)
#
# ============================================================================

set -e  # Exit on error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo ""
echo "============================================================================"
echo "SpringFood Database Seed Data Execution"
echo "============================================================================"
echo ""

# Check if psql is installed
if ! command -v psql &> /dev/null; then
    echo -e "${RED}[ERROR]${NC} PostgreSQL client (psql) not found"
    echo ""
    echo "Please install PostgreSQL client:"
    echo "  - Ubuntu/Debian: sudo apt-get install postgresql-client"
    echo "  - macOS: brew install postgresql"
    echo "  - CentOS/RHEL: sudo yum install postgresql"
    echo ""
    exit 1
fi

echo -e "${GREEN}[OK]${NC} PostgreSQL client found: $(psql --version)"
echo ""

# Get NeonDB connection parameters
if [ -z "$NEON_HOST" ]; then
    read -p "Enter NeonDB host (e.g., ep-xxx-xxx.us-east-2.aws.neon.tech): " NEON_HOST
else
    echo "Using NEON_HOST from environment: $NEON_HOST"
fi

if [ -z "$NEON_USER" ]; then
    read -p "Enter NeonDB username (default: neondb_owner): " NEON_USER
    NEON_USER=${NEON_USER:-neondb_owner}
else
    echo "Using NEON_USER from environment: $NEON_USER"
fi

if [ -z "$NEON_DB" ]; then
    read -p "Enter NeonDB database name (default: neondb): " NEON_DB
    NEON_DB=${NEON_DB:-neondb}
else
    echo "Using NEON_DB from environment: $NEON_DB"
fi

if [ -z "$NEON_PASSWORD" ] && [ -z "$PGPASSWORD" ]; then
    echo ""
    echo -e "${YELLOW}[INFO]${NC} Password will be prompted by psql (or set PGPASSWORD environment variable)"
    echo ""
else
    if [ -n "$NEON_PASSWORD" ]; then
        echo "Using NEON_PASSWORD from environment"
        export PGPASSWORD="$NEON_PASSWORD"
    else
        echo "Using PGPASSWORD from environment"
    fi
fi

echo ""
echo "============================================================================"
echo "Connection Details"
echo "============================================================================"
echo "Host:     $NEON_HOST"
echo "User:     $NEON_USER"
echo "Database: $NEON_DB"
echo "Port:     5432 (default)"
echo "SSL:      require (NeonDB default)"
echo ""

# Confirm execution
read -p "Proceed with seed data execution? (y/n): " CONFIRM
if [ "$CONFIRM" != "y" ] && [ "$CONFIRM" != "Y" ]; then
    echo ""
    echo -e "${YELLOW}[CANCELLED]${NC} Seed data execution cancelled by user"
    echo ""
    exit 0
fi

echo ""
echo "============================================================================"
echo "Executing Seed Data"
echo "============================================================================"
echo ""

# Execute the master SQL script
if psql -h "$NEON_HOST" -U "$NEON_USER" -d "$NEON_DB" -p 5432 -f run_all_seeds.sql; then
    echo ""
    echo "============================================================================"
    echo -e "${GREEN}[SUCCESS]${NC} Seed data execution completed successfully"
    echo "============================================================================"
    echo ""
    echo "Next steps:"
    echo "  1. Verify record counts in database"
    echo "  2. Test predefined user accounts (see README.md)"
    echo "  3. Test application with seed data"
    echo ""
    exit 0
else
    EXIT_CODE=$?
    echo ""
    echo "============================================================================"
    echo -e "${RED}[ERROR]${NC} Seed data execution failed (exit code: $EXIT_CODE)"
    echo "============================================================================"
    echo ""
    echo "Common issues:"
    echo "  1. Connection refused - Check host, port, and network connectivity"
    echo "  2. Authentication failed - Verify username and password"
    echo "  3. Foreign key violation - Ensure schemas exist and are empty"
    echo "  4. Permission denied - Verify user has INSERT permissions"
    echo ""
    echo "Check the error messages above for details."
    echo ""
    exit $EXIT_CODE
fi
