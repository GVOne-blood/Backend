@echo off
REM ============================================================================
REM SpringFood Database Seed Data - Windows Execution Script
REM ============================================================================
REM This script executes all seed data SQL files on NeonDB using psql client
REM
REM Prerequisites:
REM   - PostgreSQL client (psql) installed and in PATH
REM   - NeonDB connection credentials
REM
REM Usage:
REM   run_seeds.bat
REM
REM Environment Variables (optional):
REM   NEON_HOST     - NeonDB host (default: prompt)
REM   NEON_USER     - NeonDB username (default: prompt)
REM   NEON_DB       - NeonDB database name (default: prompt)
REM   NEON_PASSWORD - NeonDB password (default: prompt)
REM
REM ============================================================================

setlocal enabledelayedexpansion

echo.
echo ============================================================================
echo SpringFood Database Seed Data Execution
echo ============================================================================
echo.

REM Check if psql is installed
where psql >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo [ERROR] PostgreSQL client (psql) not found in PATH
    echo.
    echo Please install PostgreSQL client:
    echo   - Download from: https://www.postgresql.org/download/windows/
    echo   - Or install via Chocolatey: choco install postgresql
    echo.
    pause
    exit /b 1
)

echo [OK] PostgreSQL client found
echo.

REM Get NeonDB connection parameters
if "%NEON_HOST%"=="" (
    set /p NEON_HOST="Enter NeonDB host (e.g., ep-xxx-xxx.us-east-2.aws.neon.tech): "
) else (
    echo Using NEON_HOST from environment: %NEON_HOST%
)

if "%NEON_USER%"=="" (
    set /p NEON_USER="Enter NeonDB username (default: neondb_owner): "
    if "!NEON_USER!"=="" set NEON_USER=neondb_owner
) else (
    echo Using NEON_USER from environment: %NEON_USER%
)

if "%NEON_DB%"=="" (
    set /p NEON_DB="Enter NeonDB database name (default: neondb): "
    if "!NEON_DB!"=="" set NEON_DB=neondb
) else (
    echo Using NEON_DB from environment: %NEON_DB%
)

if "%NEON_PASSWORD%"=="" (
    echo.
    echo [INFO] Password will be prompted by psql (or use PGPASSWORD environment variable)
    echo.
) else (
    echo Using NEON_PASSWORD from environment
    set PGPASSWORD=%NEON_PASSWORD%
)

echo.
echo ============================================================================
echo Connection Details
echo ============================================================================
echo Host:     %NEON_HOST%
echo User:     %NEON_USER%
echo Database: %NEON_DB%
echo Port:     5432 (default)
echo SSL:      require (NeonDB default)
echo.

REM Confirm execution
set /p CONFIRM="Proceed with seed data execution? (y/n): "
if /i not "%CONFIRM%"=="y" (
    echo.
    echo [CANCELLED] Seed data execution cancelled by user
    echo.
    pause
    exit /b 0
)

echo.
echo ============================================================================
echo Executing Seed Data
echo ============================================================================
echo.

REM Execute the master SQL script
psql -h %NEON_HOST% -U %NEON_USER% -d %NEON_DB% -p 5432 -f run_all_seeds.sql

REM Check execution result
if %ERRORLEVEL% equ 0 (
    echo.
    echo ============================================================================
    echo [SUCCESS] Seed data execution completed successfully
    echo ============================================================================
    echo.
    echo Next steps:
    echo   1. Verify record counts in database
    echo   2. Test predefined user accounts (see README.md)
    echo   3. Test application with seed data
    echo.
) else (
    echo.
    echo ============================================================================
    echo [ERROR] Seed data execution failed
    echo ============================================================================
    echo.
    echo Common issues:
    echo   1. Connection refused - Check host, port, and network connectivity
    echo   2. Authentication failed - Verify username and password
    echo   3. Foreign key violation - Ensure schemas exist and are empty
    echo   4. Permission denied - Verify user has INSERT permissions
    echo.
    echo Check the error messages above for details.
    echo.
    pause
    exit /b 1
)

REM Clean up
set PGPASSWORD=

echo.
pause
exit /b 0
