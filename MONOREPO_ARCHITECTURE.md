# Architecture du Monorepo eHealth - Approche Polyglotte

## Vue d'ensemble

Le projet eHealth est organisé comme un **monorepo polyglotte** utilisant une approche stratégique:
- **Java/Spring Boot** pour les services critiques (EMPI, HIE, DPI) nécessitant FHIR R4/R5 et haute performance transactionnelle
- **Node.js/NestJS** pour les services légers (Workflow, Notifications, API Gateway)
- **React** pour le frontend

Cette approche optimise le projet pour la production tout en maintenant une gestion centralisée.

## Structure du Monorepo

```
eHealth/
├── .github/
│   ├── workflows/
│   │   ├── ci.yml                    # Pipeline CI/CD principal
│   │   ├── build-java.yml            # Build services Java
│   │   ├── build-nodejs.yml          # Build services Node.js
│   │   ├── test.yml                  # Tests tous les services
│   │   ├── deploy.yml                # Déploiement
│   │   └── security.yml              # Analyse de sécurité
│   └── PULL_REQUEST_TEMPLATE.md
│
├── services/
│   │
│   ├── java/                         # Services Java/Spring Boot (CRITIQUES)
│   │   │
│   │   ├── empi-service/             # Service EMPI (Master Patient Index)
│   │   │   ├── pom.xml
│   │   │   ├── src/
│   │   │   │   ├── main/java/
│   │   │   │   │   └── com/sih/empi/
│   │   │   │   │       ├── EmpiServiceApplication.java
│   │   │   │   │       ├── controller/
│   │   │   │   │       ├── service/
│   │   │   │   │       ├── repository/
│   │   │   │   │       ├── entity/
│   │   │   │   │       ├── dto/
│   │   │   │   │       ├── exception/
│   │   │   │   │       └── config/
│   │   │   │   ├── test/java/
│   │   │   │   └── resources/
│   │   │   │       ├── application.yml
│   │   │   │       ├── application-dev.yml
│   │   │   │       └── application-prod.yml
│   │   │   ├── Dockerfile
│   │   │   └── README.md
│   │   │
│   │   ├── hie-router/               # Health Information Exchange Router
│   │   │   ├── pom.xml
│   │   │   ├── src/
│   │   │   ├── Dockerfile
│   │   │   └── README.md
│   │   │
│   │   ├── dpi-service/              # Dossier Patient Informatisé
│   │   │   ├── pom.xml
│   │   │   ├── src/
│   │   │   │   ├── main/java/
│   │   │   │   │   └── com/sih/dpi/
│   │   │   │   │       ├── DpiServiceApplication.java
│   │   │   │   │       ├── controller/
│   │   │   │   │       ├── service/
│   │   │   │   │       ├── repository/
│   │   │   │   │       ├── entity/
│   │   │   │   │       ├── dto/
│   │   │   │   │       ├── fhir/
│   │   │   │   │       ├── exception/
│   │   │   │   │       └── config/
│   │   │   │   ├── test/java/
│   │   │   │   └── resources/
│   │   │   ├── Dockerfile
│   │   │   └── README.md
│   │   │
│   │   ├── consent-service/          # Service de gestion des consentements
│   │   │   ├── pom.xml
│   │   │   ├── src/
│   │   │   ├── Dockerfile
│   │   │   └── README.md
│   │   │
│   │   ├── terminology-service/      # Service de terminologie (SNOMED, LOINC, ICD)
│   │   │   ├── pom.xml
│   │   │   ├── src/
│   │   │   ├── Dockerfile
│   │   │   └── README.md
│   │   │
│   │   ├── gap-service/              # Gestion Administrative du Patient
│   │   │   ├── pom.xml
│   │   │   ├── src/
│   │   │   │   ├── main/java/
│   │   │   │   │   └── com/sih/gap/
│   │   │   │   │       ├── GapServiceApplication.java
│   │   │   │   │       ├── controller/
│   │   │   │   │       ├── service/
│   │   │   │   │       ├── repository/
│   │   │   │   │       ├── entity/
│   │   │   │   │       ├── dto/
│   │   │   │   │       ├── exception/
│   │   │   │   │       └── config/
│   │   │   │   ├── test/java/
│   │   │   │   └── resources/
│   │   │   ├── Dockerfile
│   │   │   └── README.md
│   │   │
│   │   ├── lis-service/              # Laboratory Information System
│   │   │   ├── pom.xml
│   │   │   ├── src/
│   │   │   ├── Dockerfile
│   │   │   └── README.md
│   │   │
│   │   ├── ris-service/              # Radiology Information System
│   │   │   ├── pom.xml
│   │   │   ├── src/
│   │   │   ├── Dockerfile
│   │   │   └── README.md
│   │   │
│   │   ├── pharmacy-service/         # Service Pharmacie
│   │   │   ├── pom.xml
│   │   │   ├── src/
│   │   │   ├── Dockerfile
│   │   │   └── README.md
│   │   │
│   │   └── pom.xml                   # POM parent pour tous les services Java
│   │
│   ├── nodejs/                       # Services Node.js/NestJS (LÉGERS)
│   │   │
│   │   ├── api-gateway/              # API Gateway
│   │   │   ├── package.json
│   │   │   ├── src/
│   │   │   │   ├── main.ts
│   │   │   │   ├── app.module.ts
│   │   │   │   ├── gateway/
│   │   │   │   ├── middleware/
│   │   │   │   ├── interceptors/
│   │   │   │   └── config/
│   │   │   ├── test/
│   │   │   ├── Dockerfile
│   │   │   ├── tsconfig.json
│   │   │   └── README.md
│   │   │
│   │   ├── workflow-engine/          # Moteur d'orchestration des workflows
│   │   │   ├── package.json
│   │   │   ├── src/
│   │   │   ├── test/
│   │   │   ├── Dockerfile
│   │   │   ├── tsconfig.json
│   │   │   └── README.md
│   │   │
│   │   ├── notification-service/     # Service de notifications
│   │   │   ├── package.json
│   │   │   ├── src/
│   │   │   ├── test/
│   │   │   ├── Dockerfile
│   │   │   ├── tsconfig.json
│   │   │   └── README.md
│   │   │
│   │   ├── audit-service/            # Service d'audit et logging
│   │   │   ├── package.json
│   │   │   ├── src/
│   │   │   ├── test/
│   │   │   ├── Dockerfile
│   │   │   ├── tsconfig.json
│   │   │   └── README.md
│   │   │
│   │   └── package.json              # Package.json racine Node.js
│   │
│   └── frontend/                     # Application Web (React)
│       ├── package.json
│       ├── public/
│       ├── src/
│       │   ├── components/           # Composants réutilisables
│       │   │   ├── common/
│       │   │   ├── layout/
│       │   │   ├── forms/
│       │   │   └── charts/
│       │   ├── pages/
│       │   │   ├── dashboard/
│       │   │   ├── patient/
│       │   │   ├── clinical/
│       │   │   └── admin/
│       │   ├── services/
│       │   │   ├── api.ts
│       │   │   ├── auth.ts
│       │   │   └── storage.ts
│       │   ├── hooks/
│       │   ├── context/
│       │   ├── types/
│       │   ├── utils/
│       │   ├── styles/
│       │   └── App.tsx
│       ├── test/
│       ├── Dockerfile
│       ├── tsconfig.json
│       ├── vite.config.ts
│       └── README.md
│
├── shared/                           # Code partagé (Protobuf, Types, Utils)
│   │
│   ├── proto/                        # Définitions gRPC (Protobuf)
│   │   ├── patient.proto
│   │   ├── empi.proto
│   │   ├── clinical.proto
│   │   ├── common.proto
│   │   └── README.md
│   │
│   ├── types-java/                   # Types Java partagés
│   │   ├── pom.xml
│   │   ├── src/main/java/
│   │   │   └── com/sih/shared/
│   │   │       ├── dto/
│   │   │       ├── entity/
│   │   │       ├── exception/
│   │   │       └── constant/
│   │   └── README.md
│   │
│   └── types-ts/                     # Types TypeScript partagés
│       ├── package.json
│       ├── src/
│       │   ├── types/
│       │   ├── constants/
│       │   └── index.ts
│       └── README.md
│
├── contract/                         # Contrats d'interface (Frontend/Backend)
│   ├── api-v1.md                     # Spécification API v1
│   ├── changelog.md                  # Historique des modifications
│   └── endpoints/
│       ├── gap-service.md
│       ├── dpi-service.md
│       ├── empi-service.md
│       └── ...
│
├── infrastructure/
│   ├── docker/
│   │   ├── docker-compose.dev.yml    # Environnement de développement
│   │   ├── docker-compose.prod.yml   # Environnement de production
│   │   ├── docker-compose.test.yml   # Environnement de test
│   │   ├── Dockerfile.java           # Image de base Java
│   │   ├── Dockerfile.nodejs         # Image de base Node.js
│   │   └── Dockerfile.frontend       # Image de base Frontend
│   │
│   ├── kubernetes/
│   │   ├── namespaces/
│   │   ├── deployments/
│   │   ├── services/
│   │   ├── configmaps/
│   │   ├── secrets/
│   │   └── ingress/
│   │
│   ├── terraform/                    # Infrastructure as Code
│   │   ├── main.tf
│   │   ├── variables.tf
│   │   ├── outputs.tf
│   │   └── modules/
│   │
│   └── scripts/
│       ├── setup.sh                  # Script de setup initial
│       ├── build.sh                  # Script de build
│       ├── test.sh                   # Script de test
│       ├── deploy.sh                 # Script de déploiement
│       ├── setup.bat                 # Équivalent Windows
│       ├── build.bat
│       ├── test.bat
│       └── deploy.bat
│
├── docs/
│   ├── architecture.md               # Documentation d'architecture
│   ├── api.md                        # Documentation API
│   ├── development.md                # Guide de développement
│   ├── deployment.md                 # Guide de déploiement
│   ├── security.md                   # Politique de sécurité
│   ├── database.md                   # Schémas de base de données
│   ├── java-guidelines.md            # Guide de développement Java
│   ├── nodejs-guidelines.md          # Guide de développement Node.js
│   └── contributing.md               # Guide de contribution
│
├── .github/
│   └── workflows/                    # Voir section ci-dessus
│
├── .gitignore
├── .gitattributes
├── .editorconfig
├── .prettierrc
├── .eslintrc.json
├── pom.xml                           # POM parent racine (optionnel)
├── package.json                      # Package.json racine (pour scripts globaux)
├── README.md                         # README principal
├── CONTRIBUTING.md                   # Guide de contribution
├── MONOREPO_ARCHITECTURE.md          # Ce document
├── LICENSE
└── CHANGELOG.md                      # Historique des versions

```

