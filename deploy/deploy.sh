#!/usr/bin/env bash
set -euo pipefail
set +H

# Usage:
#   deploy.sh [--force-update | -fu] <env> [frontend] [empi] [gap] [dpi] [tenant]
#   env = prod

if [ "$#" -lt 1 ]; then
  echo "Usage: $0 [--force-update | -fu] <env> [frontend_image] [empi_image] [gap_image] [dpi_image] [tenant_image]" >&2
  exit 2
fi

if [[ "$1" == "--force-update" || "$1" == "-fu" ]]; then
  echo ">>> [deploy] Force update requested. Updating deploy scripts from GitHub..."
  REPO="${EHEALTH_GITHUB_REPO:-AQUILA04/eHealth}"
  curl -sSL "https://raw.githubusercontent.com/${REPO}/main/deploy/update-deploy.sh" | bash
  shift
  if [ "$#" -lt 1 ]; then
    echo "Error: Missing environment argument after --force-update." >&2
    exit 2
  fi
  echo ">>> [deploy] Re-executing updated deploy.sh..."
  exec /opt/ehealth/deploy/deploy.sh "$@"
fi

ENV="$1"
FRONTEND_ARG="${2:-}"
EMPI_ARG="${3:-}"
GAP_ARG="${4:-}"
DPI_ARG="${5:-}"
TENANT_ARG="${6:-}"

ROOT_DIR="$(cd "$(dirname "$0")" && pwd)"
COMPOSE_FILE="$ROOT_DIR/docker-compose.$ENV.yml"
STACK_DIR="/opt/ehealth/$ENV"
ENV_FILE="$STACK_DIR/.env"
RELEASES_DIR="$STACK_DIR/releases"
PROJECT_NAME="ehealth-$ENV"
mkdir -p "$RELEASES_DIR"

