#!/bin/bash
set -e

echo "=== Setting up git-secret for autocoin project ==="

# Check if git-secret is installed
if ! command -v git-secret &> /dev/null; then
    echo "git-secret not found. Installing..."
    if [[ "$OSTYPE" == "darwin"* ]]; then
        # macOS
        brew install git-secret
    elif [[ "$OSTYPE" == "linux-gnu"* ]]; then
        # Debian/Ubuntu
        echo "deb https://gitsecret.jfrog.io/artifactory/git-secret-deb git-secret main" | sudo tee /etc/apt/sources.list.d/git-secret.list
        wget -qO - https://gitsecret.jfrog.io/artifactory/api/gpg/key/public | sudo apt-key add -
        sudo apt-get update
        sudo apt-get install -y git-secret
    else
        echo "Automatic installation not supported for your OS."
        echo "Please install git-secret manually: https://git-secret.io/installation"
        exit 1
    fi
fi

# Navigate to project root
cd "$(git rev-parse --show-toplevel)"

# Initialize git-secret
if [ ! -d ".gitsecret" ]; then
    echo "Initializing git-secret..."
    git secret init
    echo ".env" >> .gitignore
    echo ".gitsecret/keys/random_seed" >> .gitignore
    git add .gitignore
    git commit -m "Add .env to .gitignore"
else
    echo "git-secret already initialized"
fi

# Check if GPG key exists or create one
GPG_KEY_EMAIL="deploy@autocoin.com"
if ! gpg --list-keys "$GPG_KEY_EMAIL" &> /dev/null; then
    echo "Creating GPG key for $GPG_KEY_EMAIL..."
    # Create batch file for automated key generation
    cat > /tmp/gpg-key-gen.batch << EOF
%echo Generating a basic OpenPGP key
Key-Type: RSA
Key-Length: 2048
Subkey-Type: RSA
Subkey-Length: 2048
Name-Real: AutoCoin Deploy
Name-Email: $GPG_KEY_EMAIL
Expire-Date: 0
Passphrase: autocoin-deploy-passphrase
%commit
%echo Key generation completed
EOF
    gpg --batch --gen-key /tmp/gpg-key-gen.batch
    rm /tmp/gpg-key-gen.batch
fi

# Add user to git-secret
echo "Adding deploy user to git-secret..."
git secret tell "$GPG_KEY_EMAIL"

# Export public key for sharing with team members
mkdir -p ./scripts/git-secret/keys
gpg --export --armor "$GPG_KEY_EMAIL" > ./scripts/git-secret/keys/deploy-public-key.asc
echo "Exported public key to ./scripts/git-secret/keys/deploy-public-key.asc"

# Export private key (protected by passphrase) for CI/CD
gpg --export-secret-keys --armor "$GPG_KEY_EMAIL" > ./scripts/git-secret/keys/deploy-private-key.asc
echo "Exported private key to ./scripts/git-secret/keys/deploy-private-key.asc"
echo "IMPORTANT: Keep this private key secure and use it only for deployment!"

echo "=== git-secret setup completed ==="
echo "Next steps:"
echo "1. Share the public key (./scripts/git-secret/keys/deploy-public-key.asc) with team members"
echo "2. Store the private key securely or in your CI/CD secrets"
echo "3. Run 'git secret add .env' to add your .env file"
echo "4. Run 'git secret hide' to encrypt the .env file"
echo "5. Commit the .env.secret file to your repository"
