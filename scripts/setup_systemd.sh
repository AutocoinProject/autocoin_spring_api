#!/bin/bash
set -e

echo "=== Setting up AutoCoin as a systemd service ==="

# 1. Create systemd service file
echo "Creating systemd service file..."

# Get the username
CURRENT_USER=$(whoami)

# Get the actual JAR file name
JAR_NAME=$(ls -t ~/app/*.jar | head -1 | xargs basename)
echo "Using JAR file: $JAR_NAME"

# Create service file content
cat > autocoin.service << EOF
[Unit]
Description=AutoCoin Spring API Service
After=network.target mysql.service

[Service]
User=${CURRENT_USER}
WorkingDirectory=/home/${CURRENT_USER}/app
ExecStart=/usr/bin/java -jar /home/${CURRENT_USER}/app/${JAR_NAME} --spring.profiles.active=prod -Xmx512m -Xms256m
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
