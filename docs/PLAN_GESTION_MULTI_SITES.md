# Plan de gestion multi-sites par tenant

**Statut :** proposition d’architecture — **aucune implémentation incluse dans ce document**.

## 1. Objectif et principe directeur

Un tenant représente une organisation juridique ou un groupe de cliniques. Un **site** représente une implantation opérationnelle de cette organisation : clinique, hôpital, antenne, centre de soins ou laboratoire. Le système doit donc introduire une seconde frontière de contexte, sous le tenant, sans remettre en cause le cloisonnement actuel entre organisations.

> Toute opération quotidienne doit être exécutée dans un site actif ; les administrateurs tenant disposent d’une vue consolidée et contrôlée des sites auxquels leur rôle leur donne accès.

| Contexte | Exemple | Droits attendus |
|---|---|---|
| **Tenant** | Groupe Santé Horizon | Administration du groupe, gouvernance, statistiques consolidées et paramétrage transverse. |
| **Site** | Clinique Horizon Centre | Accueil, consultations, lits, facturation, stock, planning et files d’attente de l’implantation. |
| **Unité** | Urgences, cardiologie, laboratoire | Restriction fonctionnelle supplémentaire à l’intérieur d’un site lorsque nécessaire. |

## 2. État actuel à préserver

Le socle existant isole déjà les données par `tenantId`, porte le tenant dans le JWT, le valide au gateway et le transmet aux microservices par `X-Tenant-ID`. Les entités et requêtes sensibles sont tenant-scopées. Le plan **ne remplace pas** cette isolation : il ajoute un scope `siteId` sous le tenant.

La règle fondamentale devient :

```text
TenantContext : organisation autorisée
SiteContext   : implantation autorisée et active
Unité         : facultative, selon le module et le rôle
```

## 3. Modèle de données cible

### 3.1 Référentiel des sites

Un référentiel central, géré par `tenant-service`, doit ajouter une entité `Site`.

| Champ | Rôle |
|---|---|
| `id` / `code` | Identifiant stable et lisible du site. |
| `tenantId` | Parent obligatoire ; un site ne peut appartenir qu’à un seul tenant. |
| `name`, `legalName` | Libellés opérationnel et légal. |
| `type` | Clinique, hôpital, antenne, laboratoire, pharmacie, centre de télémédecine. |
| `address`, `timezone`, `country` | Contexte local pour opérations, fiscalité et horaires. |
| `status` | `ACTIVE`, `SUSPENDED`, `CLOSED`. |
| `settings` | Paramètres locaux : préfixe ticket, numérotation, capacité, horaires, règles de triage. |

Une contrainte unique `(tenantId, code)` doit être appliquée. La suppression physique n’est pas recommandée : un site fermé est conservé pour l’intégrité de l’historique.

### 3.2 Classification des données

| Classe | Exemples | Stratégie |
|---|---|---|
| **Tenant-global** | Abonnement, plan, référentiel de rôles, catalogue groupe, administration centrale | `tenantId` uniquement. |
| **Site-local** | Admissions, lits, tickets Smart Queue, rendez-vous, encaissements, stocks, plannings, interventions support | `tenantId` + `siteId` obligatoires. |
| **Partagé sous contrôle** | Patient, identité EMPI, dossier clinique, prescripteurs | Donnée tenant-globale avec `homeSiteId` et règles explicites d’accès inter-sites. |
| **Agrégat consolidé** | BI, volumes, recettes, qualité, délais d’attente | Lecture multi-sites uniquement après contrôle d’autorisation tenant. |

Le patient ne doit pas devenir automatiquement visible dans tous les sites. Par défaut, son identité reste tenant-globale, mais l’accès clinique et administratif doit être justifié par un rattachement au site, un épisode de soins, une référence inter-sites ou une autorisation explicite conforme aux politiques de l’établissement.

## 4. Contexte de requête et sécurité

### 4.1 Propagation sécurisée

Le frontend ne doit jamais pouvoir imposer librement un site via un en-tête. Le flux cible est le suivant :

1. L’utilisateur s’authentifie et le JWT porte son tenant ainsi que ses appartenances aux sites.
2. L’utilisateur sélectionne un site parmi ceux autorisés ; ce choix est un **contexte demandé**, non une autorité.
3. Le gateway vérifie que le site est actif, appartient au tenant du JWT et est autorisé pour l’utilisateur.
4. Le gateway injecte `X-Tenant-ID` et `X-Site-ID` vers les services internes.
5. Les services créent un `SiteContext` en parallèle du `TenantContext` et rejettent toute requête sans site lorsque l’endpoint est site-local.

| Contexte d’appel | Condition |
|---|---|
| Utilisateur opérationnel | Un seul `siteId` actif, appartenant à sa liste d’accès. |
| Responsable de site | Site(s) attribué(s), avec administration limitée à ces sites. |
| Administrateur tenant | Peut choisir un site opérationnel ou le mode consolidé selon la permission. |
| Super administrateur plateforme | Accès exceptionnel, tracé et soumis aux politiques de support. |
| Écran public Smart Queue | Compte d’affichage dédié à un site et un service, sans accès aux données identifiantes. |

### 4.2 RBAC à portée de site

Les rôles actuels doivent être étendus par une attribution de portée : `TENANT`, `SITE` ou, à terme, `UNIT`.

| Attribution | Exemple | Effet |
|---|---|---|
| `MEDECIN @ site-centre` | Médecin de la Clinique Centre | Peut consulter et appeler la file de ce site, pas celle d’un autre site. |
| `RESPONSABLE_FINANCIER @ tenant` | Direction financière du groupe | Peut consulter les agrégats de tous les sites ; les opérations restent réalisées dans un site actif. |
| `ADMIN_GAP @ site-nord` | Responsable accueil Nord | Gère admissions et tickets uniquement à Nord. |

