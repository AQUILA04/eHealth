# Deploy Contabo — eHealth

Default path: **Docker Compose + shared-traefik + optimize-common-infra** (pas K8s).

## Layout serveur

```
/opt/ehealth/
  init.sh
  deploy/          # sync depuis GitHub
  prod/
    .env
    releases/

/opt/optimizesolux/common-infra/   # Keycloak, Redis, Artemis, …
```

## Scripts

| Script | Rôle |
|--------|------|
| `init.sh` | Bootstrap CD : sync + setup 1re fois + `deploy.sh` |
| `setup-server.sh` | Networks `traefik-public` + `optimizesolux-common`, `.env` |
| `deploy.sh` | Pull images GHCR + `compose up` + smoke HTTP |
| `update-deploy.sh` | Clone `deploy/` depuis GitHub (swap atomique) |

## Prod vs local

| | Contabo (`deploy/docker-compose.prod.yml`) | Laptop (`infrastructure/docker/docker-compose.dev.yml`) |
|--|---------------------------------------------|-------------------------------|
| Conteneurs | Postgres + EMPI/GAP/DPI/Tenant + FE | Keycloak + Maildev + H2 services + FE |
| Auth | `auth.optimizesolux.com/realms/ehealth` | `localhost:8180` |
| Réseaux | `optimizesolux-common` + `traefik-public` | bridge local |

## Prerequisite Keycloak

Realm `ehealth` must exist in common-infra (`images/keycloak/realms/ehealth-realm.json`).
After adding/updating:

```bash
sudo /opt/optimizesolux/common-infra/install.sh --force-update keycloak
```

See optimize-common-infra `docs/CONSUMER-GUIDE.md`.

## Manual promote (SSH)

Préférer le CD GitHub ; en secours :

```bash
sudo /opt/ehealth/init.sh prod \
  ghcr.io/<org>/ehealth-frontend:<sha> \
  ghcr.io/<org>/ehealth-empi:<sha> \
  ghcr.io/<org>/ehealth-gap:<sha> \
  ghcr.io/<org>/ehealth-dpi:<sha> \
  ghcr.io/<org>/ehealth-tenant:<sha> \
  --ghcr-username ... --ghcr-token ... \
  --keycloak-admin-password ...
```

Voir [GITHUB-SECRETS-CONTABO.md](./GITHUB-SECRETS-CONTABO.md).
