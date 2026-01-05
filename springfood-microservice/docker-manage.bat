@echo off
REM Docker Management Script for SpringFood Microservices

if "%1"=="" goto help
if "%1"=="start" goto start
if "%1"=="stop" goto stop
if "%1"=="restart" goto restart
if "%1"=="status" goto status
if "%1"=="logs" goto logs
if "%1"=="clean" goto clean
goto help

:start
echo Starting all services...
docker-compose up -d
echo.
echo Waiting for services to be ready...
timeout /t 10 /nobreak >nul
docker-compose ps
echo.
echo Services started! Access:
echo - MinIO Console: http://localhost:9001 (minioadmin/minioadmin)
echo - PostgreSQL: localhost:5432 (postgres/123456)
echo - Redis: localhost:6379 (password: 123456)
echo - Kafka: localhost:9092
goto end

:stop
echo Stopping all services...
docker-compose down
echo Services stopped!
goto end

:restart
echo Restarting all services...
docker-compose restart
echo Services restarted!
goto end

:status
echo Checking service status...
docker-compose ps
goto end

:logs
if "%2"=="" (
    echo Showing logs for all services...
    docker-compose logs -f
) else (
    echo Showing logs for %2...
    docker-compose logs -f %2
)
goto end

:clean
echo WARNING: This will remove all data!
set /p confirm="Are you sure? (y/n): "
if /i "%confirm%"=="y" (
    echo Stopping and removing all containers and volumes...
    docker-compose down -v
    echo All data cleaned!
) else (
    echo Cancelled.
)
goto end

:help
echo SpringFood Docker Management
echo.
echo Usage: docker-manage.bat [command] [options]
echo.
echo Commands:
echo   start      - Start all services
echo   stop       - Stop all services
echo   restart    - Restart all services
echo   status     - Show service status
echo   logs       - Show logs (add service name for specific service)
echo   clean      - Stop and remove all data
echo.
echo Examples:
echo   docker-manage.bat start
echo   docker-manage.bat logs minio
echo   docker-manage.bat status
goto end

:end
