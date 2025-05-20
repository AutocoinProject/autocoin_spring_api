#!/bin/bash
set -e

echo "=== Setting up environment variables ==="

# Create .env file
cat > ~/app/.env << EOF
DB_USERNAME="{{DB_USERNAME}}"
DB_PASSWORD="{{DB_PASSWORD}}"
DATABASE_URL="{{DATABASE_URL}}"
JWT_SECRET="{{JWT_SECRET}}"
UPBIT_ENCRYPTION_KEY="{{UPBIT_ENCRYPTION_KEY}}"
AWS_ACCESS_KEY="{{AWS_ACCESS_KEY}}"
AWS_SECRET_KEY="{{AWS_SECRET_KEY}}"
AWS_S3_BUCKET="{{AWS_S3_BUCKET}}"
AWS_REGION="{{AWS_REGION}}"
GOOGLE_CLIENT_ID="{{GOOGLE_CLIENT_ID}}"
GOOGLE_CLIENT_SECRET="{{GOOGLE_CLIENT_SECRET}}"
KAKAO_CLIENT_SECRET="{{KAKAO_CLIENT_SECRET}}"
SERP_API_KEY="{{SERP_API_KEY}}"
EOF

echo "Environment variables configuration created"
chmod 600 ~/app/.env
echo "File permissions set to secure mode"
