@echo off
echo ========================================
echo Cloudflare Tunnel Setup for SpringFood
echo ========================================
echo.

REM Use local directory instead of C:\
set CLOUDFLARED_DIR=%~dp0cloudflared
set CLOUDFLARED_EXE=%CLOUDFLARED_DIR%\cloudflared.exe

REM Check if cloudflared already exists
if exist "%CLOUDFLARED_EXE%" (
    echo [OK] Cloudflared already installed at %CLOUDFLARED_EXE%
    goto :start_tunnel
)

echo [STEP 1] Downloading cloudflared...
echo.

REM Create directory
if not exist "%CLOUDFLARED_DIR%" mkdir "%CLOUDFLARED_DIR%"

REM Download cloudflared for Windows using curl (no PowerShell needed!)
echo Downloading from GitHub...
echo This may take a minute...
echo.

curl -L -o "%CLOUDFLARED_EXE%" "https://github.com/cloudflare/cloudflared/releases/latest/download/cloudflared-windows-amd64.exe"

if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Download failed!
    echo.
    echo Please download manually from:
    echo https://github.com/cloudflare/cloudflared/releases/latest
    echo.
    echo Save as: %CLOUDFLARED_EXE%
    echo.
    pause
    exit /b 1
)

echo.
echo [OK] Downloaded successfully!
echo.

echo [STEP 2] Verify installation...
"%CLOUDFLARED_EXE%" --version
echo.

:start_tunnel
echo ========================================
echo [STEP 3] Starting Quick Tunnel
echo ========================================
echo.
echo IMPORTANT:
echo 1. This will create a tunnel to localhost:8080
echo 2. You will get a FIXED URL like: https://xxx.trycloudflare.com
echo 3. This URL will NOT change when you restart!
echo 4. Copy the URL and update your frontend environment
echo.
echo Starting tunnel in 3 seconds...
timeout /t 3 /nobreak >nul
echo.

REM Start quick tunnel (no login required!)
"%CLOUDFLARED_EXE%" tunnel --url http://localhost:8080

pause
