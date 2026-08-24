# Module VIII — Engagement patient et télémédecine

Le Module VIII ajoute `patient-portal-service` pour les rendez-vous et les téléconsultations. Il est exposé exclusivement via `/api/v1/portal/**` par le gateway, qui vérifie le tenant actif et propage le contexte tenant.

| Flux | Règles livrées |
|---|---|
| Rendez-vous | Création et liste tenant-isolées ; transition `REQUESTED → CONFIRMED`. |
| Téléconsultation | Création, salon sécurisé et transitions `SCHEDULED → STARTED → COMPLETED`. |
| Portail | Route React `/portal`, tableaux accessibles des rendez-vous et téléconsultations, actions filtrées par permission. |

Les permissions `PORTAL_VIEW`, `PORTAL_APPOINTMENT_CONFIRM`, `PORTAL_TELE_START` et `PORTAL_TELE_COMPLETE` sont centralisées dans `frontend/src/auth/permissions.tsx`. Le rôle `COORDINATEUR_TELEMEDECINE` est ajouté au realm Keycloak et au composite `SUPER_ADMIN`.

Docker Compose ajoute le service interne sur `8091`, son contrôle de santé et `PORTAL_SERVICE_URL=http://patient-portal-service:8091` au gateway. Le frontend reçoit `VITE_PORTAL_API_URL=/api/v1/portal`.

La compilation Maven du service, le build/lint frontend, le build gateway et `git diff --check` ont été exécutés. Le moteur Docker n’étant pas disponible, aucun démarrage Compose complet n’est revendiqué.
