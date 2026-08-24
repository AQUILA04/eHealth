# Module VI — Ressources humaines et gestion du personnel

> **Portée.** Le Module VI centralise les dossiers de personnel, les accréditations professionnelles et la planification des gardes. Il est conçu comme un domaine métier séparé, tenant-isolé et accessible exclusivement via l’API Gateway.

## 1. Vue d’ensemble

Le module est fourni par le microservice Spring Boot `hr-service`, situé dans `services/java/hr-service`. Il couvre l’enregistrement des collaborateurs, le suivi des habilitations et la construction des plannings opérationnels. Son objectif de sécurité est double : empêcher le mélange de données entre établissements et empêcher qu’un personnel clinique dont l’accréditation est expirée soit affecté à une garde.

| Élément | Implémentation |
|---|---|
| Microservice | `hr-service` Spring Boot 3 / Java 21 |
| Port interne | `8089` |
| Préfixe public | `/api/v1/hr` via `api-gateway` |
| Persistance | JPA ; H2 avec profil `mock`, PostgreSQL en cible |
| Cloisonnement | `TenantScopedEntity`, `TenantContext` et recherches `...AndTenantId` |
| Interface | Route React protégée `/hr` |

## 2. Modèle métier

Le dossier du collaborateur (`StaffMember`) porte un matricule unique dans un tenant, l’identité, le service, la fonction, le statut d’emploi, la date d’embauche et l’indicateur `clinicalStaff`. Les accréditations (`Credential`) sont rattachées à un collaborateur et portent leur type, numéro, date de délivrance et date d’expiration. Les gardes (`ShiftAssignment`) conservent l’unité, le type de garde, le créneau et un statut de publication.

| Entité | Responsabilité | Règle d’intégrité principale |
|---|---|---|
| `StaffMember` | Référentiel du personnel | `(tenantId, employeeNumber)` est unique. |
| `Credential` | Licence, inscription, habilitation ou certificat | L’expiration doit être postérieure à la délivrance. |
| `ShiftAssignment` | Garde ou astreinte affectée à une unité | La fin doit être postérieure au début et le créneau ne peut pas chevaucher une autre garde du même collaborateur. |

## 3. Règles de gestion et de conformité

La création d’une garde vérifie que le collaborateur appartient au tenant courant et que son emploi est actif. Pour un collaborateur marqué comme clinique, le service recherche toute accréditation expirée. Si une habilitation est expirée, l’affectation est refusée avec un statut HTTP `409 Conflict`. Cette règle s’applique côté serveur et ne dépend donc pas de l’interface utilisateur.

La publication n’est autorisée que pour une garde au statut `DRAFT`. Une garde peut ensuite être annulée ; la garde n’est pas supprimée afin de préserver la trace de planification. Un renouvellement doit être à une date strictement future. Les contrôles de chevauchement comparent le créneau demandé avec les créneaux déjà enregistrés pour le même collaborateur du tenant courant.

## 4. API REST

Toutes les routes nécessitent un tenant actif, résolu depuis le jeton par le gateway. Les réponses par identifiant sont systématiquement vérifiées dans le tenant courant.

| Méthode | Route | Usage | Autorisations serveur (profil `secure`) |
|---|---|---|---|
| `GET` | `/api/v1/hr/staff` | Lister le personnel du tenant | `RH_MANAGER`, `PLANNING_RH`, `SUPER_ADMIN` |
| `POST` | `/api/v1/hr/staff` | Créer un collaborateur | `RH_MANAGER`, `SUPER_ADMIN` |
| `GET` | `/api/v1/hr/credentials` | Lister les habilitations | `RH_MANAGER`, `PLANNING_RH`, `SUPER_ADMIN` |
| `POST` | `/api/v1/hr/credentials` | Enregistrer une habilitation | `RH_MANAGER`, `SUPER_ADMIN` |
| `POST` | `/api/v1/hr/credentials/{id}/renew` | Renouveler une habilitation | `RH_MANAGER`, `SUPER_ADMIN` |
| `GET` | `/api/v1/hr/shifts` | Lister les gardes | `RH_MANAGER`, `PLANNING_RH`, `SUPER_ADMIN` |
| `POST` | `/api/v1/hr/shifts` | Créer une garde brouillon | `RH_MANAGER`, `PLANNING_RH`, `SUPER_ADMIN` |
| `POST` | `/api/v1/hr/shifts/{id}/publish` | Publier une garde | `RH_MANAGER`, `PLANNING_RH`, `SUPER_ADMIN` |
| `POST` | `/api/v1/hr/shifts/{id}/cancel` | Annuler une garde | `RH_MANAGER`, `PLANNING_RH`, `SUPER_ADMIN` |

## 5. Isolation multi-tenant

