#!/bin/bash
set -e

echo "=== AutoCoin Spring API Deployment Script ==="
echo "Starting deployment at $(date)"

# 1. Prepare directories
echo "Preparing directories..."
mkdir -p ~/app
mkdir -p ~/logs/autocoin
echo "Directory structure prepared"

# 2. Setup environment variables from ENV file
echo "Setting up environment variables..."
if [ -f ~/app/.env ]; then
  echo "Environment variables file already exists"
else
  echo "No .env file found. Creating a new one..."
  if [ -f ./setup_env.sh ]; then
    chmod +x ./setup_env.sh
    ./setup_env.sh
  else
    echo "WARNING: setup_env.sh not found. You need to manually set up your .env file!"
  fi
fi

# 3. Check for JAR file
echo "Checking for JAR file..."
JAR_FILE=$(ls -t ~/app/*.jar 2>/dev/null | head -1)
if [ -z "$JAR_FILE" ]; then
  echo "No existing JAR file found. Checking current directory..."
  
  NEW_JAR=$(ls -t *.jar 2>/dev/null | head -1)
  if [ -z "$NEW_JAR" ]; then
    echo "ERROR: No JAR file found in current directory or ~/app directory"
    echo "Please make sure your application is built and the JAR file is available"
    exit 1
  else
    echo "Found JAR file: $NEW_JAR. Moving to ~/app directory..."
    cp $NEW_JAR ~/app/
    JAR_FILE=~/app/$NEW_JAR
  fi
fi

echo "Using JAR file: $JAR_FILE"

# 4. Set up as systemd service
echo "Setting up as systemd service..."
if [ -f ./setup_systemd.sh ]; then
  chmod +x ./setup_systemd.sh
  ./setup_systemd.sh
else
  echo "setup_systemd.sh not found. Creating it on the fly..."
  
  # Create systemd setup script
  cat > ./setup_systemd.sh << 'EOF'
#!/bin/bash
set -e

echo "=== Setting up AutoCoin as a systemd service ==="

# 1. Create systemd service file
echo "Creating systemd service file..."

# Get the username
CURRENT_USER=$(whoami)

# Create service file content
cat > autocoin.service << EOF
[Unit]
Description=AutoCoin Spring API Service
After=network.target mysql.service

[Service]
User=${CURRENT_USER}
WorkingDirectory=/home/${CURRENT_USER}/app
ExecStart=/usr/bin/java -jar /home/${CURRENT_USER}/app/\$(ls -t /home/${CURRENT_USER}/app/*.jar | head -1) --spring.profiles.active=prod -Xmx512m -Xms256m
SuccessExitStatus=143
TimeoutStopSec=10
Restart=always
RestartSec=5
EnvironmentFile=/home/${CURRENT_USER}/app/.env

[Install]
WantedBy=multi-user.target
EOF

# 2. Copy to systemd directory
echo "Installing service file to systemd..."
sudo mv autocoin.service /etc/systemd/system/
sudo chmod 644 /etc/systemd/system/autocoin.service

# 3. Reload systemd and enable service
echo "Enabling and starting the service..."
sudo systemctl daemon-reload
sudo systemctl enable autocoin.service
sudo systemctl start autocoin.service

# 4. Check service status
echo "Checking service status:"
sudo systemctl status autocoin.service

echo "=== Setup completed ==="
echo "You can control the service with the following commands:"
echo "  sudo systemctl start autocoin.service"
echo "  sudo systemctl stop autocoin.service"
echo "  sudo systemctl restart autocoin.service"
echo "  sudo systemctl status autocoin.service"
echo "The service is now configured to start automatically on system boot."
EOF
  
  chmod +x ./setup_systemd.sh
  ./setup_systemd.sh
fi

# 5. Verify deployment
echo "Waiting 30 seconds for application to start..."
sleep 30

echo "Performing health check..."
# Spring Boot 2.x: /actuator/health, Spring Boot 1.x: /health
HEALTH_ENDPOINTS=("/actuator/health" "/health")
HEALTH_CHECK_SUCCESS=false

for endpoint in "${HEALTH_ENDPOINTS[@]}"; do
  if curl -f http://localhost:8080$endpoint; then
    echo "Health check successful at $endpoint!"
    HEALTH_CHECK_SUCCESS=true
    break
  fi
done

if [ "$HEALTH_CHECK_SUCCESS" = false ]; then
  echo "Health check failed. Checking logs..."
  sudo journalctl -u autocoin -n 50 || echo "Could not read systemd logs"
  echo "Application may not have started correctly. Please check logs for details."
fi

echo "Deployment completed at $(date)"
echo "Application is set up as a systemd service and will automatically start on server reboot."
echo "You can control the service with:"
echo "  sudo systemctl start autocoin"
echo "  sudo systemctl stop autocoin"
echo "  sudo systemctl restart autocoin"
echo "  sudo systemctl status autocoin"
