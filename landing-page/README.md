# eHealth product landing page

Static marketing / self-serve entry for **https://ehealth.optimizesolux.com**.

> **Product model**  
> Self-serve like CleanTrack (sign in / start free). The homepage sells the **clinical product and its impact** — not SaaS jargon. Plan limits live in signup/billing, not in the marketing narrative.

## Local preview

```bash
cd landing-page
npx --yes serve .
```

- Accueil : `/`
- Inscription : `/signup.html`

## Deploy (Contabo + shared Traefik)

### Important — host conflict with the app

Production frontend is already routed on `ehealth.optimizesolux.com` (`deploy/docker-compose.prod.yml`).  
Do **not** deploy this static stack on the same Host label once the React landing is live — prefer the in-app landing (CleanTrack pattern).

If you still need a static mirror, set `LANDING_HOST` to a distinct host (e.g. marketing-only) via GitHub secrets.

### GitHub Actions

Workflow : [`.github/workflows/deploy-landing-page.yml`](../.github/workflows/deploy-landing-page.yml)

Secrets : `VPS_HOST`, `VPS_USER`, `SSH_PRIVATE_KEY` (+ optional `VPS_LANDING_PATH`, `LANDING_HOST`, `LANDING_COMPOSE_PROJECT`).

## Auth CTAs

- **Créer un compte** → `/signup.html` (plan Free par défaut)
- **Se connecter** → `{APP_ORIGIN}/login` (defaults to same origin; override with `window.EHEALTH_APP_ORIGIN` or `<meta name="ehealth-app-origin">`)

Contacts footer : `ehealth@optimizesolux.com`.  
Email routing : see [docs/EMAIL-ROUTING.md](docs/EMAIL-ROUTING.md).