Le gateway extrait `tenant_id` — ou `tenant` selon le jeton — et refuse les requêtes RH en l’absence d’un tenant actif. Après vérification auprès de `tenant-service`, il propage `X-Tenant-ID` vers `hr-service`. Les entités racines RH héritent de `TenantScopedEntity` et le service appelle `TenantContext.requireCurrentTenant()` avant chaque lecture ou mutation.

Cela garantit notamment qu’un identifiant de collaborateur connu dans un autre établissement ne peut pas être affecté à une garde, renouvelé ou consulté. Les repositories exposent des méthodes `findByIdAndTenantId` et les listes sont filtrées par `tenantId`.

## 6. Matrice RBAC

Les permissions frontend sont centralisées dans `frontend/src/auth/permissions.tsx`, conformément à `docs/FRONTEND_RBAC.md`. Elles masquent les menus, la route, les boutons d’action et les modales, tandis que le backend conserve l’autorité définitive avec `SecureSecurityConfig`.

| Permission frontend | RH Manager | Planning RH | Super admin | Effet visible |
|---|---:|---:|---:|---|
| `HR_VIEW` | Oui | Oui | Oui | Menu et route `/hr` |
| `HR_STAFF_CREATE` | Oui | Non | Oui | Bouton et modale de création du personnel |
| `HR_CREDENTIAL_CREATE` | Oui | Non | Oui | Ajout d’une habilitation |
| `HR_CREDENTIAL_RENEW` | Oui | Non | Oui | Renouvellement d’une habilitation expirée |
| `HR_SHIFT_CREATE` | Oui | Oui | Oui | Planification d’une garde |
| `HR_SHIFT_PUBLISH` | Oui | Oui | Oui | Publication d’une garde brouillon |
| `HR_SHIFT_CANCEL` | Oui | Oui | Oui | Annulation d’une garde |

Le realm Keycloak de développement déclare les rôles `RH_MANAGER` et `PLANNING_RH`; ils sont inclus dans le composite `SUPER_ADMIN`.

## 7. Interface utilisateur

La page `frontend/src/pages/hr/HumanResourcesPage.tsx` constitue le poste RH. Elle propose une synthèse des collaborateurs actifs, du personnel clinique, des habilitations à surveiller et des gardes publiées. Les trois espaces principaux affichent le répertoire du personnel, le registre des accréditations et le planning.

Chaque formulaire comporte des libellés explicites, des contrôles de saisie et un retour d’échec contextualisé. Les actions de renouvellement, publication et annulation sont rendues seulement lorsque la permission correspondante est accordée. Cette prévention côté interface améliore la confidentialité opérationnelle mais ne remplace jamais le contrôle backend.

## 8. Infrastructure

`infrastructure/docker/docker-compose.dev.yml` ajoute `hr-service` sur le réseau privé `ehealth-network`, sans publication de port vers l’hôte. Le service reçoit son JWK Keycloak, s’expose uniquement sur `8089` au sein du réseau et possède un contrôle de santé `/actuator/health`. Le gateway reçoit `HR_SERVICE_URL=http://hr-service:8089` et attend le service RH avant son démarrage.

Le gateway route `/api/v1/hr/**` vers ce service et l’inclut dans la vérification de tenant actif. La variable de build `VITE_HR_API_URL=/api/v1/hr` est également déclarée avec les autres routes de domaines métier.

## 9. Validation

Les tests d’intégration `RcmWorkflowServiceIT` ne concernent pas ce module. Le Module VI est validé par `HrWorkflowServiceIT`, qui couvre les deux scénarios suivants :

| Scénario | Contrôle exercé |
|---|---|
| Accréditation expirée | Une garde clinique est refusée, puis acceptée après renouvellement de l’habilitation. |
| Isolation inter-tenant | Un collaborateur créé pour `hospital-a` est invisible et inutilisable depuis `hospital-b`. |

Les commandes de vérification sont les suivantes :

```bash
mvn -pl services/java/hr-service -am test
cd frontend && npm run build && npm run lint
cd services/nodejs/api-gateway && npm run build
python3 -m json.tool infrastructure/keycloak/realms/ehealth-realm.json > /dev/null
git diff --check
```

La structure de Docker Compose a été vérifiée statiquement avec une lecture YAML. Le moteur Docker n’étant pas disponible dans l’environnement de validation, le démarrage complet de la pile n’est pas revendiqué dans cette documentation.

## 10. Extensions recommandées

Le modèle est prêt à accueillir l’historique des affectations, les demandes de congés, les règles de couverture par unité, les absences et un rapprochement avec les identités Keycloak. Toute extension doit préserver le filtrage par tenant, ajouter une permission frontend au référentiel central et appliquer une règle backend équivalente.
