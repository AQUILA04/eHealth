# Changelog - eHealth

Tous les changements notables de ce projet seront documentés dans ce fichier.

Le format est basé sur [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
et ce projet adhère à [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- Architecture initiale du monorepo polyglotte
- Configuration Maven parent pour services Java
- Configuration pnpm workspaces pour services Node.js
- Pipeline CI/CD GitHub Actions complet
- Docker Compose pour environnement de développement
- Dockerfiles optimisés pour Java, Node.js et Frontend
- Configuration Nginx pour le frontend React
- Scripts de setup, build et test pour Windows et Unix
- Gitflow avec branches main, develop et feature/setup
- Structure complète des services (9 services Java, 4 services Node.js, 1 frontend)
- Configuration TypeScript, ESLint, Prettier partagée
- Documentation complète (README, CONTRIBUTING, MONOREPO_ARCHITECTURE)
- Template de pull request
- Support pour PostgreSQL, MongoDB, RabbitMQ, Redis, Prometheus, Grafana, ELK

### Changed
- Mise à jour du README avec documentation complète

### Fixed
- N/A

### Removed
- N/A

### Security
- Configuration de sécurité dans les Dockerfiles (utilisateurs non-root)
- Headers de sécurité dans Nginx
- Support JWT pour authentification

---

## [0.1.0] - 2025-12-11

### Initial Release

#### Added
- **Monorepo Structure**
  - Architecture polyglotte (Java + Node.js + React)
  - 9 services Java/Spring Boot (EMPI, HIE, DPI, GAP, CPOE, LIS, RIS, Pharmacy, Consent)
  - 4 services Node.js/NestJS (API Gateway, Workflow Engine, Notification Service, Audit Service)
  - 1 application Frontend React
  - Modules partagés (Proto, Types Java, Types TypeScript)

- **Build & Package Management**
  - Maven 3.9+ avec pom.xml parent
  - pnpm 8.x avec workspaces
  - Configuration TypeScript centralisée
  - Gestion des dépendances cohérente

- **Code Quality**
  - ESLint avec configuration TypeScript
  - Prettier pour formatage automatique
  - EditorConfig pour cohérence IDE
  - Conventions de nommage standardisées

- **CI/CD Pipeline**
  - GitHub Actions workflow complet
  - Lint & Format Check
  - Build Java et Node.js parallélisés
  - Tests unitaires et d'intégration
  - Security scan avec Trivy
  - Build et push Docker automatisé
  - Support des branches main, develop, feature/*, release/*

- **Infrastructure**
  - Docker Compose pour développement local
  - Services: PostgreSQL, MongoDB, RabbitMQ, Redis, Prometheus, Grafana, ELK, MailDev
  - Dockerfiles optimisés pour Java, Node.js et Frontend
  - Configuration Nginx pour le frontend
  - Scripts de setup, build, test (Windows & Unix)

- **Git & Workflow**
  - Gitflow avec branches protégées
  - Template de pull request
  - Conventions de commit (Conventional Commits)

- **Documentation**
  - README complet avec guide d'installation
  - MONOREPO_ARCHITECTURE.md avec détails techniques
  - CONTRIBUTING.md avec guide de contribution
  - CHANGELOG.md (ce fichier)

#### Technology Stack
- **Backend Java:** Spring Boot 3.1.5, HAPI FHIR 6.8.0, gRPC 1.59.0
- **Backend Node.js:** NestJS 10.2.1, TypeScript 5.2.2
- **Frontend:** React 18.2.0, Vite 5.0.0, Tailwind CSS 3.3.0
- **Databases:** PostgreSQL 16, MongoDB 7
- **Messaging:** RabbitMQ 3.12
- **Monitoring:** Prometheus, Grafana
- **Logging:** Elasticsearch, Logstash, Kibana
- **Container:** Docker 24.x, Kubernetes 1.28.x

#### Known Issues
- Maven n'est pas installé dans l'environnement de développement (à installer manuellement)
- pnpm-lock.yaml n'est pas généré (à générer lors de la première installation)

---

## Format des versions futures

### [X.Y.Z] - YYYY-MM-DD

#### Added
- Nouvelles fonctionnalités

#### Changed
- Changements dans les fonctionnalités existantes

#### Deprecated
- Fonctionnalités qui seront supprimées dans les versions futures

#### Removed
- Fonctionnalités supprimées

#### Fixed
- Corrections de bugs

#### Security
- Corrections de sécurité

---

## Roadmap

### Phase 2: Services Fondamentaux (Q1 2025)
- [ ] Implémentation complète du service EMPI
- [ ] Implémentation du service GAP
- [ ] Implémentation du service DPI avec FHIR
- [ ] Implémentation de l'API Gateway
- [ ] Tests d'intégration complets

### Phase 3: Services Secondaires (Q2 2025)
- [ ] Implémentation du service CPOE
- [ ] Implémentation du service LIS
- [ ] Implémentation du service RIS
- [ ] Implémentation du service Pharmacy
- [ ] Intégration avec les services fondamentaux

### Phase 4: Frontend & UX (Q2-Q3 2025)
- [ ] Développement de l'interface React
- [ ] Intégration avec l'API Gateway
- [ ] Tests E2E
- [ ] Optimisation des performances

### Phase 5: Infrastructure & DevOps (Q3 2025)
- [ ] Configuration Kubernetes complète
- [ ] Terraform pour IaC
- [ ] Monitoring et alerting
- [ ] Documentation de déploiement

### Phase 6: Production & Hardening (Q4 2025)
- [ ] Audit de sécurité
- [ ] Tests de charge
- [ ] Documentation de production
- [ ] Release v1.0.0

---

## Comment contribuer

Voir [CONTRIBUTING.md](./CONTRIBUTING.md) pour les directives de contribution.

---

**Dernière mise à jour:** 2025-12-11
