#!/bin/bash

# Autocoin API 모니터링 환경 시작 스크립트

echo "🚀 Autocoin API 모니터링 환경을 시작합니다..."

# Docker가 실행 중인지 확인
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker가 실행되지 않았습니다. Docker를 시작해주세요."
    exit 1
fi

# 환경 변수 파일 확인
if [ ! -f .env ]; then
    echo "⚠️  .env 파일이 없습니다. 환경 변수를 설정해주세요."
    echo "SENTRY_DSN=your-sentry-dsn-here" > .env
    echo "SLACK_WEBHOOK_URL=your-slack-webhook-url-here" >> .env
    echo ".env 파일을 생성했습니다. 필요한 값들을 설정해주세요."
fi

# 모니터링 디렉토리 생성
echo "📁 모니터링 디렉토리 구조를 확인합니다..."
mkdir -p monitoring/prometheus/rules
mkdir -p monitoring/grafana/provisioning/datasources
mkdir -p monitoring/grafana/provisioning/dashboards
mkdir -p monitoring/grafana/dashboards
mkdir -p monitoring/alertmanager

# Docker Compose로 모니터링 스택 시작
echo "🐳 모니터링 컨테이너들을 시작합니다..."
docker-compose -f docker-compose.monitoring.yml up -d

# 서비스 상태 확인
echo "⏳ 서비스들이 시작될 때까지 잠시 기다립니다..."
sleep 30

echo "🔍 서비스 상태를 확인합니다..."
docker-compose -f docker-compose.monitoring.yml ps

echo ""
echo "✅ 모니터링 환경이 시작되었습니다!"
echo ""
echo "📊 접속 정보:"
echo "- Prometheus: http://localhost:9090"
echo "- Grafana: http://localhost:3001 (admin/autocoin123!)"
echo "- AlertManager: http://localhost:9093"
echo "- Node Exporter: http://localhost:9100/metrics"
echo ""
echo "🔗 API 모니터링 엔드포인트:"
echo "- Health Check: http://localhost:8080/actuator/health"
echo "- Metrics: http://localhost:8080/actuator/metrics"
echo "- Prometheus Metrics: http://localhost:8080/actuator/prometheus"
echo ""
echo "🧪 테스트 엔드포인트:"
echo "- Sentry Test: POST http://localhost:8080/api/monitoring/test-sentry"
echo "- Metrics Test: POST http://localhost:8080/api/monitoring/test-metrics"
echo "- Performance Test: POST http://localhost:8080/api/monitoring/test-performance"
echo ""
echo "⚠️  Spring Boot 애플리케이션도 실행해주세요: ./gradlew bootRun"
