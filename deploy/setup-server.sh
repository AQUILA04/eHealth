#!/usr/bin/env bash
# =============================================================================
# setup-server.sh — One-time Contabo setup for eHealth
# Prérequis: shared-traefik + optimize-common-infra (optimizesolux-common).
# =============================================================================
set -euo pipefail
set +H

DEPLOY_DIR="$(cd "$(dirname "$0")" && pwd)"
ROOT="/opt/ehealth"

echo "=== eHealth Server Setup ==="

echo "[1/5] Checking Docker installation..."
if ! command -v docker &>/dev/null; then
  echo "      Docker not found. Installing..."
  apt-get update -y
  apt-get install -y ca-certificates curl gnupg git
  install -m 0755 -d /etc/apt/keyrings
  curl -fsSL https://download.docker.com/linux/ubuntu/gpg | gpg --dearmor -o /etc/apt/keyrings/docker.gpg
  chmod a+r /etc/apt/keyrings/docker.gpg
  echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu $(. /etc/os-release && echo "$VERSION_CODENAME") stable" \
    | tee /etc/apt/sources.list.d/docker.list > /dev/null
  apt-get update -y
  apt-get install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
  echo "      Docker installed successfully."
else
  echo "      Docker already installed."
fi

echo "[2/5] Creating directory structure..."
mkdir -p "$ROOT/prod/releases"
mkdir -p "$ROOT/deploy"
if [[ "$DEPLOY_DIR" != "$ROOT/deploy" ]]; then
  cp -a "$DEPLOY_DIR"/. "$ROOT/deploy/"
  chmod +x "$ROOT/deploy"/*.sh 2>/dev/null || true
fi
echo "      Directories created."

echo "[3/5] Creating Docker networks..."
for net in traefik-public optimizesolux-common; do
  if docker network inspect "$net" > /dev/null 2>&1; then
    echo "      Network '$net' already exists, skipping."
  else
    docker network create "$net"
    echo "      Network '$net' created."
  fi
done

if [[ ! -d /opt/optimizesolux/common-infra ]]; then
  echo "      WARNING: /opt/optimizesolux/common-infra not found."
  echo "      Install optimize-common-infra before deploying this product."
else
  echo "      Tip: ensure common-infra is up: sudo /opt/optimizesolux/common-infra/install.sh"
fi

env_quote() {
  local val="$1"
  if [[ "$val" =~ ^[A-Za-z0-9._:/+-]+$ ]]; then
    printf '%s' "$val"
  else
    local escaped="${val//\\/\\\\}"
    escaped="${escaped//\"/\\\"}"
    escaped="${escaped//\$/\\$}"
    escaped="${escaped//\`/\\\`}"
    printf '"%s"' "$escaped"
  fi
}

echo "[4/5] Creating .env files..."

_db_user="${EH_DB_USER:-ehealth}"
_db_pass="${EH_DB_PASSWORD:-CHANGE_ME_prod_db_password}"
_db_name="${EH_DB_NAME:-ehealth}"
_app_host="${EH_APP_HOSTNAME_PROD:-ehealth.optimizesolux.com}"
_oidc="${EH_OIDC_ISSUER_URI:-https://auth.optimizesolux.com/realms/ehealth}"
_oidc_jwk="${EH_OIDC_JWK_URI:-https://auth.optimizesolux.com/realms/ehealth/protocol/openid-connect/certs}"
_kc_url="${EH_KEYCLOAK_SERVER_URL:-http://keycloak:8080}"
_kc_admin_user="${EH_KEYCLOAK_ADMIN_USERNAME:-admin}"
_kc_admin_pass="${EH_KEYCLOAK_ADMIN_PASSWORD:-CHANGE_ME_keycloak_admin}"
_redis_db="${EH_REDIS_DATABASE:-6}"

_db_pass_q="$(env_quote "$_db_pass")"
_oidc_q="$(env_quote "$_oidc")"
_oidc_jwk_q="$(env_quote "$_oidc_jwk")"
_kc_admin_pass_q="$(env_quote "$_kc_admin_pass")"

PROD_ENV="$ROOT/prod/.env"
if [[ ! -f "$PROD_ENV" ]]; then
  cat > "$PROD_ENV" << EOF
# =============================================================================
# eHealth PROD — $ROOT/prod/.env
# Shared tools: optimize-common-infra (keycloak on optimizesolux-common)
# =============================================================================
DB_USER=${_db_user}
DB_PASSWORD=${_db_pass_q}
DB_NAME=${_db_name}
DDL_AUTO=update

APP_HOSTNAME=${_app_host}
OIDC_ISSUER_URI=${_oidc_q}
OIDC_JWK_URI=${_oidc_jwk_q}

KEYCLOAK_SERVER_URL=${_kc_url}
KEYCLOAK_REALM=ehealth
KEYCLOAK_ADMIN_USERNAME=${_kc_admin_user}
KEYCLOAK_ADMIN_PASSWORD=${_kc_admin_pass_q}
KEYCLOAK_ADMIN_CLIENT_ID=admin-cli

REDIS_DATABASE=${_redis_db}

OTEL_EXPORTER_OTLP_ENDPOINT=http://otel-collector:4318

FRONTEND_IMAGE=
EMPI_IMAGE=
GAP_IMAGE=
DPI_IMAGE=
TENANT_IMAGE=
EOF
  chmod 600 "$PROD_ENV"
  echo "      Created $PROD_ENV"
else
  echo "      $PROD_ENV already exists, skipping."
fi

echo "[5/5] Checking Traefik reverse proxy..."
if docker ps --format '{{.Names}}' | grep -qx 'shared-traefik'; then
  echo "      Shared Traefik (shared-traefik) already running — skipping local Traefik."
elif ss -tlnp 2>/dev/null | grep -q ':80 '; then
  echo "      Port 80 already in use — skipping local Traefik bootstrap."
else
  echo "      WARNING: shared-traefik not detected and :80 free."
  echo "      Install/start OptimizeSolux shared-traefik before exposing HTTPS."
fi

echo ""
echo "=== Setup complete! ==="
echo "DNS (grey cloud or orange+Full) → this server:"
echo "  ${_app_host}"
echo "Auth (shared): ${_oidc}"
echo "Verify secrets in $PROD_ENV (KEYCLOAK_ADMIN_* must match common-infra)."
echo "Then deploy via init.sh / CD."
