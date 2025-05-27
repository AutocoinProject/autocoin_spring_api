@echo off
REM Autocoin API 모니터링 환경 시작 스크립트 (Windows)

echo 🚀 Autocoin API 모니터링 환경을 시작합니다...

REM Docker가 실행 중인지 확인
docker info >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ Docker가 실행되지 않았습니다. Docker를 시작해주세요.
    pause
    exit /b 1
)

REM 환경 변수 파일 확인
if not exist .env (
    echo ⚠️  .env 파일이 없습니다. 환경 변수를 설정해주세요.
    echo SENTRY_DSN=your-sentry-dsn-here > .env
    echo SLACK_WEBHOOK_URL=your-slack-webhook-url-here >> .env
    echo .env 파일을 생성했습니다. 필요한 값들을 설정해주세요.
)

REM 모니터링 디렉토리 생성
echo 📁 모니터링 디렉토리 구조를 확인합니다...
if not exist monitoring\prometheus\rules mkdir monitoring\prometheus\rules
if not exist monitoring\grafana\provisioning\datasources mkdir monitoring\grafana\provisioning\datasources
if not exist monitoring\grafana\provisioning\dashboards mkdir monitoring\grafana\provisioning\dashboards
if not exist monitoring\grafana\dashboards mkdir monitoring\grafana\dashboards
if not exist monitoring\alertmanager mkdir monitoring\alertmanager

REM Docker Compose로 모니터링 스택 시작
echo 🐳 모니터링 컨테이너들을 시작합니다...
docker-compose -f docker-compose.monitoring.yml up -d

REM 서비스 상태 확인
echo ⏳ 서비스들이 시작될 때까지 잠시 기다립니다...
timeout /t 30 /nobreak >nul

echo 🔍 서비스 상태를 확인합니다...
docker-compose -f docker-compose.monitoring.yml ps

echo.
echo ✅ 모니터링 환경이 시작되었습니다!
echo.
echo 📊 접속 정보:
echo - Prometheus: http://localhost:9090
echo - Grafana: http://localhost:3001 (admin/autocoin123!)
echo - AlertManager: http://localhost:9093
echo - Node Exporter: http://localhost:9100/metrics
echo.
echo 🔗 API 모니터링 엔드포인트:
echo - Health Check: http://localhost:8080/actuator/health
echo - Metrics: http://localhost:8080/actuator/metrics
echo - Prometheus Metrics: http://localhost:8080/actuator/prometheus
echo.
echo 🧪 테스트 엔드포인트:
echo - Sentry Test: POST http://localhost:8080/api/monitoring/test-sentry
echo - Metrics Test: POST http://localhost:8080/api/monitoring/test-metrics
echo - Performance Test: POST http://localhost:8080/api/monitoring/test-performance
echo.
echo ⚠️  Spring Boot 애플리케이션도 실행해주세요: gradlew.bat bootRun
pause
