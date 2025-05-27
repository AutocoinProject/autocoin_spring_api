#!/bin/bash
set -e

echo "=== AutoCoin Deployment with Automatic .env Update ==="
echo "Starting deployment at $(date)"

# Function to decrypt .env file
decrypt_env_file() {
    echo "Decrypting .env file..."
    if [ -f ".env.secret" ]; then
        git secret reveal -f
        if [ ! -f ".env" ]; then
            echo "ERROR: Failed to decrypt .env file"
            exit 1
        fi
        echo "Successfully decrypted .env file"
    else
        echo "WARNING: No encrypted .env file found!"
        if [ ! -f ".env" ]; then
            echo "ERROR: No .env file available. Cannot proceed."
            exit 1
        fi
    fi
}

# Function to prepare deployment package
prepare_deployment() {
    echo "Preparing deployment package..."
    
    # Create temp directory
    TEMP_DIR=$(mktemp -d)
    echo "Using temporary directory: $TEMP_DIR"
    
    # Copy JAR file
    if [ -f "build/libs/*.jar" ]; then
        cp build/libs/*.jar "$TEMP_DIR/"
    else
        echo "No JAR file found. Please build the project first."
        exit 1
    fi
    
    # Copy .env file
    cp .env "$TEMP_DIR/"
    
    # Copy deployment scripts
    cp scripts/deploy.sh "$TEMP_DIR/"
    
    echo "Deployment package prepared"
    echo "$TEMP_DIR"
}

# Function to deploy to server
deploy_to_server() {
    local SERVER=$1
    local TEMP_DIR=$2
    
    echo "Deploying to $SERVER..."
    
    # Copy files to server
    scp -r "$TEMP_DIR"/* "$SERVER":~/app/
    
    # Execute remote deployment script
    ssh "$SERVER" "cd ~/app && bash deploy.sh"
    
    echo "Deployment to $SERVER completed"
}

# Function to update environment variables on server
update_env_on_server() {
    local SERVER=$1
    
    echo "Updating environment variables on $SERVER..."
    
    # Copy .env file to server
    scp .env "$SERVER":~/app/.env
    
    # Restart service
    ssh "$SERVER" "sudo systemctl restart autocoin"
    
    echo "Environment variables updated on $SERVER"
}

# Main deployment process
main() {
    # Check if we're in the project root
    if [ ! -d ".git" ]; then
        echo "Please run this script from the project root directory"
        exit 1
    fi
    
    # Step 1: Decrypt environment file
    decrypt_env_file
    
    # Step 2: Update environment variables only or full deployment
    if [ "$1" == "--env-only" ]; then
        # Update environment variables only
        echo "Performing environment variables update only..."
        
        # Loop through servers (can be configured in a server list file)
        SERVERS=("user@your-ec2-server-1" "user@your-ec2-server-2")
        
        for SERVER in "${SERVERS[@]}"; do
            update_env_on_server "$SERVER"
        done
    else
        # Full deployment
        echo "Performing full deployment..."
        
        # Prepare deployment package
        TEMP_DIR=$(prepare_deployment)
        
        # Loop through servers
        SERVERS=("user@your-ec2-server-1" "user@your-ec2-server-2")
        
        for SERVER in "${SERVERS[@]}"; do
            deploy_to_server "$SERVER" "$TEMP_DIR"
        done
        
        # Clean up
        rm -rf "$TEMP_DIR"
    fi
    
    echo "Deployment completed at $(date)"
}

# Run the main function with all arguments
main "$@"
