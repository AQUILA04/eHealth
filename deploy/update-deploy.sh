#!/usr/bin/env bash
set -euo pipefail
# =============================================================================
# update-deploy.sh — Sync atomique de deploy/ depuis GitHub
# =============================================================================
# EHEALTH_GITHUB_REPO=owner/repo (défaut: AQUILA04/eHealth)
# =============================================================================

REPO="${EHEALTH_GITHUB_REPO:-AQUILA04/eHealth}"
ROOT="/opt/ehealth"

echo ">>> [update-deploy] Fetching latest deploy scripts from GitHub ($REPO)..."
rm -rf /tmp/ehealth_src
git clone --depth 1 "https://github.com/${REPO}.git" /tmp/ehealth_src > /dev/null 2>&1

echo ">>> [update-deploy] Applying new scripts..."
rm -rf "$ROOT/deploy.new"
cp -r /tmp/ehealth_src/deploy "$ROOT/deploy.new"
rm -rf /tmp/ehealth_src

chmod +x "$ROOT/deploy.new"/*.sh 2>/dev/null || true

BACKUP_DIR="$ROOT/deploy.old_$(date +%s)"
if [[ -d "$ROOT/deploy" ]]; then
  mv "$ROOT/deploy" "$BACKUP_DIR"
  echo ">>> [update-deploy] Old scripts backed up in $BACKUP_DIR"
fi
mv "$ROOT/deploy.new" "$ROOT/deploy"

echo ">>> [update-deploy] Update complete!"