Les permissions doivent être évaluées avec la paire `(permission, scope)`, et pas seulement avec le rôle brut. Tout refus de site, changement de site et accès consolidé doit être tracé.

## 5. Évolution par module

| Module | Données à scoper au site | Vue tenant consolidée |
|---|---|---|
| GAP / Smart Queue | Admissions, rendez-vous, lits, files, tickets, capacités | Occupation, volumes, délais, motifs d’annulation par site. |
| DPI | Épisodes, observations, prescriptions et constantes liés à un site de prise en charge | Accès uniquement selon politique clinique inter-sites ; indicateurs sans exposition inutile de détail. |
| LIS / RIS | Demandes, équipements, prélèvements, examens et résultats exécutés localement | Production, délais et charge de chaque plateau technique. |
| Pharmacie | Dépôts, lots, inventaires, dispensations et seuils | Stock consolidé, transferts inter-sites explicitement tracés. |
| RCM | Factures, caisses, claims, moyens de paiement | Recettes et créances consolidées avec ventilation par site. |
| RH / Support | Personnel affecté, gardes, actifs, interventions et nettoyage | Capacités, conformité et coûts par site. |
| BI | Snapshots et tableaux de bord | Filtres site, comparaison inter-sites et vue groupe. |

## 6. Expérience utilisateur cible

La barre applicative doit afficher un sélecteur de contexte : `Organisation > Site actif`. Les pages opérationnelles indiquent toujours le site dans leur titre, leurs formulaires et leurs exports. Un changement de site réinitialise les caches de requêtes, les filtres et les brouillons afin d’éviter toute confusion de données.

Le mode « Tous les sites » n’est visible que pour les permissions consolidées. Il s’agit d’un mode de consultation, avec filtres explicites et étiquettes de provenance ; toute commande créant ou modifiant une donnée exige ensuite de sélectionner un site précis.

## 7. Plan de mise en œuvre recommandé

| Lot | Contenu | Critères de sortie |
|---|---|---|
| **1. Fondation** | Entité `Site`, CRUD tenant-service, statut, migration et API d’appartenance utilisateur-site. | Un tenant peut créer, fermer et lister ses sites. |
| **2. Contexte et gateway** | `SiteContext`, validation gateway, JWT/groups Keycloak, `X-Site-ID`, journalisation. | Aucun appel site-local n’est accepté avec un site absent ou non autorisé. |
| **3. RBAC et UI** | Portée site des rôles, sélecteur de site, route consolidée, purge du cache au changement de site. | Un rôle site ne peut pas voir ni agir sur un autre site. |
| **4. Modules prioritaires** | GAP, Smart Queue, lits, rendez-vous, RCM, pharmacie et RH. | Création/lecture/mutation filtrées par tenant et site ; tests d’isolation. |
| **5. Modules cliniques** | DPI, LIS, RIS et consentements avec politique de partage inter-sites. | Les accès inter-sites sont explicitement autorisés, motivés et audités. |
| **6. Consolidation BI** | Agrégats multi-sites, filtres et exports contrôlés. | L’administration voit les indicateurs groupe sans contourner les données opérationnelles. |
| **7. Migration et exploitation** | Attribution d’un site par défaut, reprise historique, formation, monitoring et rollout progressif. | Réconciliation des données, rollback documenté et indicateurs de sécurité stables. |

## 8. Migration des données existantes

Le démarrage le plus sûr consiste à créer un site principal par tenant, par exemple `DEFAULT` ou `SITE-PRINCIPAL`, puis à renseigner ce site pour toutes les données historiques site-locales. Les nouvelles implantations sont ensuite créées avant l’activation de leurs opérations.

La migration doit être par service, idempotente et vérifiée par des comptes avant/après. Les colonnes `siteId` deviennent d’abord nullables pendant la reprise, puis obligatoires uniquement après contrôle de complétude. Aucun filtrage par site ne doit être activé avant que les enregistrements concernés aient reçu un site valide.

## 9. Tests, audit et indicateurs

Les tests automatisés doivent couvrir au minimum : refus d’un site appartenant à un autre tenant ; refus d’un site non attribué à l’utilisateur ; impossibilité pour un rôle site de lire ou modifier les données d’un autre site ; accès consolidé autorisé seulement avec la permission tenant ; et projection Smart Queue limitée au site configuré.

Les journaux d’audit devront inclure `tenantId`, `siteId`, utilisateur, rôle, action, ressource, décision d’autorisation et corrélation de requête. Les tableaux de bord de sécurité suivront les refus de contexte site, les accès consolidés, les changements de contexte et les actions inter-sites.

## 10. Décisions à valider avant implémentation

| Décision | Question à trancher |
|---|---|
| Identité patient | EMPI strictement tenant-global ou séparation par site avec rapprochement contrôlé ? |
| Partage clinique | Quels rôles et quelles circonstances autorisent le DPI inter-sites ? |
| Rôles | Les rôles sont-ils attribués seulement par site, ou certains peuvent-ils être tenant-globaux ? |
| Facturation | Une facture doit-elle être attachée à un seul site ou peut-elle consolider un parcours multi-sites ? |
| Exploitation | Faut-il des horaires, devises, numérotations et catalogues propres à chaque site ? |
| Migration | Quel site doit devenir le site principal de chaque tenant existant ? |

## Conclusion

Le modèle recommandé conserve le tenant comme frontière forte d’organisation et ajoute le site comme frontière opérationnelle obligatoire. Il permet à chaque clinique d’exécuter ses opérations sans interférence avec les autres implantations, tout en donnant à l’administration du tenant des capacités de supervision, de paramétrage et d’analyse consolidée, strictement tracées et limitées par permission.
