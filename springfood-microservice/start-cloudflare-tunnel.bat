@echo off
echo ========================================
echo Starting Cloudflare Tunnel - SpringFood
echo ========================================
echo.

REM Use local directory
set CLOUDFLARED_DIR=%~dp0cloudflared
set CLOUDFLARED_EXE=%CLOUDFLARED_DIR%\cloudflared.exe

REM Check if cloudflared exists
if not exist "%CLOUDFLARED_EXE%" (
    echo [ERROR] Cloudflared not found!
    echo.
    echo Please run: setup-cloudflare-tunnel.bat first
    echo.
    pause
    exit /b 1
)

echo [INFO] Checking if API Gateway is running...
echo.

REM Check if port 8080 is listening
netstat -an | findstr ":8080" | findstr "LISTENING" >nul
if %ERRORLEVEL% NEQ 0 (
    echo [WARNING] API Gateway (port 8080) is not running!
    echo.
    echo Please start API Gateway first:
    echo   cd api-gateway
    echo   mvn spring-boot:run
    echo.
    echo Press any key to continue anyway, or Ctrl+C to cancel...
    pause >nul
)

echo ========================================
echo Starting Tunnel to localhost:8080
echo ========================================
echo.
echo IMPORTANT NOTES:
echo.
echo 1. Your tunnel URL will be displayed below
echo 2. Copy the HTTPS URL (e.g., https://xxx.trycloudflare.com)
echo 3. This URL is FIXED and won't change on restart
echo 4. Update your frontend environment.prod.ts with this URL
echo 5. Keep this window open while testing
echo.
echo Press Ctrl+C to stop the tunnel
echo ========================================
echo.

"%CLOUDFLARED_EXE%" tunnel --url http://localhost:8080
