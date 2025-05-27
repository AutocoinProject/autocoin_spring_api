#!/bin/bash
set -e

echo "=== Checking system requirements for AutoCoin deployment ==="

# 1. Check Java version
echo "Checking Java version..."
if command -v java &> /dev/null; then
  java_version=$(java -version 2>&1 | head -1 | cut -d'"' -f2 | sed 's/^1\.//' | cut -d'.' -f1)
  echo "Found Java version: $java_version"
  
  if [ "$java_version" -lt 17 ]; then
    echo "WARNING: Java version $java_version detected. This application requires Java 17 or higher."
    echo "Please install Java 17+ before deploying."
  else
    echo "Java version check passed."
  fi
else
  echo "ERROR: Java not found. Please install Java 17 or higher."
fi

# 2. Check for sudo access (required for systemd)
echo "Checking sudo access..."
if sudo -v &> /dev/null; then
  echo "Sudo access confirmed. You have permission to set up systemd services."
else
  echo "WARNING: You don't have sudo access. You won't be able to set up the systemd service."
  echo "Either obtain sudo permissions or use an alternative deployment method."
fi

# 3. Check for MySQL client
echo "Checking MySQL client installation..."
if command -v mysql &> /dev/null; then
  echo "MySQL client found."
else
  echo "WARNING: MySQL client not found. You might need it to verify database connection."
fi

# 4. Check disk space
echo "Checking available disk space..."
available_space=$(df -h ~ | awk 'NR==2 {print $4}')
echo "Available space in home directory: $available_space"

# 5. Check for required directories
echo "Checking if required directories exist and are writable..."
mkdir -p ~/app ~/logs/autocoin

if [ -w ~/app ] && [ -w ~/logs/autocoin ]; then
  echo "Directories exist and are writable."
else
  echo "ERROR: Unable to create or write to required directories."
  echo "Please ensure you have permission to write to ~/app and ~/logs/autocoin"
fi

# 6. Check for memory
echo "Checking available memory..."
available_memory=$(free -m | awk 'NR==2 {print $7}')
echo "Available memory: ${available_memory}MB"

if [ "$available_memory" -lt 1024 ]; then
  echo "WARNING: Less than 1GB of available memory detected. The application may require more memory to run optimally."
fi

# 7. Check network port availability
echo "Checking if port 8080 is available..."
if command -v netstat &> /dev/null; then
  if netstat -tuln | grep -q ':8080 '; then
    echo "WARNING: Port 8080 is already in use. The application might not start correctly."
    echo "Consider changing the port in your application-prod.yml or stopping the service using port 8080."
  else
    echo "Port 8080 is available."
  fi
elif command -v ss &> /dev/null; then
  if ss -tuln | grep -q ':8080 '; then
    echo "WARNING: Port 8080 is already in use. The application might not start correctly."
  else
    echo "Port 8080 is available."
  fi
else
  echo "WARNING: Unable to check port availability. Please ensure port 8080 is available."
fi

echo "=== System check completed ==="
