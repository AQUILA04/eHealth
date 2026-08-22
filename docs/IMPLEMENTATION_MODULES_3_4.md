# Implémentation des Modules III et IV

**Auteur :** Manus AI  
**Périmètre :** Module III — Plateaux techniques et services diagnostiques ; Module IV — Pharmacie et chaîne logistique.

## Vue d’ensemble

Cette implémentation complète les fondations GAP et DPI du SIH eHealth par trois services Spring Boot autonomes : le **LIS** pour le laboratoire et la banque de sang, le **RIS** pour la radiologie, et le service **Pharmacie** pour la gestion des produits, des lots et de la dispensation. Chaque service utilise son propre modèle JPA et une base H2 temporaire sous le profil `mock`. Les profils sécurisés conservent l’intégration OAuth2/JWT avec les rôles Keycloak existants.

| Domaine | Service | Port local | Rôle principal |
|---|---:|---:|---|
| Laboratoire et banque de sang | `lis-service` | `8084` | Gérer les échantillons, les résultats, la validation biologique et la traçabilité transfusionnelle. |
| Radiologie | `ris-service` | `8085` | Orchestrer le cycle de l’examen d’imagerie, la dosimétrie et le compte-rendu. |
| Pharmacie et stocks | `pharmacy-service` | `8086` | Gérer le catalogue, les lots, les seuils de stock et la dispensation FEFO. |

## Module III — Plateaux techniques

### LIS — Laboratoire

Le LIS couvre le flux pré-analytique, analytique et post-analytique. Une demande comporte une référence patient, un dossier clinique, un examen, un type d’échantillon, une priorité et un code-barres unique. Le parcours impose les transitions **prescrit → prélevé → reçu → en analyse → validé biologiquement**. Un résultat critique ne peut être notifié qu’après l’enregistrement d’au moins une valeur `CRITICAL_LOW` ou `CRITICAL_HIGH`.

| Endpoint | Méthode | Usage |
|---|---|---|
| `/api/v1/lis/orders` | `POST`, `GET` | Créer et consulter les demandes de laboratoire. |
| `/api/v1/lis/orders/{id}/collect` | `PATCH` | Tracer le prélèvement. |
| `/api/v1/lis/orders/{id}/receive` | `PATCH` | Tracer la réception du spécimen. |
| `/api/v1/lis/orders/{id}/results` | `POST` | Saisir un résultat analytique. |
| `/api/v1/lis/orders/{id}/validate` | `PATCH` | Valider biologiquement une demande comportant des résultats. |
| `/api/v1/lis/orders/{id}/critical-notification` | `POST` | Tracer l’alerte au prescripteur pour une valeur critique. |

### RIS — Radiologie

Le RIS assure la création et la planification des examens par modalité (`XR`, `CT`, `MRI`, `US`, `NM`, `MAMMO`). Il conserve l’UID PACS, la dose en mGy, les responsabilités de manipulation et de radiologie, ainsi que le texte du compte-rendu. Une étude doit être planifiée ou accueillie avant d’être réalisée, puis réalisée avant validation du compte-rendu.

| Endpoint | Méthode | Usage |
|---|---|---|
| `/api/v1/ris/studies` | `POST`, `GET` | Créer et consulter les examens radiologiques. |
| `/api/v1/ris/studies/{id}/schedule` | `PATCH` | Planifier la modalité et le manipulateur. |
| `/api/v1/ris/studies/{id}/check-in` | `PATCH` | Confirmer l’arrivée du patient. |
| `/api/v1/ris/studies/{id}/perform` | `PATCH` | Tracer la réalisation, l’UID PACS et la dosimétrie. |
| `/api/v1/ris/studies/{id}/report` | `PATCH` | Enregistrer et valider le compte-rendu du radiologue. |

### Banque de sang

La Banque de sang est intégrée au LIS. Elle enregistre les poches par composant, groupe ABO, Rhésus, date de collecte, date de péremption et emplacement de stockage. Lors d’une demande, le service réserve automatiquement une poche compatible à échéance la plus proche. Le système verrouille la délivrance tant que le cross-match n’est pas validé. La réalisation de la transfusion et les réactions éventuelles sont également tracées.

> **Verrou de sécurité :** une demande transfusionnelle ne peut être délivrée qu’après validation de compatibilité. Les règles ABO/Rh sont appliquées avant la réservation et contrôlées à nouveau lors du cross-match.

| Endpoint | Méthode | Usage |
|---|---|---|
| `/api/v1/lis/blood-bank/units` | `POST`, `GET` | Réceptionner et consulter les poches. |
| `/api/v1/lis/blood-bank/transfusions` | `POST`, `GET` | Créer et consulter les demandes transfusionnelles. |
| `/api/v1/lis/blood-bank/transfusions/{id}/crossmatch` | `PATCH` | Valider la compatibilité. |
| `/api/v1/lis/blood-bank/transfusions/{id}/issue` | `PATCH` | Délivrer une poche compatible. |
| `/api/v1/lis/blood-bank/transfusions/{id}/complete` | `PATCH` | Clôturer la transfusion. |
| `/api/v1/lis/blood-bank/transfusions/{id}/reaction` | `POST` | Déclarer un incident transfusionnel. |

