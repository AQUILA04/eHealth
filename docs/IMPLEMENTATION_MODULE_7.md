# Module VII — Services de support et hôtellerie

> Le Module VII couvre la diététique, la maintenance biomédicale et technique, ainsi que le bio-nettoyage. Il est porté par `support-service`, tenant-isolé et accessible uniquement à travers le gateway.

## Périmètre fonctionnel

| Domaine | Capacités livrées | Transitions contrôlées |
|---|---|---|
| Diététique | Commandes de repas par patient, régime, type de repas, lit et date prévue | `REQUESTED → PREPARED → DELIVERED` |
| Maintenance | Référentiel d’équipements et interventions préventives ou correctives | La création d’une intervention place l’équipement en `MAINTENANCE`; sa clôture le rétablit à `OPERATIONAL`. |
| Bio-nettoyage | Tâches par lit, unité et type de nettoyage | `REQUESTED → COMPLETED` avec horodatage de clôture |

Les transitions sont validées côté serveur. Ainsi, un repas ne peut être livré sans préparation préalable, une intervention déjà clôturée ne peut être clôturée de nouveau, et une tâche de nettoyage ne peut être finalisée qu’une fois.

## Architecture et tenant

Le microservice Spring Boot `services/java/support-service` écoute exclusivement sur le port interne `8090`. Le gateway route `/api/v1/support/**`, exige un tenant actif du jeton et propage `X-Tenant-ID`. Les quatre agrégats (`MealOrder`, `Equipment`, `MaintenanceWorkOrder`, `CleaningTask`) héritent de `TenantScopedEntity`; toutes les lectures et opérations par identifiant sont filtrées par `tenantId`.

| Route | Fonction |
|---|---|
| `GET, POST /api/v1/support/meals` | Liste et commande de repas |
| `POST /api/v1/support/meals/{id}/prepare` | Préparation du plateau |
| `POST /api/v1/support/meals/{id}/deliver` | Traçabilité de livraison |
| `GET, POST /api/v1/support/equipment` | Parc technique tenant-isolé |
| `GET, POST /api/v1/support/maintenance` | Liste et création d’interventions |
| `POST /api/v1/support/maintenance/{id}/complete` | Clôture d’intervention et remise en service |
| `GET, POST /api/v1/support/cleaning` | Liste et création de tâches de nettoyage |
| `POST /api/v1/support/cleaning/{id}/complete` | Validation du bio-nettoyage |

## RBAC

La page `/support`, son menu et toutes les actions sont protégés par le référentiel `frontend/src/auth/permissions.tsx`. Le backend applique les mêmes rôles avec `SecureSecurityConfig` lorsque le profil `secure` est actif.

| Permission | Rôles autorisés |
|---|---|
| `SUPPORT_VIEW` | `DIETETICIEN`, `TECHNICIEN_MAINTENANCE`, `AGENT_ENTRETIEN`, `RESPONSABLE_SUPPORT`, `SUPER_ADMIN` |
| `SUPPORT_MEAL_MANAGE` | `DIETETICIEN`, `RESPONSABLE_SUPPORT`, `SUPER_ADMIN` |
| `SUPPORT_MAINTENANCE_MANAGE` | `TECHNICIEN_MAINTENANCE`, `RESPONSABLE_SUPPORT`, `SUPER_ADMIN` |
| `SUPPORT_CLEANING_MANAGE` | `AGENT_ENTRETIEN`, `RESPONSABLE_SUPPORT`, `SUPER_ADMIN` |

Les rôles sont déclarés dans le realm Keycloak de développement et ajoutés au composite `SUPER_ADMIN`.

## Interface

`frontend/src/pages/support/SupportOperationsPage.tsx` fournit les indicateurs opérationnels et trois espaces de travail : distribution de repas, maintenance et bio-nettoyage. Les boutons « Préparer », « Livrer », « Clôturer » et « Valider » ne sont rendus qu’avec la permission métier correspondante. Le backend reste l’autorité de sécurité.

## Infrastructure et validation

Docker Compose déclare `support-service` sur le réseau privé, avec contrôle de santé sur `/actuator/health`; le gateway reçoit `SUPPORT_SERVICE_URL=http://support-service:8090` et attend le service avant son démarrage. La variable `VITE_SUPPORT_API_URL=/api/v1/support` est ajoutée au build frontend.

Les tests `SupportWorkflowServiceIT` vérifient le cycle de livraison d’un repas et l’impossibilité d’utiliser un équipement d’un autre tenant. Les contrôles exécutés sont :

```bash
mvn -pl services/java/support-service -am test
cd frontend && npm run build && npm run lint
cd services/nodejs/api-gateway && npm run build
git diff --check
```

La configuration Compose a été vérifiée de façon structurelle. Le moteur Docker n’étant pas disponible dans l’environnement de validation, aucun démarrage complet de la pile n’est revendiqué.
