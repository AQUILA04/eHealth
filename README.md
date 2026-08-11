# eHealth - Système d'Information Hospitalier Intégré

[![CI/CD Pipeline](https://github.com/AQUILA04/eHealth/actions/workflows/ci.yml/badge.svg)](https://github.com/AQUILA04/eHealth/actions/workflows/ci.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

**Architecture Hub-and-Spoke pour un système hospitalier intégré moderne**

Le projet eHealth est un système d'information hospitalier (SIH) complet conçu selon une architecture distribuée Hub-and-Spoke. Il combine la gestion hospitalière locale autonome avec une interopérabilité centralisée pour garantir l'échange sécurisé de données cliniques entre établissements.

---

## 📋 Table des matières

- [Vue d'ensemble](#vue-densemble)
- [Architecture](#architecture)
- [Technologies](#technologies)
- [Prérequis](#prérequis)
- [Installation](#installation)
- [Développement](#développement)
- [Tests](#tests)
- [Déploiement](#déploiement)
- [Documentation](#documentation)
- [Contribution](#contribution)
- [Licence](#licence)

---

## 🎯 Vue d'ensemble

Le système eHealth est organisé en **monorepo polyglotte** qui combine les forces de Java/Spring Boot pour les services critiques et Node.js/NestJS pour les services légers, offrant ainsi une solution optimale pour chaque cas d'usage.

### Fonctionnalités principales

**Hub Central (Interopérabilité & Identité)**

- **EMPI (Enterprise Master Patient Index)** - Gestion unique de l'identité patient avec support MOSIP
- **HIE Router** - Routage des échanges d'information de santé conformes FHIR R4/R5
- **Consent Service** - Gestion des consentements patients pour le partage de données
- **Terminology Service** - Service de terminologie standardisée (SNOMED CT, LOINC, ICD-10)

**Nœuds Locaux (Clinique/Hôpital)**

- **GAP (Gestion Administrative du Patient)** - Admission, transfert, sortie (ADT), gestion des lits
- **DPI (Dossier Patient Informatisé)** - Dossier clinique électronique avec support FHIR
- **CPOE** - Prescription informatisée avec support à la décision clinique
- **LIS** - Système d'information de laboratoire
- **RIS** - Système d'information de radiologie
- **Pharmacy** - Gestion de la pharmacie hospitalière

**Services Transversaux**

- **API Gateway** - Point d'entrée unique avec authentification et routage
- **Workflow Engine** - Orchestration des processus métiers
- **Notification Service** - Notifications multi-canaux (SMS, Email, Push)
- **Audit Service** - Traçabilité complète des accès et modifications

---

## 🏗️ Architecture

Le projet suit une architecture **Hub-and-Spoke** avec une approche **microservices polyglotte**:

```
┌─────────────────────────────────────────────────────────┐
│                    Frontend (React)                      │
└─────────────────────────────────────────────────────────┘
                          ↓ REST/HTTP
┌─────────────────────────────────────────────────────────┐
│              API Gateway (Node.js/NestJS)               │
└─────────────────────────────────────────────────────────┘
         ↓ gRPC                              ↓ gRPC
┌──────────────────────┐          ┌──────────────────────┐
│  Services Java       │          │  Services Node.js    │
│  (EMPI, DPI, GAP)    │←─gRPC───→│  (Workflow, Audit)   │
└──────────────────────┘          └──────────────────────┘
         ↓ Events (RabbitMQ/Kafka)
┌─────────────────────────────────────────────────────────┐
│              Message Broker (Event Bus)                 │
└─────────────────────────────────────────────────────────┘
```

### Choix technologiques stratégiques

| Composant              | Technologie                | Justification                                                   |
| ---------------------- | -------------------------- | --------------------------------------------------------------- |
| **Services critiques** | Java 17 + Spring Boot 3.1  | Robustesse transactionnelle, HAPI FHIR, écosystème santé mature |
| **Services légers**    | Node.js 20 + NestJS 10     | Performance I/O, architecture modulaire, gRPC natif             |
| **Frontend**           | React 18 + Vite 5          | Composants réutilisables, performance, écosystème riche         |
| **Bases de données**   | PostgreSQL 16 + MongoDB 7  | ACID pour données structurées, flexibilité pour DPI             |
| **Messaging**          | RabbitMQ 3.12              | Fiabilité, event-driven architecture                            |
| **gRPC**               | Protocol Buffers 3         | Performance, contrats stricts inter-services                    |
| **Interopérabilité**   | FHIR R4/R5 (HAPI FHIR 6.8) | Standard mondial pour l'échange de données de santé             |

Pour plus de détails, consultez [MONOREPO_ARCHITECTURE.md](./MONOREPO_ARCHITECTURE.md).

---

## 🛠️ Technologies

### Backend Java

- **Framework:** Spring Boot 3.1.5, Spring Cloud 2022.0.4
- **FHIR:** HAPI FHIR 6.8.0 (R4/R5)
- **Communication:** gRPC 1.59.0, Protobuf 3.24.0
- **Base de données:** PostgreSQL 16, MongoDB 7
- **Build:** Maven 3.9+
- **Tests:** JUnit 5.9.3

### Backend Node.js

- **Framework:** NestJS 10.2.1
- **Runtime:** Node.js 20.x LTS
- **Communication:** gRPC, REST
- **Validation:** class-validator, class-transformer
- **Build:** pnpm 8.x
- **Tests:** Jest 29.7.0

### Frontend

- **Framework:** React 18.2.0
- **Build:** Vite 5.0.0
- **State Management:** Zustand 4.4.1
- **Data Fetching:** TanStack Query 5.8.0
- **Styling:** Tailwind CSS 3.3.0
- **Tests:** Vitest 0.34.0

### Infrastructure

- **Conteneurisation:** Docker 24.x
- **Orchestration:** Kubernetes 1.28.x
- **CI/CD:** GitHub Actions
- **Monitoring:** Prometheus + Grafana
- **Logging:** ELK Stack (Elasticsearch, Logstash, Kibana)

---

## 📦 Prérequis

Avant de commencer, assurez-vous d'avoir installé:

### Obligatoire

- **Java 17+** (OpenJDK ou Oracle JDK)
- **Node.js 20.x LTS**
- **pnpm 8.x** (`npm install -g pnpm`)
- **Maven 3.9+**
- **Docker 24.x** et **Docker Compose**
- **Git 2.x**

### Recommandé

- **IDE:** IntelliJ IDEA (Java) + VS Code (Node.js/React)
- **Kubernetes:** Minikube ou Docker Desktop (pour tests locaux)
- **PostgreSQL 16** et **MongoDB 7** (ou via Docker Compose)

### Vérification

```bash
# Vérifier les versions
java -version        # Doit afficher Java 17+
node -v              # Doit afficher v20.x.x
pnpm -v              # Doit afficher 8.x.x
mvn -v               # Doit afficher 3.9.x
docker -v            # Doit afficher 24.x.x
```

---

## 🚀 Installation

### 1. Cloner le repository

```bash
git clone https://github.com/AQUILA04/eHealth.git
cd eHealth
```

### 2. Installer les dépendances

#### Option A: Script automatique (Recommandé)

**Linux/macOS:**

```bash
chmod +x infrastructure/scripts/setup.sh
./infrastructure/scripts/setup.sh
```

**Windows:**

```cmd
infrastructure\scripts\setup.bat
```

#### Option B: Installation manuelle

**Dépendances Node.js:**

```bash
pnpm install
```

**Dépendances Java:**

```bash
mvn clean install -DskipTests
```

### 3. Configuration de l'environnement

Créer un fichier `.env` à la racine:

```bash
# Database
POSTGRES_HOST=localhost
POSTGRES_PORT=5432
POSTGRES_USER=ehealth
POSTGRES_PASSWORD=ehealth_dev_password
POSTGRES_DB=ehealth_dev

MONGO_HOST=localhost
MONGO_PORT=27017
MONGO_USER=ehealth
MONGO_PASSWORD=ehealth_dev_password
MONGO_DB=ehealth_dev

# RabbitMQ
RABBITMQ_HOST=localhost
RABBITMQ_PORT=5672
RABBITMQ_USER=ehealth
RABBITMQ_PASSWORD=ehealth_dev_password

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379

# JWT
JWT_SECRET=your-secret-key-change-in-production
JWT_EXPIRATION=3600

# API Gateway
API_GATEWAY_PORT=3000
```

### 4. Démarrer l'infrastructure locale

```bash
pnpm docker:up
```

Cette commande démarre:

- PostgreSQL (port 5432)
- MongoDB (port 27017)
- RabbitMQ (port 5672, Management UI: 15672)
- Redis (port 6379)
- Prometheus (port 9090)
- Grafana (port 3000)
- ELK Stack (Elasticsearch: 9200, Kibana: 5601)
- MailDev (Web UI: 1080, SMTP: 1025)

---

## 💻 Développement

### Structure du projet

```
eHealth/
├── services/
│   ├── java/           # Services Java/Spring Boot (critiques)
│   ├── nodejs/         # Services Node.js/NestJS (légers)
│   └── frontend/       # Application React
├── shared/
│   ├── proto/          # Définitions gRPC
│   ├── types-java/     # Types Java partagés
│   └── types-ts/       # Types TypeScript partagés
├── contract/           # Contrats API (Frontend/Backend)
├── infrastructure/     # Docker, Kubernetes, Terraform
└── docs/               # Documentation
```

### Commandes de développement

#### Démarrer tous les services en mode développement

```bash
pnpm dev
```

#### Démarrer un service spécifique

**Service Java:**

```bash
cd services/java/empi-service
mvn spring-boot:run
```

**Service Node.js:**

```bash
cd services/nodejs/api-gateway
pnpm dev
```

**Frontend:**

```bash
cd services/frontend
pnpm dev
```

### Linting et formatage

```bash
# Vérifier le linting
pnpm lint

# Corriger automatiquement
pnpm lint:fix

# Formater le code
pnpm format

# Vérifier le formatage
pnpm format:check
```

---

## 🧪 Tests

### Exécuter tous les tests

#### Option A: Script automatique

```bash
# Linux/macOS
./infrastructure/scripts/test.sh

# Windows
infrastructure\scripts\test.bat
```

#### Option B: Commandes manuelles

**Tests Java:**

```bash
mvn test
```

**Tests Node.js:**

```bash
pnpm test
```

**Tests avec couverture:**

```bash
# Java
mvn test jacoco:report

# Node.js
pnpm test:coverage
```

### Tests d'intégration

```bash
# Démarrer l'environnement de test
docker-compose -f infrastructure/docker/docker-compose.test.yml up -d

# Exécuter les tests d'intégration
mvn verify
pnpm test:e2e
```

---

## 📦 Build

### Build de tous les services

#### Option A: Script automatique

```bash
# Linux/macOS
./infrastructure/scripts/build.sh

# Windows
infrastructure\scripts\build.bat
```

#### Option B: Commandes manuelles

**Services Java:**

```bash
mvn clean package -DskipTests
```

**Services Node.js:**

```bash
pnpm build
```

**Frontend:**

```bash
cd services/frontend
pnpm build
```

### Build des images Docker

```bash
# Build toutes les images
pnpm docker:build

# Build une image spécifique
docker build -f infrastructure/docker/Dockerfile.java \
  --build-arg SERVICE_NAME=empi-service \
  -t ehealth/empi-service:latest .
```

---

## 🚢 Déploiement

### Déploiement local (Docker Compose)

```bash
# Environnement de développement (stack autonome)
docker compose -f infrastructure/docker/docker-compose.dev.yml up --build
```

### Déploiement Contabo (OptimizeSolux)

Compose métier uniquement (Postgres + EMPI/GAP/DPI/Tenant + Frontend), branché sur
**shared-traefik** + **optimize-common-infra** (Keycloak `auth.optimizesolux.com`).

Voir [deploy/README.md](./deploy/README.md) et [deploy/GITHUB-SECRETS-CONTABO.md](./deploy/GITHUB-SECRETS-CONTABO.md).

```bash
# Sur le VPS (via CD GitHub ou manuellement)
sudo /opt/ehealth/init.sh prod \
  ghcr.io/<org>/ehealth-frontend:<sha> \
  ghcr.io/<org>/ehealth-empi:<sha> \
  ghcr.io/<org>/ehealth-gap:<sha> \
  ghcr.io/<org>/ehealth-dpi:<sha> \
  ghcr.io/<org>/ehealth-tenant:<sha> \
  --ghcr-username ... --ghcr-token ...
```

### Déploiement Kubernetes

```bash
# Appliquer les manifests
kubectl apply -f infrastructure/kubernetes/namespaces/
kubectl apply -f infrastructure/kubernetes/configmaps/
kubectl apply -f infrastructure/kubernetes/secrets/
kubectl apply -f infrastructure/kubernetes/deployments/
kubectl apply -f infrastructure/kubernetes/services/
kubectl apply -f infrastructure/kubernetes/ingress/

# Vérifier le déploiement
kubectl get pods -n ehealth
kubectl get services -n ehealth
```

### CI/CD

Le pipeline GitHub Actions est configuré pour:

1. **Lint & Format Check** - Vérification de la qualité du code
2. **Build Java** - Compilation des services Java
3. **Build Node.js** - Compilation des services Node.js
4. **Test Java** - Tests unitaires et d'intégration Java
5. **Test Node.js** - Tests unitaires et d'intégration Node.js
6. **Security Scan** - Analyse de sécurité avec Trivy
7. **Build Contabo Images** - Push GHCR (`-frontend`, `-empi`, `-gap`, `-dpi`, `-tenant`) sur `main` / `release/**`
8. **CD Contabo** - Déploiement SSH via `deploy/init.sh` (branches `release/**` ou promote manuel)
7. **Build Docker** - Construction et push des images Docker
8. **Deploy** - Déploiement automatique (sur main/develop)

---

## 📚 Documentation

- **[Architecture du Monorepo](./MONOREPO_ARCHITECTURE.md)** - Structure détaillée du projet
- **[Architecture Technique](./docs/architecture.md)** - Architecture Hub-and-Spoke
- **[Spécifications Fonctionnelles](./docs/SIH%20_%20Fonctionnalités%20Détaillées%20Par%20Module.md)** - Fonctionnalités par module
- **[Contrats API](./contract/)** - Spécifications des interfaces
- **[Guide de Contribution](./CONTRIBUTING.md)** - Comment contribuer au projet

### Accès aux interfaces

- **Frontend:** http://localhost:80
- **API Gateway:** http://localhost:3000
- **RabbitMQ Management:** http://localhost:15672 (user: ehealth, pass: ehealth_dev_password)
- **Prometheus:** http://localhost:9090
- **Grafana:** http://localhost:3000 (user: admin, pass: admin)
- **Kibana:** http://localhost:5601
- **MailDev:** http://localhost:1080

---

## 🤝 Contribution

Nous suivons la méthodologie **Git Flow** pour la gestion des branches:

### Branches principales

- `main` - Code en production (protégée)
- `develop` - Code en développement (protégée)
- `release/*` - Préparation de release (protégée)

### Branches de travail

- `feature/*` - Nouvelles fonctionnalités
- `bugfix/*` - Corrections de bugs
- `hotfix/*` - Corrections urgentes en production

### Workflow de contribution

1. **Créer une branche depuis develop:**

   ```bash
   git checkout develop
   git pull origin develop
   git checkout -b feature/ma-fonctionnalite
   ```

2. **Développer et commiter:**

   ```bash
   git add .
   git commit -m "feat: ajout de ma fonctionnalité"
   ```

3. **Pousser et créer une Pull Request:**

   ```bash
   git push origin feature/ma-fonctionnalite
   ```

4. **Attendre la review et la validation CI/CD**

5. **Merger après approbation**

### Conventions de commit

Nous utilisons [Conventional Commits](https://www.conventionalcommits.org/):

- `feat:` - Nouvelle fonctionnalité
- `fix:` - Correction de bug
- `docs:` - Documentation
- `style:` - Formatage, points-virgules manquants, etc.
- `refactor:` - Refactorisation du code
- `test:` - Ajout de tests
- `chore:` - Maintenance

---

## 📄 Licence

Ce projet est sous licence MIT. Voir le fichier [LICENSE](./LICENSE) pour plus de détails.

---

## 👥 Équipe

- **Architecte:** Winston
- **Organisation:** AQUILA04

---

## 📞 Support

Pour toute question ou problème:

- **Issues:** https://github.com/AQUILA04/eHealth/issues
- **Discussions:** https://github.com/AQUILA04/eHealth/discussions

---

**Fait avec ❤️ pour améliorer les soins de santé**
