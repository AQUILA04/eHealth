#!/usr/bin/env bash
# =============================================================================
# init.sh — Bootstrap Contabo eHealth (shared-traefik + common-infra)
# =============================================================================
# Usage (CD via SSH):
#   ./init.sh prod <frontend> <empi> <gap> <dpi> <tenant> [options...]
# =============================================================================
set -euo pipefail
set +H

INIT_DIR="$(cd "$(dirname "$0")" && pwd)"
DEPLOY_DIR="/opt/ehealth/deploy"
GITHUB_REPO="${EHEALTH_GITHUB_REPO:-AQUILA04/eHealth}"
GITHUB_RAW="https://raw.githubusercontent.com/${GITHUB_REPO}/main/deploy"

ORIG_ARGS=("$@")

ENV=""
FRONTEND_IMAGE=""
EMPI_IMAGE=""
GAP_IMAGE=""
DPI_IMAGE=""
TENANT_IMAGE=""
FORCE_UPDATE=false

DB_USER=""
DB_PASSWORD=""
DB_NAME=""
APP_HOSTNAME_PROD=""
OIDC_ISSUER_URI=""
OIDC_JWK_URI=""
KEYCLOAK_SERVER_URL=""
KEYCLOAK_ADMIN_USERNAME=""
KEYCLOAK_ADMIN_PASSWORD=""
REDIS_DATABASE=""
GHCR_USERNAME=""
GHCR_TOKEN=""

if [[ "$#" -ge 1 && "$1" != --* && "$1" != -* ]]; then
  ENV="$1"; shift
fi
if [[ "$#" -ge 1 && "$1" != --* && "$1" != -* ]]; then
  FRONTEND_IMAGE="$1"; shift
fi
if [[ "$#" -ge 1 && "$1" != --* && "$1" != -* ]]; then
  EMPI_IMAGE="$1"; shift
fi
if [[ "$#" -ge 1 && "$1" != --* && "$1" != -* ]]; then
  GAP_IMAGE="$1"; shift
fi
if [[ "$#" -ge 1 && "$1" != --* && "$1" != -* ]]; then
  DPI_IMAGE="$1"; shift
fi
if [[ "$#" -ge 1 && "$1" != --* && "$1" != -* ]]; then
  TENANT_IMAGE="$1"; shift
fi

while [[ "$#" -gt 0 ]]; do
  case "$1" in
    --force-update|-fu) FORCE_UPDATE=true ;;
    --db-user)                    DB_USER="$2";                    shift ;;
    --db-password)                DB_PASSWORD="$2";                shift ;;
    --db-name)                    DB_NAME="$2";                    shift ;;
    --app-hostname-prod)          APP_HOSTNAME_PROD="$2";          shift ;;
    --oidc-issuer-uri)            OIDC_ISSUER_URI="$2";            shift ;;
    --oidc-jwk-uri)               OIDC_JWK_URI="$2";               shift ;;
    --keycloak-server-url)        KEYCLOAK_SERVER_URL="$2";        shift ;;
    --keycloak-admin-username)    KEYCLOAK_ADMIN_USERNAME="$2";    shift ;;
    --keycloak-admin-password)    KEYCLOAK_ADMIN_PASSWORD="$2";    shift ;;
    --redis-database)             REDIS_DATABASE="$2";             shift ;;
    --ghcr-username)              GHCR_USERNAME="$2";              shift ;;
    --ghcr-token)                 GHCR_TOKEN="$2";                 shift ;;
    --github-repo)
      GITHUB_REPO="$2"
      export EHEALTH_GITHUB_REPO="$2"
      GITHUB_RAW="https://raw.githubusercontent.com/${GITHUB_REPO}/main/deploy"
      shift
      ;;
    *) echo "Unknown parameter: $1" >&2; exit 1 ;;
  esac
  shift
done

if [[ -z "$ENV" || -z "$FRONTEND_IMAGE" || -z "$EMPI_IMAGE" || -z "$GAP_IMAGE" || -z "$DPI_IMAGE" || -z "$TENANT_IMAGE" ]]; then
  echo "Error: env and five images (frontend, empi, gap, dpi, tenant) are required." >&2
  echo "Usage: $0 <env> <frontend_image> <empi_image> <gap_image> <dpi_image> <tenant_image> [options...]" >&2
  exit 1
fi

