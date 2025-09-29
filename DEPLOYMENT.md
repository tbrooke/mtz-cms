# mtz-CMS Deployment Guide

## Overview
This app is deployed on the trust server at `mtzcg.com` using Docker and nginx.

## Prerequisites
- SSH access to `tmb@trust` with password `tmb`
- Docker and docker-compose installed on trust server
- nginx running on trust server
- trust-network Docker network available

## Deployment Methods

### Method 1: Direct Push (./deploy.sh)

Run the deployment script from your local machine:

```bash
./deploy.sh
```

This will:
1. Create/update the app directory on trust server
2. Copy all files via rsync
3. Update nginx configuration
4. Build and run the Docker container
5. Verify deployment

### Method 2: GitHub Actions (Automated)

Set up GitHub Actions for automatic deployment on push to main:

1. Add SSH private key to GitHub Secrets:
   - Go to repository Settings → Secrets and variables → Actions
   - Create secret named `SSH_PRIVATE_KEY`
   - Add your SSH private key for accessing trust server

2. Ensure the server hostname is reachable:
   - Either add `trust` to DNS
   - Or update `.github/workflows/deploy.yml` with the IP address

3. Push to main branch:
```bash
git push origin main
```

The workflow will automatically deploy the application.

## Architecture

### Docker Setup
- **Image**: Official Clojure image (temurin-21-tools-deps-jammy)
- **Network**: trust-network (connects to other services)
- **Port**: 3000 (internal), proxied by nginx
- **Environment**:
  - ALFRESCO_HOST=admin.mtzcg.com
  - ALFRESCO_PORT=80
  - ALFRESCO_USER=admin
  - ALFRESCO_PASSWORD=admin

### nginx Configuration
- **Domain**: mtzcg.com
- **Root**: Proxies to mtz-cms:3000
- **/mcp**: Proxies to localhost:8085 (existing MCP service)

### Alfresco Connection
The app connects to Alfresco at `admin.mtzcg.com` through nginx:
- Share UI: admin.mtzcg.com/share → localhost:8081
- Alfresco API: admin.mtzcg.com/alfresco → localhost:8080

## Manual Deployment Steps

If you need to deploy manually:

```bash
# 1. SSH into server
ssh tmb@trust

# 2. Create app directory
mkdir -p /home/tmb/mtz-cms

# 3. Copy files (from local machine)
rsync -avz ./ tmb@trust:/home/tmb/mtz-cms/

# 4. Update nginx config (on server)
sudo cp nginx-mtzcg.com.conf /etc/nginx/sites-available/mtzcg.com
sudo ln -sf /etc/nginx/sites-available/mtzcg.com /etc/nginx/sites-enabled/
sudo nginx -t
sudo systemctl reload nginx

# 5. Start application (on server)
cd /home/tmb/mtz-cms
docker compose build
docker compose up -d

# 6. Check status
docker ps | grep mtz-cms
docker logs mtz-cms
```

## Updating the Application

### Local Development Changes
After making changes locally:

```bash
# Option 1: Run deploy script
./deploy.sh

# Option 2: Push to GitHub (if using GitHub Actions)
git add .
git commit -m "Your changes"
git push origin main
```

### Hot Reload Without Rebuild
For quick updates without rebuilding the container:

```bash
# Copy changed files
rsync -avz ./src tmb@trust:/home/tmb/mtz-cms/

# Restart container
ssh tmb@trust "cd /home/tmb/mtz-cms && docker compose restart"
```

## Monitoring

### Check Application Status
```bash
ssh tmb@trust "docker ps | grep mtz-cms"
```

### View Logs
```bash
ssh tmb@trust "docker logs -f mtz-cms"
```

### Check nginx Status
```bash
ssh tmb@trust "echo 'tmb' | sudo -S nginx -t"
ssh tmb@trust "echo 'tmb' | sudo -S systemctl status nginx"
```

### Test Application
```bash
curl http://mtzcg.com
```

## Troubleshooting

### Container Won't Start
```bash
# Check logs
ssh tmb@trust "docker logs mtz-cms"

# Check if port 3000 is available
ssh tmb@trust "docker ps | grep 3000"

# Rebuild container
ssh tmb@trust "cd /home/tmb/mtz-cms && docker compose down && docker compose up --build -d"
```

### nginx Errors
```bash
# Test nginx config
ssh tmb@trust "echo 'tmb' | sudo -S nginx -t"

# Check nginx logs
ssh tmb@trust "echo 'tmb' | sudo -S tail -f /var/log/nginx/error.log"
```

### Can't Connect to Alfresco
```bash
# Check if Alfresco containers are running
ssh tmb@trust "docker ps | grep alfresco"

# Test Alfresco API from server
ssh tmb@trust "curl -u admin:admin http://admin.mtzcg.com/alfresco/api/-default-/public/alfresco/versions/1/nodes/-root-"
```

### Network Issues
```bash
# Verify trust-network exists
ssh tmb@trust "docker network ls | grep trust"

# Check if container is on network
ssh tmb@trust "docker network inspect trust-network"
```

## Files

- `Dockerfile` - Container definition
- `docker-compose.yml` - Docker Compose configuration
- `nginx-mtzcg.com.conf` - nginx configuration for mtzcg.com
- `deploy.sh` - Deployment script for direct push
- `.github/workflows/deploy.yml` - GitHub Actions workflow
- `.dockerignore` - Files to exclude from Docker build

## Security Notes

- SSH credentials are in plain text in scripts (trust is internal server)
- Alfresco credentials are in docker-compose.yml
- For production, use secrets management (Docker secrets, environment files)
- Consider using SSL/TLS certificates for https