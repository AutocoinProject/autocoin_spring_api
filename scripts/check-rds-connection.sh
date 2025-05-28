#!/bin/bash

# RDS Security Group Check Script
# This script helps verify RDS connectivity from EC2

echo "=== RDS Connectivity Check ==="

# 1. Check if we can resolve RDS endpoint
echo "1. DNS Resolution Test:"
RDS_ENDPOINT=$(grep "DB_URL" .env | cut -d'/' -f3 | cut -d':' -f1)
echo "RDS Endpoint: $RDS_ENDPOINT"
nslookup $RDS_ENDPOINT 2>&1 || echo "DNS resolution failed"

# 2. Check network connectivity
echo ""
echo "2. Network Connectivity Test:"
nc -zv $RDS_ENDPOINT 3306 2>&1 || echo "Cannot connect to port 3306"

# 3. Test with telnet
echo ""
echo "3. Telnet Test:"
timeout 5 telnet $RDS_ENDPOINT 3306 2>&1 || echo "Telnet connection failed"

# 4. MySQL connection test (if mysql client is installed)
echo ""
echo "4. MySQL Connection Test:"
if command -v mysql &> /dev/null; then
    DB_USER=$(grep "DB_USERNAME" .env | cut -d'=' -f2)
    DB_PASS=$(grep "DB_PASSWORD" .env | cut -d'=' -f2)
    mysql -h $RDS_ENDPOINT -u $DB_USER -p$DB_PASS -e "SELECT 1;" 2>&1 || echo "MySQL connection failed"
else
    echo "MySQL client not installed. Install with: sudo apt-get install mysql-client"
fi

# 5. Check security group rules
echo ""
echo "5. EC2 Instance Info:"
curl -s http://169.254.169.254/latest/meta-data/local-ipv4 2>/dev/null && echo " (EC2 Private IP)"
curl -s http://169.254.169.254/latest/meta-data/public-ipv4 2>/dev/null && echo " (EC2 Public IP)"

echo ""
echo "=== Check Complete ==="
echo ""
echo "If connection fails, check:"
echo "1. RDS security group allows inbound MySQL/Aurora (3306) from EC2 security group"
echo "2. RDS is in the same VPC or VPC peering is configured"
echo "3. RDS is publicly accessible (if connecting from outside VPC)"
echo "4. Database exists and credentials are correct"
