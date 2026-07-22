# ============================================================================
# Frontend React — Dockerfile
# Build context : frontend/
# Usage : docker build -t ehealth/frontend ./frontend
# ============================================================================

# ── Stage 1 : Build Vite ─────────────────────────────────────────────────────
FROM node:22-alpine AS builder
WORKDIR /app

# Copier les manifestes de dépendances
COPY package.json package-lock.json ./
RUN npm ci --silent

# Copier les sources
COPY . .

# Variables d'environnement injectées au build
# Elles peuvent être surchargées via --build-arg
ARG VITE_AUTH_ENABLED=true
ARG VITE_KEYCLOAK_URL=http://localhost:8180
ARG VITE_KEYCLOAK_REALM=ehealth
ARG VITE_KEYCLOAK_CLIENT_ID=ehealth-frontend
ARG VITE_GAP_API_URL=/api/v1/gap
ARG VITE_DPI_API_URL=/api/v1/dpi
ARG VITE_EMPI_API_URL=/api/v1/empi

ENV VITE_AUTH_ENABLED=$VITE_AUTH_ENABLED
ENV VITE_KEYCLOAK_URL=$VITE_KEYCLOAK_URL
ENV VITE_KEYCLOAK_REALM=$VITE_KEYCLOAK_REALM
ENV VITE_KEYCLOAK_CLIENT_ID=$VITE_KEYCLOAK_CLIENT_ID
ENV VITE_GAP_API_URL=$VITE_GAP_API_URL
ENV VITE_DPI_API_URL=$VITE_DPI_API_URL
ENV VITE_EMPI_API_URL=$VITE_EMPI_API_URL

RUN npm run build

# ── Stage 2 : Nginx ──────────────────────────────────────────────────────────
FROM nginx:1.27-alpine

# Copier la config Nginx personnalisée
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Copier le build Vite
COPY --from=builder /app/dist /usr/share/nginx/html

# Healthcheck
HEALTHCHECK --interval=15s --timeout=3s --start-period=10s --retries=3 \
  CMD wget -qO- http://localhost:80/health || exit 1

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