## Stratégie de Dépendances

### Services Java (Spring Boot)
**Dépendances communes (pom.xml parent):**
- `spring-boot-starter-web`
- `spring-boot-starter-data-jpa`
- `spring-boot-starter-security`
- `spring-boot-starter-validation`
- `org.springframework.cloud:spring-cloud-starter-config`
- `ca.uhn.hapi.fhir:hapi-fhir-base` (FHIR R4/R5)
- `ca.uhn.hapi.fhir:hapi-fhir-structures-r4`
- `io.grpc:grpc-netty-shaded`
- `io.grpc:grpc-protobuf`
- `io.grpc:grpc-stub`
- `org.postgresql:postgresql`
- `org.mongodb:mongodb-driver-sync`
- `org.springframework.amqp:spring-rabbit`
- `org.springframework.kafka:spring-kafka`
- `io.jsonwebtoken:jjwt`
- `org.springframework.boot:spring-boot-starter-actuator`
- `io.micrometer:micrometer-registry-prometheus`

### Services Node.js (NestJS)
**Dépendances communes (services/nodejs/package.json):**
- `@nestjs/core`
- `@nestjs/common`
- `@nestjs/platform-express`
- `@nestjs/config`
- `@nestjs/typeorm`
- `@nestjs/mongoose`
- `@nestjs/microservices`
- `@nestjs/grpc`
- `@grpc/grpc-js`
- `@grpc/proto-loader`
- `typeorm`
- `mongoose`
- `pg`
- `amqplib`
- `kafkajs`
- `jsonwebtoken`
- `passport`
- `helmet`
- `pino` (logging)
- `class-validator`
- `class-transformer`

