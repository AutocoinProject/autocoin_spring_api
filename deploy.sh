#!/bin/bash

# Autocoin API Docker 배포 스크립트

set -e

echo "🚀 Autocoin API Docker 배포를 시작합니다..."

# 환경 변수 확인
if [ ! -f .env.prod ]; then
    echo "❌ .env.prod 파일이 없습니다. 파일을 생성해주세요."
    exit 1
fi

# 이전 컨테이너 정리
echo "🧹 이전 컨테이너를 정리합니다..."
docker-compose -f docker-compose.prod.yml --env-file .env.prod down

# JAR 파일 빌드
echo "🔨 애플리케이션을 빌드합니다..."
./gradlew clean bootJar

# JAR 파일 존재 확인
if [ ! -f build/libs/*.jar ]; then
    echo "❌ JAR 파일을 찾을 수 없습니다. 빌드를 다시 실행해주세요."
    exit 1
fi

echo "✅ JAR 파일 빌드 완료"

# Docker 이미지 빌드 및 실행
echo "🐳 Docker 이미지를 빌드하고 컨테이너를 시작합니다..."
docker-compose -f docker-compose.prod.yml --env-file .env.prod up -d --build

# 서비스 상태 확인
echo "⏳ 서비스가 시작될 때까지 기다립니다..."
sleep 30

echo "🔍 서비스 상태를 확인합니다..."
docker-compose -f docker-compose.prod.yml --env-file .env.prod ps

# 헬스체크
echo "🏥 헬스체크를 수행합니다..."
for i in {1..10}; do
    if curl -f http://localhost:8080/actuator/health > /dev/null 2>&1; then
        echo "✅ 애플리케이션이 정상적으로 시작되었습니다!"
        break
    else
        echo "⏳ 애플리케이션 시작 대기 중... ($i/10)"
        sleep 10
    fi
    
    if [ $i -eq 10 ]; then
        echo "❌ 애플리케이션 시작에 실패했습니다."
        echo "📋 로그를 확인해주세요:"
        docker-compose -f docker-compose.prod.yml --env-file .env.prod logs autocoin-api
        exit 1
    fi
done

echo ""
echo "🎉 배포가 완료되었습니다!"
echo ""
echo "📋 서비스 접속 정보:"
echo "- API Server: http://localhost:8080"
echo "- Health Check: http://localhost:8080/actuator/health"
echo "- Prometheus: http://localhost:9090"
echo "- Grafana: http://localhost:3001"
echo ""
echo "📊 모니터링:"
echo "- 로그 확인: docker-compose -f docker-compose.prod.yml logs -f autocoin-api"
echo "- 컨테이너 상태: docker-compose -f docker-compose.prod.yml ps"
echo "- 컨테이너 중지: docker-compose -f docker-compose.prod.yml down"
echo ""
echo "🛡️ 보안 체크리스트:"
echo "- [ ] .env.prod 파일의 모든 비밀값 설정 완료"
echo "- [ ] JWT_SECRET 256비트 이상 설정"
echo "- [ ] 데이터베이스 비밀번호 강화"
echo "- [ ] CORS 도메인 설정 확인"
echo "- [ ] Swagger 비활성화 (운영환경)"
