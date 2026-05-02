#!/bin/bash
set -e

# Navigate to project root (3 levels up from admin/scripts/Bash)
cd "$(dirname "$0")/../../.."

# Configuration
SERVER="tmb@trust"
APP_DIR="/home/tmb/mtz-cms"
NGINX_CONFIG="/etc/nginx/sites-available/mtzcg.com"

echo "🚀 Deploying mtz-cms to trust server..."
echo "📍 Project root: $(pwd)"

# Step 1: Create app directory on server if it doesn't exist
echo "📁 Creating application directory..."
ssh $SERVER "mkdir -p $APP_DIR"

# Step 2: Copy files to server
echo "📦 Copying files to server..."
rsync -avz --exclude='.git' \
           --exclude='.cpcache' \
           --exclude='target' \
           --exclude='.DS_Store' \
           --exclude='.nrepl-port' \
           --exclude='.clj-kondo' \
           ./ $SERVER:$APP_DIR/

# Step 3: Update nginx configuration
echo "🔧 Updating nginx configuration..."
scp nginx-mtzcg.com.conf $SERVER:/tmp/mtzcg.com.conf
ssh $SERVER "echo 'tmb' | sudo -S cp /tmp/mtzcg.com.conf $NGINX_CONFIG"
ssh $SERVER "echo 'tmb' | sudo -S ln -sf $NGINX_CONFIG /etc/nginx/sites-enabled/mtzcg.com"
ssh $SERVER "echo 'tmb' | sudo -S nginx -t && echo 'tmb' | sudo -S systemctl reload nginx"

# Step 4: Build and run docker container
echo "🐳 Building and starting Docker container..."
ssh $SERVER "cd $APP_DIR && docker compose down || true"
ssh $SERVER "cd $APP_DIR && docker compose build"
ssh $SERVER "cd $APP_DIR && docker compose up -d"

# Step 5: Check container status
echo "✅ Checking container status..."
ssh $SERVER "docker ps | grep mtz-cms"

echo "🎉 Deployment complete! Visit http://mtzcg.com"