## Module IV — Pharmacie et chaîne logistique

Le module gère le catalogue des médicaments, le stock physique par lot, les dates de péremption, les seuils minimums et la dispensation. La validation pharmaceutique confirme que le stock total non périmé couvre la demande. La délivrance choisit automatiquement le premier lot non expiré ayant une quantité suffisante, selon la règle **FEFO** (*First Expiry, First Out*), puis décrémente son stock.

| Endpoint | Méthode | Usage |
|---|---|---|
| `/api/v1/pharmacy/products` | `POST`, `GET` | Gérer le catalogue de produits et visualiser les alertes de seuil. |
| `/api/v1/pharmacy/inventory/receipts` | `POST` | Réceptionner un lot avec quantité, péremption et emplacement. |
| `/api/v1/pharmacy/inventory/lots` | `GET` | Consulter les lots dans l’ordre FEFO. |
| `/api/v1/pharmacy/dispensations` | `POST`, `GET` | Valider et consulter les dispensations. |
| `/api/v1/pharmacy/dispensations/{id}/dispense` | `PATCH` | Délivrer depuis un lot éligible et décrémenter le stock. |

## Interface utilisateur

L’interface React ajoute quatre postes de travail accessibles depuis la navigation latérale. Les écrans utilisent les composants et tokens existants, sont typés, et raccordés aux nouveaux clients Axios via les routes `/api/v1/lis`, `/api/v1/ris` et `/api/v1/pharmacy`.

| Écran | Route | Fonctions couvertes |
|---|---|---|
| Poste laboratoire | `/lis/worklist` | File des demandes, collecte, réception, résultat, validation et alertes critiques. |
| Poste radiologie | `/ris/worklist` | Demande, planification, accueil, réalisation et compte-rendu. |
| Banque de sang | `/lis/blood-bank` | Stock de poches, compatibilité, délivrance, clôture et incidents. |
| Pharmacie | `/pharmacy` | Catalogue, réception de lots, alertes de stock, validation et délivrance. |

## Exécution locale

Les trois services sont intégrés à `infrastructure/docker/docker-compose.dev.yml`. Le frontend Nginx et le serveur Vite possèdent les proxys correspondants. Les services démarrent sur les ports 8084 à 8086, avec le même realm Keycloak que les modules existants.

```bash
# Tests backend des modules III et IV
mvn -pl services/java/lis-service,services/java/ris-service,services/java/pharmacy-service -am test

# Build et contrôle qualité du frontend
cd frontend
npm run build
npm run lint

# Pile complète de développement
docker compose -f infrastructure/docker/docker-compose.dev.yml up --build
```

## Couverture de test

Des tests d’intégration `MockMvc` couvrent un cycle complet pour chacun des flux critiques : laboratoire avec valeur critique, radiologie avec compte-rendu, banque de sang avec compatibilité et délivrance, ainsi que pharmacie avec réception de lot, validation et dispensation FEFO.

> L’environnement `mock` emploie H2 en mémoire. Il est destiné au développement et aux tests ; les déploiements sécurisés doivent utiliser les configurations de persistance et d’exploitation prévues pour l’environnement cible.


## Intégration multi-tenant

Les modules III et IV sont intégrés au socle multi-tenant introduit par le `tenant-service` et `shared-tenant-lib`. Le tenant n’est jamais reçu depuis les DTO métier. Il est résolu à partir du claim JWT `tenant_id` par l’API Gateway, qui vérifie que le tenant est actif puis propage uniquement l’en-tête interne `X-Tenant-ID` aux services en aval.

| Couche | Mécanisme appliqué |
|---|---|
| Keycloak | Le client `ehealth-frontend` publie l’attribut utilisateur `tenant_id` dans les jetons d’accès, d’identité et UserInfo. |
| Gateway | Les routes `/api/v1/lis`, `/api/v1/ris` et `/api/v1/pharmacy` exigent un tenant actif et sont transmises avec `X-Tenant-ID`. |
| Services Java | Chaque service dépend de `shared-tenant-lib`; le `TenantContext` est utilisé de façon obligatoire sur les lectures et accès par identifiant. |
| Persistance | Toutes les entités LIS, RIS et Pharmacie étendent `TenantScopedEntity`, qui renseigne `tenantId` à la création; les contraintes métier et index sont tenant-scopés. |
| Infrastructure | LIS, RIS et Pharmacie ne publient aucun port hôte dans Docker Compose et sont joignables uniquement par le gateway. |

> **Garantie applicative :** toute lecture, sélection FEFO, réservation de poche, dispensation ou transition de workflow est filtrée par le tenant courant. Un identifiant connu d’un autre tenant produit une réponse « introuvable » plutôt qu’un accès aux données.

Les tests d’intégration `LisTenantIsolationIT`, `RisTenantIsolationIT` et `PharmacyTenantIsolationIT` démontrent l’absence de visibilité croisée et autorisent les identifiants métier identiques lorsqu’ils appartiennent à des tenants distincts.