### Frontend (React)
**Dépendances (services/frontend/package.json):**
- `react`
- `react-dom`
- `react-router-dom`
- `axios`
- `zustand` (state management)
- `react-query`
- `tailwindcss`
- `typescript`
- `vite`

## Conventions de Nommage

### Java
- **Packages:** `com.sih.{service}.{module}` (ex: `com.sih.empi.service`)
- **Classes:** PascalCase (ex: `EmpiService`, `PatientController`)
- **Méthodes:** camelCase (ex: `getPatientById()`)
- **Constantes:** UPPER_SNAKE_CASE

### Node.js/TypeScript
- **Packages:** kebab-case (ex: `api-gateway`, `workflow-engine`)
- **Fichiers:** kebab-case (ex: `patient.service.ts`, `auth.middleware.ts`)
- **Classes/Interfaces:** PascalCase (ex: `PatientService`, `IPatient`)
- **Fonctions/Variables:** camelCase

### Git Branches
- `main` - Production
- `develop` - Développement
- `feature/*` - Nouvelles fonctionnalités
- `bugfix/*` - Corrections de bugs
- `release/*` - Préparation de release
- `hotfix/*` - Corrections urgentes en production

## Outils et Frameworks

| Catégorie | Outil | Version | Justification |
|-----------|-------|---------|---------------|
| **Backend Java** | Spring Boot | 3.1.x | Écosystème mature, FHIR, microservices |
| **Backend Java** | Spring Cloud | 2022.x | Service discovery, config management |
| **Backend Java** | HAPI FHIR | 6.x | Référence FHIR R4/R5 |
| **Backend Node.js** | NestJS | 10.x | Architecture modulaire, gRPC |
| **Frontend** | React | 18.x | Composants, écosystème riche |
| **Frontend Build** | Vite | 5.x | Performance, HMR rapide |
| **Database (Relational)** | PostgreSQL | 16.x | ACID, JSONB, robustesse |
| **Database (Document)** | MongoDB | 7.x | Flexibilité pour DPI |
| **gRPC** | Protocol Buffers | 3.x | Performance, contrats stricts |
| **Messaging** | RabbitMQ | 3.12.x | Fiabilité, event-driven |
| **Testing (Java)** | JUnit 5 | 5.9.x | Framework standard |
| **Testing (Node.js)** | Jest | 29.x | Coverage, snapshot testing |
| **Container** | Docker | 24.x | Isolation, déploiement |
| **Orchestration** | Kubernetes | 1.28.x | Scalabilité, HA |
| **Build (Java)** | Maven | 3.9.x | Gestion dépendances, plugins |
| **Build (Node.js)** | pnpm | 8.x | Efficacité workspaces |

