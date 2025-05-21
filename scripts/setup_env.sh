#!/bin/bash
set -e

echo "=== Setting up environment variables ==="

# 기본 디렉토리 생성
mkdir -p ~/app

# Create .env file with actual values for your environment
cat > ~/app/.env << EOF
# 데이터베이스 설정
DB_USERNAME="{{DB_USERNAME}}"
DB_PASSWORD="{{DB_PASSWORD}}"
DATABASE_URL="{{DATABASE_URL}}"

# JWT 설정
JWT_SECRET="{{JWT_SECRET}}"
JWT_EXPIRATION=1800000

# 업비트 API 설정
UPBIT_API_URL="https://api.upbit.com"
UPBIT_ENCRYPTION_KEY="{{UPBIT_ENCRYPTION_KEY}}"

# AWS S3 설정
AWS_ACCESS_KEY="{{AWS_ACCESS_KEY}}"
AWS_SECRET_KEY="{{AWS_SECRET_KEY}}"
AWS_S3_BUCKET="{{AWS_S3_BUCKET}}"
AWS_REGION="{{AWS_REGION}}"

# OAuth2 클라이언트 설정
GOOGLE_CLIENT_ID="{{GOOGLE_CLIENT_ID}}"
GOOGLE_CLIENT_SECRET="{{GOOGLE_CLIENT_SECRET}}"
KAKAO_CLIENT_ID="kakao_client_id"
KAKAO_CLIENT_SECRET="{{KAKAO_CLIENT_SECRET}}"
OAUTH2_REDIRECT_URI="https://your-domain.com/oauth2/redirect"

# CORS 설정
CORS_ALLOWED_ORIGINS="https://your-frontend-domain.com,http://localhost:3000"

# 서버 설정
SERVER_PORT=8080

# SERP API 설정
SERP_API_KEY="{{SERP_API_KEY}}"
EOF

echo "Environment variables configuration created"
chmod 600 ~/app/.env
echo "File permissions set to secure mode"
