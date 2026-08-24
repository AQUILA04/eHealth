# Module V — Gestion financière et cycle de revenus (RCM)

## Objet et périmètre

Le Module V introduit un service de **Revenue Cycle Management** tenant-isolé, couvrant la facture patient, l’encaissement, le tiers payant et le suivi d’un dossier assureur. Il ne remplace pas un ERP comptable général : il fournit le cycle opérationnel hospitalier allant de l’acte facturable jusqu’à l’instruction du remboursement.

| Domaine | Livrable | Responsabilité |
|---|---|---|
| Facturation | `rcm-service` | Création et émission de factures à partir de lignes d’actes. |
| Caisse | Encaissements | Contrôle du solde restant et historique de paiement. |
| Tiers payant | Dossiers assureur | Création, transmission et adjudication des demandes de remboursement. |
| Interface | `/rcm` | Poste de travail financier avec actions filtrées par permission. |

## Modèle métier

Une facture est créée au statut `DRAFT`, puis émise au statut `ISSUED`. Les paiements font évoluer son statut vers `PARTIALLY_PAID` ou `PAID`. La création conserve la ventilation entre la part assureur et la part patient; le montant assuré est déterminé à partir du taux de couverture fourni lors de la création.

| Objet | Champs métier essentiels | Règle de cohérence |
|---|---|---|
| `Invoice` | Patient, séjour clinique, payeur, devise, total, ventilation et solde | Le numéro de facture est unique par tenant. |
| `InvoiceLine` | Code d’acte, libellé, quantité, prix unitaire et total de ligne | Le total de facture est la somme des lignes. |
| `Payment` | Facture, montant, mode, référence et caissier | Le montant ne peut pas dépasser le solde ouvert. |
| `InsuranceClaim` | Facture, assureur, police, demandé, accepté et statut | Le montant accepté ne peut pas dépasser le montant réclamé. |

> Un dossier assureur ne peut être créé qu’après émission d’une facture comportant une part assureur. Un dossier ne peut être transmis qu’une fois, puis instruit seulement au statut `SUBMITTED`.

## API REST

Le service est exposé derrière l’API Gateway sous le préfixe `/api/v1/rcm`. Les services backend ne sont pas exposés directement vers l’extérieur dans Docker.

| Méthode | Endpoint | Action |
|---|---|---|
| `GET` | `/api/v1/rcm/invoices` | Lister les factures du tenant courant. |
| `POST` | `/api/v1/rcm/invoices` | Créer une facture brouillon. |
| `POST` | `/api/v1/rcm/invoices/{id}/issue` | Émettre une facture. |
| `GET` | `/api/v1/rcm/invoices/{id}/payments` | Lister les encaissements d’une facture. |
| `POST` | `/api/v1/rcm/invoices/{id}/payments` | Enregistrer un encaissement. |
| `GET` | `/api/v1/rcm/claims` | Lister les dossiers assureur du tenant. |
| `POST` | `/api/v1/rcm/claims` | Créer un dossier de tiers payant. |
| `POST` | `/api/v1/rcm/claims/{id}/submit` | Transmettre le dossier à l’assureur. |
| `POST` | `/api/v1/rcm/claims/{id}/adjudicate` | Accepter partiellement, accepter ou refuser un dossier. |

## Isolation multi-tenant

Toutes les entités racines (`Invoice`, `Payment`, `InsuranceClaim`) étendent `TenantScopedEntity`. Le tenant est affecté à la persistance depuis `TenantContext` et jamais depuis un DTO fourni par le client. Les lectures et les accès par identifiant utilisent systématiquement le tenant courant (`findBy...AndTenantId`).

L’API Gateway déduit le claim JWT `tenant_id`, vérifie que le tenant est actif auprès de `tenant-service`, puis propage uniquement l’en-tête `X-Tenant-ID` vers `rcm-service`. Toute requête financière sans tenant valide est refusée avant d’atteindre le service.

## RBAC

Les permissions frontend sont centralisées dans `frontend/src/auth/permissions.tsx`; le backend applique la même matrice dans `SecureSecurityConfig`.

| Permission | Rôles autorisés |
|---|---|
| `RCM_VIEW` | `COMPTABLE`, `CAISSIER`, `FACTURATION`, `RESPONSABLE_FINANCIER`, `SUPER_ADMIN` |
| `RCM_INVOICE_CREATE` | `FACTURATION`, `RESPONSABLE_FINANCIER`, `SUPER_ADMIN` |
| `RCM_INVOICE_ISSUE` | `CAISSIER`, `RESPONSABLE_FINANCIER`, `SUPER_ADMIN` |
| `RCM_PAYMENT_RECORD` | `CAISSIER`, `RESPONSABLE_FINANCIER`, `SUPER_ADMIN` |
| `RCM_CLAIM_CREATE` | `FACTURATION`, `RESPONSABLE_FINANCIER`, `SUPER_ADMIN` |
| `RCM_CLAIM_SUBMIT` | `FACTURATION`, `RESPONSABLE_FINANCIER`, `SUPER_ADMIN` |
| `RCM_CLAIM_ADJUDICATE` | `FACTURATION`, `RESPONSABLE_FINANCIER`, `SUPER_ADMIN` |

Le menu, la route `/rcm`, les boutons d’émission, d’encaissement et de tiers payant, ainsi que les modales correspondantes, sont protégés par `PermissionRoute` et `Can`. Un utilisateur n’aperçoit donc pas les commandes correspondant à une mutation qu’il n’a pas le droit d’effectuer. Le backend reste le contrôle d’autorisation final.

## Infrastructure locale

| Élément | Configuration |
|---|---|
| Service | `services/java/rcm-service` |
| Port interne | `8088` |
| Profil Docker | `secure` |
| Gateway | `RCM_SERVICE_URL=http://rcm-service:8088` |
| Frontend | Route `/rcm`, client `frontend/src/services/rcm.service.ts` |
| Realm Keycloak | Rôles `CAISSIER`, `FACTURATION` et `RESPONSABLE_FINANCIER` ajoutés au realm et au composite `SUPER_ADMIN`. |

## Validation

Les scénarios d’intégration du service couvrent le calcul de part assureur, l’émission de facture, la transmission puis l’adjudication d’un dossier assureur, l’encaissement et la transition vers `PAID`. Un second scénario démontre qu’un tenant ne peut ni lister ni émettre une facture appartenant à un autre tenant.

```bash
# Backend RCM et bibliothèque tenant
mvn -pl services/java/rcm-service -am test

# Frontend
cd frontend
npm run build
npm run lint

# Gateway
cd ../services/nodejs/api-gateway
npm run build
```

La configuration Docker peut être démarrée avec la commande existante :

```bash
docker compose -f infrastructure/docker/docker-compose.dev.yml up --build
```

Le moteur Docker n’était pas disponible dans l’environnement de validation automatisé; la structure YAML, les dépendances de services, le port interne et les variables gateway/frontend ont donc été vérifiés de manière statique.
