# Walkthrough: Évolution Multi-Tenant SaaS

Nous avons achevé avec succès la transition vers le modèle multi-tenant SaaS pour les modules Spoke (GAP et DPI), en incluant l'interface utilisateur d'administration.

## Modifications effectuées

### 1. Initialisation du nouveau microservice `tenant-service`
*   Création du fichier [pom.xml](file:///c:/Users/kahonsu/Documents/GitHub/eHealth/services/java/tenant-service/pom.xml) avec les dépendances nécessaires (JPA, Web, Security, Actuator, PostgreSQL, H2).
*   Enregistrement du module dans le fichier [pom.xml parent](file:///c:/Users/kahonsu/Documents/GitHub/eHealth/pom.xml).
*   Création de l'application principale [TenantServiceApplication.java](file:///c:/Users/kahonsu/Documents/GitHub/eHealth/services/java/tenant-service/src/main/java/com/sih/tenant/TenantServiceApplication.java).

### 2. Modèle de données & Persistance
*   Création de l'entité JPA [Tenant.java](file:///c:/Users/kahonsu/Documents/GitHub/eHealth/services/java/tenant-service/src/main/java/com/sih/tenant/entity/Tenant.java) avec la structure demandée (id, name, domain, status, contactEmail, contactPhone, timestamps).
*   Création de l'énumération [TenantStatus.java](file:///c:/Users/kahonsu/Documents/GitHub/eHealth/services/java/tenant-service/src/main/java/com/sih/tenant/entity/TenantStatus.java) (ACTIVE, INACTIVE, SUSPENDED).
*   Création du repository [TenantRepository.java](file:///c:/Users/kahonsu/Documents/GitHub/eHealth/services/java/tenant-service/src/main/java/com/sih/tenant/repository/TenantRepository.java).

### 3. Logique Métier & API REST (CRUD)
*   Création du service [TenantService.java](file:///c:/Users/kahonsu/Documents/GitHub/eHealth/services/java/tenant-service/src/main/java/com/sih/tenant/service/TenantService.java).
*   Mise en place de la structure de réponse normalisée via [Response.java](file:///c:/Users/kahonsu/Documents/GitHub/eHealth/services/java/tenant-service/src/main/java/com/sih/tenant/dto/Response.java).
*   Création du contrôleur REST [TenantController.java](file:///c:/Users/kahonsu/Documents/GitHub/eHealth/services/java/tenant-service/src/main/java/com/sih/tenant/controller/TenantController.java) exposant les endpoints CRUD.
*   Implémentation de la gestion d'erreurs globale avec [GlobalExceptionHandler.java](file:///c:/Users/kahonsu/Documents/GitHub/eHealth/services/java/tenant-service/src/main/java/com/sih/tenant/config/GlobalExceptionHandler.java) retournant le format `Response` unifié.

### 4. Configuration de Sécurité & Profils
*   Configuration des propriétés pour les profils `mock` et `secure` via [application.yml](file:///c:/Users/kahonsu/Documents/GitHub/eHealth/services/java/tenant-service/src/main/resources/application.yml) et [application-secure.yml](file:///c:/Users/kahonsu/Documents/GitHub/eHealth/services/java/tenant-service/src/main/resources/application-secure.yml).
*   Développement des configurations de sécurité Spring Security [SecurityConfig.java](file:///c:/Users/kahonsu/Documents/GitHub/eHealth/services/java/tenant-service/src/main/java/com/sih/tenant/config/SecurityConfig.java) (sans auth pour dev/mock) et [SecureSecurityConfig.java](file:///c:/Users/kahonsu/Documents/GitHub/eHealth/services/java/tenant-service/src/main/java/com/sih/tenant/security/SecureSecurityConfig.java) (sécurité Keycloak OAuth2 active pour la prod).

### 5. Connecteurs administratifs Keycloak (Provisionnement de Tenant)
*   Ajout de la dépendance officielle `keycloak-admin-client` dans le [pom.xml de tenant-service](file:///c:/Users/kahonsu/Documents/GitHub/eHealth/services/java/tenant-service/pom.xml).
*   Définition de l'interface commune [KeycloakConnector.java](file:///c:/Users/kahonsu/Documents/GitHub/eHealth/services/java/tenant-service/src/main/java/com/sih/tenant/service/KeycloakConnector.java) pour séparer les environnements.
*   Implémentation mock [MockKeycloakConnector.java](file:///c:/Users/kahonsu/Documents/GitHub/eHealth/services/java/tenant-service/src/main/java/com/sih/tenant/service/MockKeycloakConnector.java) pour le développement et les tests.
*   Implémentation réelle [RealKeycloakConnector.java](file:///c:/Users/kahonsu/Documents/GitHub/eHealth/services/java/tenant-service/src/main/java/com/sih/tenant/service/RealKeycloakConnector.java) utilisant l'API d'administration Keycloak pour créer des groupes de tenants, supprimer des groupes et activer/désactiver en bloc tous les utilisateurs rattachés au groupe d'un tenant.
*   Mise à jour de [TenantService.java](file:///c:/Users/kahonsu/Documents/GitHub/eHealth/services/java/tenant-service/src/main/java/com/sih/tenant/service/TenantService.java) pour déclencher automatiquement les appels Keycloak lors de la création d'un tenant et des mises à jour de statut (les utilisateurs Keycloak d'un hôpital sont désactivés en bloc si le statut du tenant devient suspendu/inactif).

### 6. Configuration de l'API Gateway (NestJS)
*   Mise à jour de [app.controller.ts](file:///c:/Users/kahonsu/Documents/GitHub/eHealth/services/nodejs/api-gateway/src/app.controller.ts) pour transformer la passerelle en proxy inverse transparent pour les services GAP, DPI, EMPI et Tenant.
*   **Validation & Extraction de Tenant** : Extraction du token d'autorisation JWT et décodage Base64 léger pour récupérer le claim `tenant_id`.
*   **Vérification de Statut** : Appel dynamique au `tenant-service` pour s'assurer que le tenant associé à l'utilisateur est bien `ACTIVE`.
*   **Mise en Cache** : Implémentation d'un cache mémoire des statuts des tenants (durée de vie 1 min) dans l'API Gateway pour ne pas surcharger le `tenant-service`.
*   **Injection de Header** : Propagation de l'identifiant extrait dans le header HTTP `X-Tenant-ID` vers les microservices concernés.

### 7. Bibliothèque partagée backend (`shared-tenant-lib`)
*   Création du module partagé [shared-tenant-lib](file:///c:/Users/kahonsu/Documents/GitHub/shared-tenant-lib) dans le répertoire `shared`.
*   Développement de [TenantContext.java](file:///c:/Users/kahonsu/Documents/GitHub/eHealth/shared/shared-tenant-lib/src/main/java/com/sih/shared/tenant/TenantContext.java) basé sur `ThreadLocal` pour maintenir l'identifiant du tenant au niveau du thread applicatif.
*   Développement du filtre HTTP [TenantFilter.java](file:///c:/Users/kahonsu/Documents/GitHub/eHealth/shared/shared-tenant-lib/src/main/java/com/sih/shared/tenant/TenantFilter.java) qui intercepte les requêtes web, extrait le header `X-Tenant-ID` et peuple le `TenantContext` avant de le vider en fin de requête.
*   Création de [TenantRoutingDataSource.java](file:///c:/Users/kahonsu/Documents/GitHub/eHealth/shared/shared-tenant-lib/src/main/java/com/sih/shared/tenant/TenantRoutingDataSource.java) étendant `DelegatingDataSource` pour intercepter les connexions JDBC et configurer la session SQL de façon transparente en fonction de la base de données :
    *   Exécute `SET app.current_tenant = '<id>'` si la base est PostgreSQL (support natif du RLS).
    *   Exécute `SET @app_current_tenant = '<id>'` (session H2) pour les environnements de test et développement locaux.
*   Création du `BeanPostProcessor` [TenantDataSourceConfig.java](file:///c:/Users/kahonsu/Documents/GitHub/eHealth/shared/shared-tenant-lib/src/main/java/com/sih/shared/tenant/TenantDataSourceConfig.java) pour emballer automatiquement n'importe quelle source de données auto-configurée dans les services Spring Boot dépendants.
*   Enregistrement dans le fichier d'auto-configuration de Spring Boot [org.springframework.boot.autoconfigure.AutoConfiguration.imports](file:///c:/Users/kahonsu/Documents/GitHub/eHealth/shared/shared-tenant-lib/src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports).

### 8. Réfactoring du module GAP (Module I)
*   Ajout de la dépendance `shared-tenant-lib` dans le [pom.xml de gap-service](file:///c:/Users/kahonsu/Documents/GitHub/eHealth/services/java/gap-service/pom.xml).
*   **Modifications des Entités** :
    *   [Patient.java](file:///c:/Users/kahonsu/Documents/GitHub/eHealth/services/java/gap-service/src/main/java/com/sih/gap/entity/Patient.java) : Ajout du champ `tenantId`. Modification de l'index unique de `localMrn` à `(tenantId, localMrn)` pour autoriser des MRN identiques dans des hôpitaux différents mais uniques par tenant. Intégration de l'écriture automatique du `tenantId` issu du ThreadLocal lors du `@PrePersist`.
    *   [Encounter.java](file:///c:/Users/kahonsu/Documents/GitHub/eHealth/services/java/gap-service/src/main/java/com/sih/gap/entity/Encounter.java) : Ajout du champ `tenantId` avec son index et de la méthode `@PrePersist` de peuplement.
    *   [Appointment.java](file:///c:/Users/kahonsu/Documents/GitHub/eHealth/services/java/gap-service/src/main/java/com/sih/gap/entity/Appointment.java) : Ajout du champ `tenantId` avec son index et de la méthode `@PrePersist` de peuplement.
*   **Intégration & Initialisation** : Enveloppement des données de test de la classe [DataInitializer.java](file:///c:/Users/kahonsu/Documents/GitHub/eHealth/services/java/gap-service/src/main/java/com/sih/gap/config/DataInitializer.java) dans un bloc try-finally définissant un tenant temporaire afin d'éviter les erreurs d'initialisation de base de données à froid.

### 9. Réfactoring du module DPI (Module II)
*   Ajout de la dépendance `shared-tenant-lib` dans le [pom.xml de dpi-service](file:///c:/Users/kahonsu/Documents/GitHub/eHealth/services/java/dpi-service/pom.xml).
*   **Modifications des Entités Cliniques** :
    *   [ClinicalEncounter.java](file:///c:/Users/kahonsu/Documents/GitHub/eHealth/services/java/dpi-service/src/main/java/com/sih/dpi/entity/ClinicalEncounter.java) : Ajout du champ `tenantId` avec indexation et gestion de la population automatique via `@PrePersist`.
    *   [VitalSign.java](file:///c:/Users/kahonsu/Documents/GitHub/eHealth/services/java/dpi-service/src/main/java/com/sih/dpi/entity/VitalSign.java) : Ajout du champ `tenantId` avec indexation et gestion de la population automatique via `@PrePersist`.
    *   [MedicationOrder.java](file:///c:/Users/kahonsu/Documents/GitHub/eHealth/services/java/dpi-service/src/main/java/com/sih/dpi/entity/MedicationOrder.java) : Ajout du champ `tenantId` avec indexation et gestion de la population automatique via `@PrePersist`.
    *   [LabOrder.java](file:///c:/Users/kahonsu/Documents/GitHub/eHealth/services/java/dpi-service/src/main/java/com/sih/dpi/entity/LabOrder.java) : Ajout du champ `tenantId` avec indexation et gestion de la population automatique via `@PrePersist`.
*   **Initialisation des données de test** : Configuration de `TenantContext` dans [DataInitializer.java](file:///c:/Users/kahonsu/Documents/GitHub/eHealth/services/java/dpi-service/src/main/java/com/sih/dpi/config/DataInitializer.java) lors du démarrage à froid.

### 10. Implémentation du Frontend de Gestion des Tenants
*   **Types de Données** : Ajout de la définition de l'interface `Tenant` dans [index.ts](file:///c:/Users/kahonsu/Documents/GitHub/eHealth/frontend/src/types/index.ts) décrivant la structure d'un tenant.
*   **Couche Service** : Création de [tenant.service.ts](file:///c:/Users/kahonsu/Documents/GitHub/eHealth/frontend/src/services/tenant.service.ts) pour interroger les endpoints REST `/api/v1/tenants` (CRUD et activation/suspension).
*   **Navigation & Routage** :
    *   Mise à jour de [AppShell.tsx](file:///c:/Users/kahonsu/Documents/GitHub/eHealth/frontend/src/components/layout/AppShell.tsx) pour introduire une section `"Administration"` contenant le lien `"Gestion des tenants"`, sécurisée par les rôles `SUPER_ADMIN` et `ADMIN_SYSTEM`.
    *   Configuration de la route `/admin/tenants` dans [App.tsx](file:///c:/Users/kahonsu/Documents/GitHub/eHealth/frontend/src/App.tsx) enveloppée par le contrôle de rôle `ProtectedRoute`.
*   **Page d'Administration** : Création de la page premium [TenantsPage.tsx](file:///c:/Users/kahonsu/Documents/GitHub/eHealth/frontend/src/pages/admin/TenantsPage.tsx) proposant un tableau complet avec filtrage par statut, moteur de recherche, cartes d'indicateurs (KPIs) globales, modals de création et modification, ainsi que des actions d'activation/suspension.

---

## Validation et Tests

*   Écriture de tests d'intégration complets dans [TenantControllerIT.java](file:///c:/Users/kahonsu/Documents/GitHub/eHealth/services/java/tenant-service/src/test/java/com/sih/tenant/controller/TenantControllerIT.java).
*   Exécution avec succès de la commande de tests Maven :
    ```powershell
    mvn clean test -pl services/java/tenant-service
    ```
    Résultat : **BUILD SUCCESS** (2 tests réussis, mockant les requêtes Keycloak durant le cycle de test).
*   Compilation réussie de l'API Gateway avec pnpm :
    ```powershell
    pnpm --filter @ehealth/api-gateway build
    ```
    Résultat : **BUILD SUCCESS** (TypeScript compilé sans erreur).
*   Compilation réussie de la librairie partagée :
    ```powershell
    mvn clean compile -pl shared/shared-tenant-lib
    ```
    Résultat : **BUILD SUCCESS** (Java compilé sans erreur).
*   Exécution des tests de `gap-service` avec la configuration de tenant :
    ```powershell
    mvn clean test -pl services/java/gap-service
    ```
    Résultat : **BUILD SUCCESS** (32 tests passés avec succès, validant la persistance avec `tenantId` et le fonctionnement transparent de `TenantFilter` avec H2).
*   Exécution des tests de `dpi-service` avec la configuration de tenant :
    ```powershell
    mvn clean test -pl services/java/dpi-service
    ```
    Résultat : **BUILD SUCCESS** (29 tests passés avec succès).
*   Exécution globale de toute la suite de tests du projet :
    ```powershell
    mvn clean test
    ```
    Résultat : **BUILD SUCCESS** (Tous les microservices compilent et valident leurs tests unitaires et d'intégration avec succès sous le nouveau modèle multi-tenant SaaS).
*   **Compilation du Frontend** :
    ```powershell
    npm run build
    ```
    Résultat : **SUCCESS** (Fichiers d'actifs statiques optimisés générés sans erreur).
