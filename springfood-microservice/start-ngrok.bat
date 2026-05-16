@echo off
echo ========================================
echo Starting Ngrok Tunnel for API Gateway
echo ========================================
echo.
echo Exposing localhost:8080 to internet...
echo.
echo IMPORTANT:
echo 1. Copy the HTTPS URL from the output below
echo 2. Update frontend environment files with this URL
echo 3. Keep this window open while testing
echo.
echo Press Ctrl+C to stop the tunnel
echo ========================================
echo.

C:\ngrok\ngrok.exe http 8080
