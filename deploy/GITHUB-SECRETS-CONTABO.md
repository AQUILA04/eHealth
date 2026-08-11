# Secrets GitHub + DNS — eHealth (OptimizeSolux Contabo)

Prérequis VPS : **shared-traefik** + **optimize-common-infra** (réseau `optimizesolux-common`,
Keycloak `auth.optimizesolux.com`, realm `ehealth`).

## 1. DNS Cloudflare

| Type | Name | Content | Proxy |
|------|------|---------|-------|
| A | `ehealth` | `169.58.127.90` | DNS only (ou orange + SSL Full si DNS-01 Traefik) |

Auth partagée : `auth.optimizesolux.com` (common-infra) — **pas** de `ehealth-auth`.

## 2. Secrets repo (Actions) + environment `prod`

Réutilise la même clé SSH que SharedTraefik / CleanTrack / Notification Hub si possible.

| Secret | Valeur |
|--------|--------|
| `SSH_PRIVATE_KEY` | contenu de `~\.ssh\optimizesolux_vps_ed25519` |
| `PROD_SERVER_HOST` | `169.58.127.90` |
| `PROD_SERVER_USER` | `root` |
| `GHCR_USERNAME` | user GitHub |
| `GHCR_TOKEN` | PAT `read:packages` (+ `write:packages` pour CI) |
| `DB_USER` | ex. `ehealth` |
| `PROD_DB_PASSWORD` | mot de passe fort (Postgres métier) |
| `PROD_DB_NAME` | `ehealth` |
| `PROD_APP_HOSTNAME` | `ehealth.optimizesolux.com` |
| `PROD_KEYCLOAK_ADMIN_USERNAME` | admin common-infra |
| `PROD_KEYCLOAK_ADMIN_PASSWORD` | **même** valeur que common-infra Keycloak admin |

Créer aussi l’**environment** GitHub Actions nommé `prod` (approvals optionnels).

## 3. Hosts runtime

| URL | Rôle |
|-----|------|
| https://ehealth.optimizesolux.com | Frontend React (proxy `/api/v1/*`) |
| https://auth.optimizesolux.com/realms/ehealth | Keycloak (common-infra) |

## 4. Pipelines

| Workflow | Trigger |
|----------|---------|
| **CI** | push / PR sur `main` et `release/**` → tests + push images GHCR |
| **CD** | CI success sur `release/**` **ou** `workflow_dispatch` (promote) → SSH Contabo |

Promote : déploie les dernières images publiées depuis l’historique `main`.

## 5. Source de vérité

- Runtime Docker (compose, scripts) = `deploy/` du dépôt
- À chaque `init.sh`, `/opt/ehealth/deploy/` est resynchronisé depuis GitHub
- Secrets hors git : `/opt/ehealth/prod/.env`
- Template : `deploy/.env.prod.example`
- Outils partagés : `/opt/optimizesolux/common-infra/`
- Redis DB index réservé : `6` (`ehealth:`)