## Phases d'Implémentation

### Phase 1: Setup Monorepo (ACTUELLE)
- [x] Initialiser structure polyglotte
- [x] Configurer Maven parent et pnpm workspaces
- [ ] Configurer CI/CD multi-langage
- [ ] Initialiser gitflow

### Phase 2: Services Fondamentaux (Java)
- [ ] Service EMPI (Hub) - Spring Boot + HAPI FHIR
- [ ] Service GAP (Spoke) - Spring Boot
- [ ] Service DPI (Spoke) - Spring Boot + HAPI FHIR
- [ ] API Gateway (Node.js)

### Phase 3: Services Secondaires (Java)
- [ ] CPOE Service
- [ ] LIS Service
- [ ] RIS Service
- [ ] Pharmacy Service

### Phase 4: Services Légers (Node.js)
- [ ] Workflow Engine
- [ ] Notification Service
- [ ] Audit Service

### Phase 5: Frontend et Intégration
- [ ] Application React
- [ ] Intégration API Gateway
- [ ] Tests E2E

### Phase 6: Infrastructure et Déploiement
- [ ] Docker Compose multi-langage
- [ ] Kubernetes manifests
- [ ] Terraform IaC
- [ ] Monitoring et Logging

## Bonnes Pratiques

### Général
1. **Isolation des Services:** Chaque service a sa propre DB
2. **Communication:** gRPC interne (Java↔Java), REST/FHIR externe
3. **Versioning:** Semantic versioning pour tous les packages
4. **Documentation:** README.md pour chaque service
5. **Tests:** Couverture minimale 80% pour services critiques

### Java
1. **Architecture:** Couches (Controller → Service → Repository)
2. **Validation:** `@Valid` + `ConstraintValidator`
3. **Logging:** SLF4J + Logback
4. **Monitoring:** Actuator + Prometheus
5. **Security:** Spring Security + JWT

### Node.js
1. **Architecture:** Modules NestJS avec DI
2. **Validation:** `class-validator` + `class-transformer`
3. **Logging:** Pino ou Winston
4. **Monitoring:** Prometheus client
5. **Security:** Helmet + Passport

### Frontend
1. **Composants:** Réutilisables, props bien typées
2. **State:** Zustand pour état global
3. **API:** Axios + React Query
4. **Styling:** Tailwind CSS
5. **Testing:** Vitest + React Testing Library

## Communication Inter-Services

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