if [[ "${EHEALTH_INIT_SYNCED:-}" != "1" ]]; then
  echo ">>> [init] Syncing /opt/ehealth/deploy from GitHub (repo is source of truth)..."
  mkdir -p /opt/ehealth
  export EHEALTH_GITHUB_REPO="$GITHUB_REPO"
  bash <(curl -sSL "$GITHUB_RAW/update-deploy.sh")

  curl -sSL "$GITHUB_RAW/init.sh" -o /opt/ehealth/init.sh
  chmod +x /opt/ehealth/init.sh

  export EHEALTH_INIT_SYNCED=1
  echo ">>> [init] Re-executing synced init.sh from $DEPLOY_DIR..."
  exec "$DEPLOY_DIR/init.sh" "${ORIG_ARGS[@]}"
fi

if [[ "$FORCE_UPDATE" == "true" ]]; then
  echo ">>> [init] --force-update acknowledged (deploy/ already synced)."
fi

if [[ ! -d "$DEPLOY_DIR" ]]; then
  echo "Error: $DEPLOY_DIR missing after sync." >&2
  exit 1
fi

SETUP_MARKER="/opt/ehealth/.server_initialized"

if [[ ! -f "$SETUP_MARKER" ]]; then
  echo ">>> [init] First-time setup detected. Running setup-server.sh..."

  export EH_DB_USER="${DB_USER:-ehealth}"
  export EH_DB_PASSWORD="${DB_PASSWORD:-}"
  export EH_DB_NAME="${DB_NAME:-ehealth}"
  export EH_APP_HOSTNAME_PROD="${APP_HOSTNAME_PROD:-ehealth.optimizesolux.com}"
  export EH_OIDC_ISSUER_URI="${OIDC_ISSUER_URI:-https://auth.optimizesolux.com/realms/ehealth}"
  export EH_OIDC_JWK_URI="${OIDC_JWK_URI:-https://auth.optimizesolux.com/realms/ehealth/protocol/openid-connect/certs}"
  export EH_KEYCLOAK_SERVER_URL="${KEYCLOAK_SERVER_URL:-http://keycloak:8080}"
  export EH_KEYCLOAK_ADMIN_USERNAME="${KEYCLOAK_ADMIN_USERNAME:-admin}"
  export EH_KEYCLOAK_ADMIN_PASSWORD="${KEYCLOAK_ADMIN_PASSWORD:-}"
  export EH_REDIS_DATABASE="${REDIS_DATABASE:-6}"

  bash "$DEPLOY_DIR/setup-server.sh"
  touch "$SETUP_MARKER"
  echo ">>> [init] Server setup complete. Marker written to $SETUP_MARKER"
else
  echo ">>> [init] Server already initialized (found $SETUP_MARKER). Skipping setup."
fi

echo ">>> [init] Launching deployment: env=$ENV"
export GHCR_USERNAME="${GHCR_USERNAME:-}"
export GHCR_TOKEN="${GHCR_TOKEN:-}"
export EHEALTH_GITHUB_REPO="$GITHUB_REPO"

export CT_UPDATE_ENV_SECRETS="${CT_UPDATE_ENV_SECRETS:-true}"
export EH_DB_USER="${DB_USER:-}"
export EH_DB_PASSWORD="${DB_PASSWORD:-}"
export EH_DB_NAME="${DB_NAME:-}"
export EH_APP_HOSTNAME_PROD="${APP_HOSTNAME_PROD:-}"
export EH_OIDC_ISSUER_URI="${OIDC_ISSUER_URI:-}"
export EH_OIDC_JWK_URI="${OIDC_JWK_URI:-}"
export EH_KEYCLOAK_SERVER_URL="${KEYCLOAK_SERVER_URL:-}"
export EH_KEYCLOAK_ADMIN_USERNAME="${KEYCLOAK_ADMIN_USERNAME:-}"
export EH_KEYCLOAK_ADMIN_PASSWORD="${KEYCLOAK_ADMIN_PASSWORD:-}"
export EH_REDIS_DATABASE="${REDIS_DATABASE:-}"

bash "$DEPLOY_DIR/deploy.sh" "$ENV" "$FRONTEND_IMAGE" "$EMPI_IMAGE" "$GAP_IMAGE" "$DPI_IMAGE" "$TENANT_IMAGE"

echo ">>> [init] Done."
