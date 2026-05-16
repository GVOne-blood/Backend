@echo off
echo ========================================
echo SpringFood WebSocket Test
echo ========================================
echo.

REM Check if chat service is running
echo [1/3] Checking if chat service is running...
curl -s http://localhost:9098/actuator/health > nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] Chat service is not running on port 9098
    echo.
    echo Please start chat service first:
    echo   cd chat
    echo   mvn spring-boot:run
    echo.
    pause
    exit /b 1
)
echo [OK] Chat service is running
echo.

REM Check WebSocket endpoint
echo [2/3] Checking WebSocket endpoint...
curl -s -I http://localhost:9098/ws > nul 2>&1
if %errorlevel% neq 0 (
    echo [WARNING] Could not verify WebSocket endpoint
) else (
    echo [OK] WebSocket endpoint is accessible
)
echo.

REM Open test HTML file
echo [3/3] Opening WebSocket test client...
start "" "test-resources\websocket-angular-test.html"
echo [OK] Test client opened in browser
echo.

echo ========================================
echo Test Instructions:
echo ========================================
echo 1. Login to SpringFood app (http://localhost:4200)
echo 2. Get JWT token from browser cookies
echo 3. Paste token in test client
echo 4. Click "Connect"
echo 5. Send test message: "Xin chao"
echo.
echo Expected result:
echo - Connection status: Connected
echo - AI response streaming in real-time
echo.
echo Troubleshooting:
echo - If connection fails, check JWT token is valid
echo - If no AI response, check GEMINI_API_KEY in .env
echo - Check backend logs: chat/logs/spring.log
echo.
pause
