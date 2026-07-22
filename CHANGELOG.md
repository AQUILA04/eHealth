# Changelog - eHealth

Tous les changements notables de ce projet seront documentés dans ce fichier.

Le format est basé sur [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
et ce projet adhère à [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [0.5.0] - 2026-07-22

### Added

- **Stack Docker de développement** (`infrastructure/docker/docker-compose.dev.yml`) : démarrage complet en une commande
  - Service `keycloak-db` : PostgreSQL 16 dédié à Keycloak avec healthcheck `pg_isready`
  - Service `keycloak` : Keycloak 26 avec import automatique du realm `ehealth` au démarrage (`--import-realm`)
  - Service `empi-service` : mock EMPI (profil `mock`, H2 in-memory) sur le port 8081
  - Service `gap-service` : Module I (profil `secure`, H2 in-memory) sur le port 8082, dépendant de l'EMPI et de Keycloak
  - Service `dpi-service` : Module II (profil `secure`, H2 in-memory) sur le port 8083, dépendant du GAP et de Keycloak
  - Service `frontend` : application React via Nginx sur le port 3000 avec proxy inverse vers les trois backends
  - Service `maildev` : serveur SMTP de test (SMTP 1025, UI 8025) pour capturer les emails sans envoi réel
  - Chaîne de dépendances avec `condition: service_healthy` garantissant l'ordre de démarrage
- **Dockerfiles multi-stage** pour chaque service Java et le frontend
  - `services/java/empi-service/Dockerfile` : build Maven + runtime JRE 21 Alpine
  - `services/java/gap-service/Dockerfile` : build Maven + runtime JRE 21 Alpine
  - `services/java/dpi-service/Dockerfile` : build Maven + runtime JRE 21 Alpine
  - `frontend/Dockerfile` : build Vite (Node 22) + Nginx 1.27 Alpine avec injection des variables `VITE_*` au build-time
  - `frontend/nginx.conf` : configuration Nginx avec proxy inverse vers les backends, SPA fallback et cache des assets statiques
- **Configuration inter-services externalisée** : `EMPI_SERVICE_URL` et `GAP_SERVICE_URL` configurables via variables d'environnement (valeurs par défaut `localhost` pour le développement local sans Docker)

### Fixed

- URLs inter-services hardcodées en `localhost` dans `application.yml` du GAP et du DPI remplacées par des placeholders `${EMPI_SERVICE_URL:...}` et `${GAP_SERVICE_URL:...}` pour compatibilité Docker

## [0.4.0] - 2026-07-22

### Added

- **Frontend React** (`frontend/`) : application SPA Vite 6 + React 18 + TypeScript + TailwindCSS
  - Design system médical avec palette `primary` (bleu clinique), `success`, `warning`, `danger`, typographie Inter + JetBrains Mono
  - Composants UI partagés : `Button`, `Badge`, `Card`, `Input`, `Select`, `Spinner`, `Modal`, `Table`, `EmptyState`
  - Layout `AppShell` avec sidebar de navigation, header contextuel et zone de contenu principale
  - Intégration **Keycloak** (OIDC) avec `KeycloakProvider` et intercepteur Axios pour injection automatique du JWT Bearer
  - Support du mode `VITE_AUTH_ENABLED=false` pour développement sans Keycloak actif
  - **Module GAP** : pages Patients (liste + création), Admissions ADT (cycle complet admission/transfert/sortie), Tableau des lits, Rendez-vous
  - **Module DPI** : pages Dossiers cliniques, Détail dossier (constantes vitales + CPOE + examens), Prescriptions globales, Examens de laboratoire
  - Dashboard d'accueil avec KPIs temps réel (patients actifs, admissions, rendez-vous du jour)
  - Proxy Vite vers les backends GAP (`:8082`), DPI (`:8083`), EMPI (`:8081`)
  - Build de production validé : 422 kB JS (gzip: 126 kB), 0 erreur TypeScript

## [0.3.0] - 2026-07-01

### Added
- **Keycloak Integration (IAM)**
  - Serveur d'autorisation RBAC avec realm `ehealth` auto-configuré (JSON import).
  - Ajout des services `keycloak` et `keycloak-db` dans `docker-compose.dev.yml`.
  - Configuration des rôles métiers : `MEDECIN`, `INFIRMIER`, `BIOLOGISTE`, `PHARMACIEN`, `ADMIN_GAP`, `COMPTABLE`, `PATIENT`.

- **Profils de sécurité (GAP & DPI)**
  - Profil `secure` : OAuth2 Resource Server avec validation JWT et RBAC strict par endpoint.
  - Profil `unsecure` : Configuration permissive sans authentification pour le développement rapide.
  - Convertisseur JWT personnalisé pour mapper les rôles Keycloak vers les `GrantedAuthority` de Spring Security.

- **Tests de sécurité (RBAC)**
  - Ajout de `GapSecurityIT` (16 tests) et `DpiSecurityIT` (11 tests) pour valider la matrice d'autorisation clinique et administrative avec des JWT mockés.

## [0.2.0] - 2026-06-30

### Added
- **Mock EMPI Service**
  - Implémentation d'un mock server EMPI avec base de données H2 in-memory.
  - Gestion de l'enregistrement des identités patients.
  - Algorithmes de déduplication (Jaro-Winkler et Levenshtein) pour détecter les doublons.
  - Endpoints REST pour la recherche et la récupération par UUID global.
  - Suite de tests unitaires et d'intégration complète.

- **GAP Service (Module I - Gestion Administrative du Patient)**
  - Implémentation des entités `Patient`, `Encounter` (ADT) et `Appointment`.
  - Contrôleur `PatientController` pour l'enregistrement et la liaison avec l'EMPI.
  - Contrôleur `EncounterController` pour le cycle ADT (Admission, Transfert, Sortie).
  - Gestion des statuts de lits (`BedStatus`) et des tableaux de bord (`getBedBoard`).
  - Suite de tests unitaires et d'intégration.

- **DPI Service (Module II - Dossier Patient Informatisé)**
  - Implémentation de l'entité `ClinicalEncounter` pour le suivi des dossiers cliniques.
  - Gestion des constantes vitales (`VitalSign`) avec calcul automatique de l'IMC.
  - Gestion des prescriptions médicamenteuses CPOE (`MedicationOrder`).
  - Gestion des demandes d'examens complémentaires et saisie des résultats (`LabOrder`).
  - Suite de tests unitaires et d'intégration couvrant le cycle clinique complet.

- **Configuration Globale**
  - Ajout du profil `mock` pour l'exécution locale avec données de test (`DataInitializer`).
  - Configuration de la sécurité permissive pour les environnements de développement.
  - Gestion centralisée des exceptions (`GlobalExceptionHandler`).
  - Mise à jour du `pom.xml` parent pour forcer l'utilisation de JUnit 5.12.2 et résoudre les conflits avec Spring Boot 3.5.
  - Configuration du plugin `maven-surefire-plugin` pour inclure l'exécution automatique des tests d'intégration (`*IT.java`).

### Fixed
- Mise à niveau du pipeline CI/CD de Java 17 vers Java 21 pour aligner avec la cible de compilation du projet.
- Suppression des services PostgreSQL et MongoDB du job de tests CI (remplacement par H2 in-memory via le profil `mock`).
- Mise à jour de `Dockerfile.java` vers les images `eclipse-temurin:21`.

### Changed
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
  - Support des branches main, develop, feature/_, release/_

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
