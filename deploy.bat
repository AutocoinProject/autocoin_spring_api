@echo off
REM Autocoin API Docker 배포 스크립트 (Windows)

echo 🚀 Autocoin API Docker 배포를 시작합니다...

REM 환경 변수 파일 확인
if not exist .env.prod (
    echo ❌ .env.prod 파일이 없습니다. 파일을 생성해주세요.
    pause
    exit /b 1
)

REM 이전 컨테이너 정리
echo 🧹 이전 컨테이너를 정리합니다...
docker-compose -f docker-compose.prod.yml --env-file .env.prod down

REM JAR 파일 빌드
echo 🔨 애플리케이션을 빌드합니다...
gradlew.bat clean bootJar

REM JAR 파일 존재 확인
if not exist build\libs\*.jar (
    echo ❌ JAR 파일을 찾을 수 없습니다. 빌드를 다시 실행해주세요.
    pause
    exit /b 1
)

echo ✅ JAR 파일 빌드 완료

REM Docker 이미지 빌드 및 실행
echo 🐳 Docker 이미지를 빌드하고 컨테이너를 시작합니다...
docker-compose -f docker-compose.prod.yml --env-file .env.prod up -d --build

REM 서비스 상태 확인
echo ⏳ 서비스가 시작될 때까지 기다립니다...
timeout /t 30 /nobreak >nul

echo 🔍 서비스 상태를 확인합니다...
docker-compose -f docker-compose.prod.yml --env-file .env.prod ps

echo.
echo 🎉 배포가 완료되었습니다!
echo.
echo 📋 서비스 접속 정보:
echo - API Server: http://localhost:8080
echo - Health Check: http://localhost:8080/actuator/health
echo - Prometheus: http://localhost:9090
echo - Grafana: http://localhost:3001
echo.
echo 📊 모니터링:
echo - 로그 확인: docker-compose -f docker-compose.prod.yml logs -f autocoin-api
echo - 컨테이너 상태: docker-compose -f docker-compose.prod.yml ps
echo - 컨테이너 중지: docker-compose -f docker-compose.prod.yml down
echo.
echo 🛡️ 보안 체크리스트:
echo - [ ] .env.prod 파일의 모든 비밀값 설정 완료
echo - [ ] JWT_SECRET 256비트 이상 설정
echo - [ ] 데이터베이스 비밀번호 강화
echo - [ ] CORS 도메인 설정 확인
echo - [ ] Swagger 비활성화 (운영환경)
pause
