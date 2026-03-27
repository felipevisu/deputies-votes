#!/usr/bin/env bash
set -euo pipefail

# De Olho Neles — server setup & deploy script
# Run on a fresh Hetzner CAX11 (Ubuntu 24.04 ARM)
#
# Usage:
#   1. First time:  ssh root@<IP> 'bash -s' < deploy.sh
#   2. Updates:     ssh root@<IP> 'cd /opt/deolhoneles && git pull && docker compose -f docker-compose.prod.yml up -d --build'

APP_DIR="/opt/deolhoneles"
REPO="https://github.com/YOUR_USER/de-olho-neles.git"  # <-- update this

echo "==> Installing Docker..."
if ! command -v docker &>/dev/null; then
  curl -fsSL https://get.docker.com | sh
fi

echo "==> Cloning repository..."
if [ ! -d "$APP_DIR" ]; then
  git clone "$REPO" "$APP_DIR"
fi

cd "$APP_DIR"
git pull

echo "==> Starting services..."
docker compose -f docker-compose.prod.yml up -d --build

echo ""
echo "==> Done! App is running on http://$(curl -s ifconfig.me)"
