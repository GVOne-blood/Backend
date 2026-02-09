#!/bin/bash
# =====================================================
# Import Product Data to PostgreSQL
# =====================================================

echo ""
echo "========================================"
echo "  SPRING FOOD - IMPORT PRODUCT DATA"
echo "========================================"
echo ""

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo "[ERROR] Docker is not running!"
    echo "Please start Docker first."
    exit 1
fi

# Check if postgres container is running
if ! docker ps | grep -q postgres; then
    echo "[ERROR] PostgreSQL container is not running!"
    echo "Please run: docker-compose up -d"
    exit 1
fi

echo "[INFO] Importing product data..."
echo ""

# Execute SQL script
docker exec -i postgres psql -U admin -d product_db < product-data-import.sql

if [ $? -ne 0 ]; then
    echo ""
    echo "[ERROR] Failed to import data!"
    exit 1
fi

echo ""
echo "========================================"
echo "  IMPORT COMPLETED SUCCESSFULLY!"
echo "========================================"
echo ""
echo "Products imported: 110+"
echo "Categories: 18"
echo "Images: Real photos from Unsplash"
echo ""
echo "You can now view products in your app!"
echo ""
