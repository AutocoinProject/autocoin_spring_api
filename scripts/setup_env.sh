#!/bin/bash
set -e

echo "=== Setting up environment variables ==="

# 기본 디렉토리 생성
mkdir -p ~/app

# Create .env file with actual values for your environment
cat > ~/app/.env << EOF
# 데이터베이스 설정
DB_USERNAME="actual_db_username"
DB_PASSWORD="actual_db_password"
DATABASE_URL="jdbc:mysql://your-db-host:3306/autocoin?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"

# JWT 설정
JWT_SECRET="your_jwt_secret_key"
JWT_EXPIRATION=1800000

# 업비트 API 설정
UPBIT_API_URL="https://api.upbit.com"
UPBIT_ENCRYPTION_KEY="your_upbit_encryption_key"

# AWS S3 설정
AWS_ACCESS_KEY="your_aws_access_key"
AWS_SECRET_KEY="your_aws_secret_key"
AWS_S3_BUCKET="your_s3_bucket_name"
AWS_REGION="ap-northeast-2"

# OAuth2 클라이언트 설정
GOOGLE_CLIENT_ID="your_google_client_id"
GOOGLE_CLIENT_SECRET="your_google_client_secret"
KAKAO_CLIENT_ID="your_kakao_client_id"
KAKAO_CLIENT_SECRET="your_kakao_client_secret"
OAUTH2_REDIRECT_URI="https://your-domain.com/oauth2/redirect"

# CORS 설정
CORS_ALLOWED_ORIGINS="https://your-frontend-domain.com,http://localhost:3000"

# 서버 설정
SERVER_PORT=8080
EOF

echo "Environment variables configuration created"
chmod 600 ~/app/.env
echo "File permissions set to secure mode"
