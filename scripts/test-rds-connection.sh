#!/bin/bash

# RDS Connection Test Script
# Usage: ./test-rds-connection.sh

echo "=== RDS Connection Test ==="

# DB 정보 (GitHub Secrets에서 가져온 값으로 대체)
DB_HOST="your-rds-endpoint.rds.amazonaws.com"
DB_PORT="3306"
DB_NAME="your-database-name"
DB_USER="your-username"
DB_PASS="your-password"

# MySQL 클라이언트 설치 확인
if ! command -v mysql &> /dev/null; then
    echo "MySQL client not found. Installing..."
    sudo apt-get update
    sudo apt-get install -y mysql-client
fi

# 연결 테스트
echo "Testing connection to RDS..."
mysql -h "$DB_HOST" -P "$DB_PORT" -u "$DB_USER" -p"$DB_PASS" -e "SELECT 1;" 2>&1

if [ $? -eq 0 ]; then
    echo "✅ RDS connection successful!"
    
    # 데이터베이스 존재 확인
    echo "Checking database..."
    mysql -h "$DB_HOST" -P "$DB_PORT" -u "$DB_USER" -p"$DB_PASS" -e "USE $DB_NAME; SHOW TABLES;" 2>&1
else
    echo "❌ RDS connection failed!"
    echo "Please check:"
    echo "1. RDS endpoint is correct"
    echo "2. Security group allows connection from EC2"
    echo "3. Username and password are correct"
    echo "4. RDS instance is running"
fi

# 네트워크 연결 테스트
echo ""
echo "Network connectivity test:"
nc -zv "$DB_HOST" "$DB_PORT" 2>&1

# DNS 확인
echo ""
echo "DNS resolution test:"
nslookup "$DB_HOST" 2>&1

echo "=== Test Complete ==="