env_quote() {
  # Docker Compose .env + bash `source`: double-quote special chars (incl. !).
  # Single quotes were kept as part of the value by some Compose parsers.
  local val="$1"
  if [[ "$val" == *[$'\n\r']* ]]; then
    echo "Error: newline in env value for key" >&2
    return 1
  fi
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

safe_source_env() {
  local file="$1"
  [[ -f "$file" ]] || return 0
  set +u
  set -a
  # shellcheck disable=SC1090
  source "$file"
  set +a
  set -u
}

set_env_var() {
  local key="$1"
  local val="$2"
  local file="$ENV_FILE"
  local stored line found=false
  stored="$(env_quote "$val")"
  local tmp
  tmp=$(mktemp)

  if [[ -f "$file" ]]; then
    while IFS= read -r line || [[ -n "$line" ]]; do
      if [[ "$line" == "${key}="* ]] && [[ "$found" == false ]]; then
        printf '%s=%s\n' "$key" "$stored"
        found=true
      else
        printf '%s\n' "$line"
      fi
    done < "$file" > "$tmp"
  fi

  if [[ "$found" == false ]]; then
    printf '%s=%s\n' "$key" "$stored" >> "$tmp"
  fi

  cat "$tmp" > "$file"
  rm -f "$tmp"
}

set_env_var_if_missing() {
  local key="$1"
  local val="$2"
  if ! grep -q -E "^${key}=" "$ENV_FILE" 2>/dev/null; then
    set_env_var "$key" "$val"
    echo "  + added missing $key"
  elif grep -q -E "^${key}=$" "$ENV_FILE" 2>/dev/null && [[ -n "$val" ]]; then
    set_env_var "$key" "$val"
    echo "  + filled empty $key"
  fi
}

if [[ ! -f "$COMPOSE_FILE" ]]; then
  echo "Error: compose file not found: $COMPOSE_FILE" >&2
  exit 1
fi

if [[ -f "$ENV_FILE" ]]; then
  safe_source_env "$ENV_FILE"
else
  echo "Warning: $ENV_FILE not found. Run setup-server.sh or init.sh first." >&2
fi

if [[ -n "$FRONTEND_ARG" ]]; then FRONTEND_IMAGE="$FRONTEND_ARG"; else FRONTEND_IMAGE="${FRONTEND_IMAGE:-}"; fi
if [[ -n "$EMPI_ARG" ]]; then EMPI_IMAGE="$EMPI_ARG"; else EMPI_IMAGE="${EMPI_IMAGE:-}"; fi
if [[ -n "$GAP_ARG" ]]; then GAP_IMAGE="$GAP_ARG"; else GAP_IMAGE="${GAP_IMAGE:-}"; fi
if [[ -n "$DPI_ARG" ]]; then DPI_IMAGE="$DPI_ARG"; else DPI_IMAGE="${DPI_IMAGE:-}"; fi
if [[ -n "$TENANT_ARG" ]]; then TENANT_IMAGE="$TENANT_ARG"; else TENANT_IMAGE="${TENANT_IMAGE:-}"; fi

if [[ -z "$FRONTEND_IMAGE" || -z "$EMPI_IMAGE" || -z "$GAP_IMAGE" || -z "$DPI_IMAGE" || -z "$TENANT_IMAGE" ]]; then
  echo "Error: FRONTEND_IMAGE, EMPI_IMAGE, GAP_IMAGE, DPI_IMAGE, TENANT_IMAGE must be provided as args or in $ENV_FILE" >&2
  exit 1
fi

touch "$ENV_FILE"
chmod 600 "$ENV_FILE" || true

[[ -n "$FRONTEND_ARG" ]] && set_env_var "FRONTEND_IMAGE" "$FRONTEND_IMAGE"
[[ -n "$EMPI_ARG" ]] && set_env_var "EMPI_IMAGE" "$EMPI_IMAGE"
[[ -n "$GAP_ARG" ]] && set_env_var "GAP_IMAGE" "$GAP_IMAGE"
[[ -n "$DPI_ARG" ]] && set_env_var "DPI_IMAGE" "$DPI_IMAGE"
[[ -n "$TENANT_ARG" ]] && set_env_var "TENANT_IMAGE" "$TENANT_IMAGE"

echo "Ensuring $ENV_FILE has required keys..."
if [[ "$ENV" == "prod" ]]; then
  set_env_var_if_missing APP_HOSTNAME "ehealth.optimizesolux.com"
  set_env_var_if_missing OIDC_ISSUER_URI "https://auth.optimizesolux.com/realms/ehealth"
  set_env_var_if_missing OIDC_JWK_URI "https://auth.optimizesolux.com/realms/ehealth/protocol/openid-connect/certs"
  set_env_var_if_missing KEYCLOAK_SERVER_URL "http://keycloak:8080"
  set_env_var_if_missing KEYCLOAK_REALM "ehealth"
  set_env_var_if_missing KEYCLOAK_ADMIN_CLIENT_ID "admin-cli"
  set_env_var_if_missing REDIS_DATABASE "6"
  set_env_var_if_missing OTEL_EXPORTER_OTLP_ENDPOINT "http://otel-collector:4318"
fi

if [[ "${CT_UPDATE_ENV_SECRETS:-}" == "true" ]]; then
  echo "CT_UPDATE_ENV_SECRETS=true — applying secret overrides from init/CD..."
  [[ -n "${EH_APP_HOSTNAME_PROD:-}" ]] && set_env_var APP_HOSTNAME "$EH_APP_HOSTNAME_PROD"
  [[ -n "${EH_OIDC_ISSUER_URI:-}" ]] && set_env_var OIDC_ISSUER_URI "$EH_OIDC_ISSUER_URI"
  [[ -n "${EH_OIDC_JWK_URI:-}" ]] && set_env_var OIDC_JWK_URI "$EH_OIDC_JWK_URI"
  [[ -n "${EH_DB_USER:-}" ]] && set_env_var DB_USER "$EH_DB_USER"
  [[ -n "${EH_DB_PASSWORD:-}" ]] && set_env_var DB_PASSWORD "$EH_DB_PASSWORD"
  [[ -n "${EH_DB_NAME:-}" ]] && set_env_var DB_NAME "$EH_DB_NAME"
  [[ -n "${EH_KEYCLOAK_SERVER_URL:-}" ]] && set_env_var KEYCLOAK_SERVER_URL "$EH_KEYCLOAK_SERVER_URL"
  [[ -n "${EH_KEYCLOAK_ADMIN_USERNAME:-}" ]] && set_env_var KEYCLOAK_ADMIN_USERNAME "$EH_KEYCLOAK_ADMIN_USERNAME"
  [[ -n "${EH_KEYCLOAK_ADMIN_PASSWORD:-}" ]] && set_env_var KEYCLOAK_ADMIN_PASSWORD "$EH_KEYCLOAK_ADMIN_PASSWORD"
  [[ -n "${EH_REDIS_DATABASE:-}" ]] && set_env_var REDIS_DATABASE "$EH_REDIS_DATABASE"
fi

if [[ "$ENV" == "prod" ]]; then
  safe_source_env "$ENV_FILE"
  set_env_var_if_missing OIDC_ISSUER_URI "https://auth.optimizesolux.com/realms/ehealth"
fi

TIMESTAMP=$(date -u +"%Y%m%dT%H%M%SZ")
RELEASE_FILE="$RELEASES_DIR/${ENV}_${TIMESTAMP}.txt"

echo "DEPLOY: env=$ENV"
echo "Using compose file: $COMPOSE_FILE"
echo "Using env file:     $ENV_FILE"
echo "Saving release metadata to $RELEASE_FILE"
{
  echo "FRONTEND_IMAGE=$FRONTEND_IMAGE"
  echo "EMPI_IMAGE=$EMPI_IMAGE"
  echo "GAP_IMAGE=$GAP_IMAGE"
  echo "DPI_IMAGE=$DPI_IMAGE"
  echo "TENANT_IMAGE=$TENANT_IMAGE"
  echo "TIMESTAMP=$TIMESTAMP"
} > "$RELEASE_FILE"

echo "Pulling images..."
if [ -n "${GHCR_USERNAME:-}" ] && [ -n "${GHCR_TOKEN:-}" ]; then
  echo "Logging in to ghcr.io as $GHCR_USERNAME"
  echo "$GHCR_TOKEN" | docker login ghcr.io -u "$GHCR_USERNAME" --password-stdin
fi

docker compose \
  -f "$COMPOSE_FILE" \
  --project-name "$PROJECT_NAME" \
  --env-file "$ENV_FILE" \
  pull

echo "Starting services..."
# Spring Boot healthchecks use start_period=90s and are chained
# (postgres → empi → gap → dpi → frontend). Compose's default
# --wait-timeout is 60s, which marks gap unhealthy before it can boot.
set +e
docker compose \
  -f "$COMPOSE_FILE" \
  --project-name "$PROJECT_NAME" \
  --env-file "$ENV_FILE" \
  up -d --wait --wait-timeout 300
UP_RC=$?
set -e
if [[ "$UP_RC" -ne 0 ]]; then
  echo "ERROR: compose up failed (exit $UP_RC). Status and logs:" >&2
  docker compose \
    -f "$COMPOSE_FILE" \
    --project-name "$PROJECT_NAME" \
    --env-file "$ENV_FILE" \
    ps -a || true
  docker compose \
    -f "$COMPOSE_FILE" \
    --project-name "$PROJECT_NAME" \
    --env-file "$ENV_FILE" \
    logs --no-color --tail=200 || true
  exit "$UP_RC"
fi

ln -sfn "$RELEASE_FILE" "$RELEASES_DIR/${ENV}_current.txt"

if [[ "$ENV" == "prod" ]]; then
  safe_source_env "$ENV_FILE"
  APP_URL="https://${APP_HOSTNAME:-ehealth.optimizesolux.com}"
  AUTH_URL="${OIDC_ISSUER_URI:-https://auth.optimizesolux.com/realms/ehealth}"
  echo "HTTP smoke: app=$APP_URL auth=$AUTH_URL"
  sleep 8
  curl -sk -o /dev/null -w "app=%{http_code}\n" "$APP_URL/" || echo "WARN: app HTTP check failed"
  curl -sk -o /dev/null -w "health=%{http_code}\n" "$APP_URL/health" || true
  curl -sk -o /dev/null -w "auth=%{http_code}\n" "$AUTH_URL" || echo "WARN: auth HTTP check failed (common-infra Keycloak)"
fi

echo "Deployment finished."
cat "$RELEASE_FILE"
echo "Done"
