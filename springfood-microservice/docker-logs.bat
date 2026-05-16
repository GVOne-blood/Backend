@echo off
REM ============================================================================
REM Helper to read logs from the SpringFood docker stack.
REM
REM Usage:
REM   docker-logs.bat               -> show status of all containers
REM   docker-logs.bat <service>     -> tail 300 lines of <service>
REM   docker-logs.bat <service> 1000 -> tail 1000 lines
REM   docker-logs.bat errors        -> grep ERROR/Exception across all services
REM   docker-logs.bat dump          -> write logs of all services to .\logs\
REM ============================================================================

setlocal
set COMPOSE=docker compose -f docker-compose.apps.yml

if "%1"=="" goto status
if /I "%1"=="status" goto status
if /I "%1"=="errors" goto errors
if /I "%1"=="dump" goto dump
goto one

:status
%COMPOSE% ps -a
goto end

:one
set TAIL=%2
if "%TAIL%"=="" set TAIL=300
%COMPOSE% logs --tail=%TAIL% %1
goto end

:errors
%COMPOSE% logs --tail=1000 ^| findstr /I /R "error exception \"caused by\""
goto end

:dump
if not exist logs mkdir logs
for %%S in (eureka-server api-gateway authentication cart-service order-service shop-service product-service payment-service notification media statistical-report action-log chat) do (
    echo === %%S ===
    %COMPOSE% logs --tail=500 %%S > logs\%%S.log 2>&1
)
echo Logs written to .\logs\
goto end

:end
endlocal
