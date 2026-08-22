# Module IX — Business Intelligence et analytique

Le Module IX introduit `analytics-service`, un service d’indicateurs décisionnels tenant-isolés. Il conserve des instantanés issus des domaines cliniques, financiers et opérationnels et expose un tableau de bord consolidé sous `/api/v1/analytics/dashboard`.

| Catégorie | Exemples d’indicateurs attendus |
|---|---|
| `CLINICAL` | Réadmissions, infections, durée moyenne de séjour, mortalité |
| `FINANCIAL` | DSO, revenus, taux de recouvrement, rentabilité |
| `OPERATIONAL` | Occupation, attente, disponibilité d’équipements, couverture de services |

Chaque instantané contient une clé métier, un libellé, une valeur décimale, une unité, une date de mesure et son service source. Le tableau de bord conserve uniquement la valeur la plus récente par `metricKey` dans le tenant courant. Cette stratégie évite de confondre l’historique de mesures avec la lecture opérationnelle actuelle.

## Sécurité et isolation

`MetricSnapshot` hérite de `TenantScopedEntity`. Toutes les lectures reposent sur `TenantContext.requireCurrentTenant()` et le repository filtre par `tenantId`. Le gateway exige un tenant actif pour `/api/v1/analytics/**`, puis transmet son contexte au service.

La permission `ANALYTICS_VIEW` donne accès au menu, à la route `/analytics` et au tableau de bord pour `ANALYSTE_BI`, `RESPONSABLE_FINANCIER`, `RESPONSABLE_SUPPORT` et `SUPER_ADMIN`. La permission `ANALYTICS_METRIC_RECORD` est réservée à `ANALYSTE_BI` et `SUPER_ADMIN`. Le rôle `ANALYSTE_BI` est déclaré dans le realm Keycloak et ajouté au composite `SUPER_ADMIN`.

## Infrastructure et validation

Docker Compose ajoute `analytics-service` sur le port interne `8092`, avec contrôle de santé et `ANALYTICS_SERVICE_URL=http://analytics-service:8092` injectée au gateway. Le frontend React utilise `analytics.service.ts` et `AnalyticsDashboardPage.tsx` pour présenter les trois domaines d’indicateurs.

Les tests `AnalyticsServiceIT` valident la conservation du dernier instantané d’une même clé et l’invisibilité totale des données d’un tenant depuis un autre. Les commandes validées sont :

```bash
mvn -pl services/java/analytics-service -am test
cd frontend && npm run build && npm run lint
cd services/nodejs/api-gateway && npm run build
git diff --check
```

La pile Compose a été contrôlée structurellement. Le moteur Docker n’étant pas disponible dans l’environnement, aucun démarrage complet n’est revendiqué.
