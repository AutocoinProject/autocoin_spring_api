#!/bin/bash

# EC2에서 실행할 디버깅 스크립트
# Usage: ./debug-container.sh

echo "=== Container Debug Script ==="

# 환경 변수 확인
echo "1. Checking .env file:"
if [ -f .env ]; then
    echo "DB_URL line:"
    grep "DB_URL" .env | head -1
    echo "Total lines in .env:"
    wc -l .env
else
    echo "❌ .env file not found!"
fi

# 컨테이너 상태 확인
echo ""
echo "2. Docker container status:"
sudo docker ps -a | grep autocoin-api

# 컨테이너 환경 변수 확인
echo ""
echo "3. Container environment variables:"
sudo docker exec autocoin-api printenv | grep -E "DB_|SPRING_" | sort

# 컨테이너 로그 확인
echo ""
echo "4. Container logs (last 30 lines):"
sudo docker logs --tail 30 autocoin-api

# 디버그 엔드포인트 호출
echo ""
echo "5. Debug endpoint:"
curl -s http://localhost:8080/debug/env 2>/dev/null || echo "Debug endpoint not available"

echo ""
echo "=== Debug Complete ==="
