@echo off
REM =====================================================
REM Import Product Data to PostgreSQL
REM =====================================================

echo.
echo ========================================
echo   SPRING FOOD - IMPORT PRODUCT DATA
echo ========================================
echo.

REM Check if Docker is running
docker info >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Docker is not running!
    echo Please start Docker Desktop first.
    pause
    exit /b 1
)

REM Check if postgres container is running
docker ps | findstr postgres >nul 2>&1
if errorlevel 1 (
    echo [ERROR] PostgreSQL container is not running!
    echo Please run: docker-manage.bat start
    pause
    exit /b 1
)

echo [INFO] Importing product data...
echo.

REM Execute SQL script
docker exec -i postgres psql -U admin -d product_db < product-data-import.sql

if errorlevel 1 (
    echo.
    echo [ERROR] Failed to import data!
    pause
    exit /b 1
)

echo.
echo ========================================
echo   IMPORT COMPLETED SUCCESSFULLY!
echo ========================================
echo.
echo Products imported: 110+
echo Categories: 18
echo Images: Real photos from Unsplash
echo.
echo You can now view products in your app!
echo.

pause
