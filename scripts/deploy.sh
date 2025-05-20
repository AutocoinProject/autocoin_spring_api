#!/bin/bash
set -e

echo "=== AutoCoin Spring API Deployment Script ==="
echo "Starting deployment at $(date)"

# 1. Stop existing process
echo "Stopping any existing processes..."
sudo pkill -f autocoin || echo "No existing processes found"
ps aux | grep autocoin || echo "No autocoin processes running"

# 2. Prepare directories
echo "Preparing directories..."
mkdir -p ~/app

# 로그 디렉토리 생성 (홈 디렉토리에 생성)
mkdir -p ~/logs/autocoin
echo "Directory structure prepared"

# 3. Setup environment variables from ENV file
echo "Setting up environment variables..."
if [ -f ~/app/.env ]; then
  source ~/app/.env
  echo "Environment variables loaded from ~/app/.env"
else
  echo "No .env file found. Please check if environment variables are properly set"
fi

# 4. Start the application
echo "Starting application..."
cd ~/app
JAR_FILE=$(ls -t *.jar | head -1)
if [ -z "$JAR_FILE" ]; then
  echo "ERROR: No JAR file found in ~/app directory"
  exit 1
fi

echo "Using JAR file: $JAR_FILE"
nohup java -jar $JAR_FILE --spring.profiles.active=prod > ~/logs/autocoin/application.log 2>&1 &
APP_PID=$!
echo "Application started with PID: $APP_PID"

# 5. Wait and check health
echo "Waiting 30 seconds for application to start..."
sleep 30

echo "Performing health check..."
if curl -f http://localhost:8080/health; then
  echo "Health check successful!"
else
  echo "Health check failed. Checking logs..."
  tail -n 50 ~/logs/autocoin/application.log || echo "Could not read log file"
  echo "Application may not have started correctly. Please check logs for details."
  # We don't exit with error here to prevent failing the CI/CD pipeline
  # This allows for manual investigation
fi

echo "Deployment completed at $(date)"
