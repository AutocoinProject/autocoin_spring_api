@echo off
chcp 65001
echo Starting Autocoin Monitoring...

REM Check if Docker is running
docker info >nul 2>&1
if %errorlevel% neq 0 (
    echo Docker is not running. Please start Docker Desktop.
    pause
    exit /b 1
)

REM Create .env file if not exists
if not exist .env (
    echo Creating .env file...
    echo SENTRY_DSN=your-sentry-dsn-here > .env
    echo SLACK_WEBHOOK_URL=your-slack-webhook-url-here >> .env
)

REM Start monitoring containers
echo Starting monitoring containers...
docker-compose -f docker-compose.monitoring.yml up -d

REM Wait for services to start
timeout /t 30 /nobreak >nul

REM Show container status
docker-compose -f docker-compose.monitoring.yml ps

echo.
echo Monitoring started successfully!
echo.
echo Access URLs:
echo - Grafana: http://localhost:3001 (admin/autocoin123!)
echo - Prometheus: http://localhost:9090
echo - AlertManager: http://localhost:9093
echo.
echo Please also start Spring Boot: gradlew.bat bootRun
pause
