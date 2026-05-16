@echo off
REM Test Redis Connection Script for Windows
REM This script tests connection to Upstash Redis

echo ==========================================
echo Testing Redis Connection
echo ==========================================
echo.

REM Load environment variables from .env
if exist .env (
    echo Loading .env file...
    for /f "usebackq tokens=1,2 delims==" %%a in (".env") do (
        if not "%%a"=="" if not "%%a:~0,1%"=="#" (
            set "%%a=%%b"
        )
    )
    echo [OK] Loaded .env file
) else (
    echo [ERROR] .env file not found
    exit /b 1
)

echo.
echo Redis Configuration:
echo   Host: %REDIS_HOST%
echo   Port: %REDIS_PORT%
echo   SSL: %REDIS_SSL_ENABLED%
echo.

echo Testing with curl (REST API)...
if defined UPSTASH_REDIS_REST_URL (
    curl -s -H "Authorization: Bearer %UPSTASH_REDIS_REST_TOKEN%" "%UPSTASH_REDIS_REST_URL%/ping"
    if %errorlevel% equ 0 (
        echo.
        echo [OK] REST API connection successful!
    ) else (
        echo.
        echo [ERROR] REST API connection failed!
        exit /b 1
    )
) else (
    echo [WARNING] UPSTASH_REDIS_REST_URL not configured
)

echo.
echo ==========================================
echo Redis Connection Test Complete
echo ==========================================
pause
