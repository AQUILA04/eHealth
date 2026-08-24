# Guide technique — Extension du RBAC frontend

> **Objet.** Ce guide décrit la méthode obligatoire pour ajouter des permissions frontend aux futurs modules eHealth. Son objectif est qu’un utilisateur connecté ne voie que les menus, les routes, les actions et les données correspondant à ses rôles métier et à son tenant.

## 1. Principes d’architecture

Le contrôle d’accès de l’application est appliqué en **défense en profondeur**. Le frontend améliore la confidentialité et l’ergonomie en empêchant l’affichage d’éléments non autorisés. Le backend reste néanmoins l’unique autorité de sécurité : il valide le jeton JWT, le rôle et le tenant à chaque appel API.

| Couche | Responsabilité | Emplacement principal |
|---|---|---|
| Authentification | Lecture de la session Keycloak et des rôles du jeton | `frontend/src/auth/KeycloakProvider.tsx` |
| Référentiel de permissions | Association d’une permission métier avec les rôles autorisés | `frontend/src/auth/permissions.tsx` |
| Navigation | Masquage des menus non autorisés | `frontend/src/components/layout/AppShell.tsx` |
| Routage | Interdiction d’un accès direct par URL | `frontend/src/App.tsx` |
| Écrans et composants | Masquage des boutons, modales et actions unitaires | Pages métier sous `frontend/src/pages/` |
| API backend | Contrôle RBAC, validation du tenant et filtrage des données | Services Java et API Gateway |

> **Règle fondamentale :** ne jamais considérer un masquage frontend comme une protection suffisante. Une permission frontend doit toujours correspondre à une règle d’autorisation dans le service backend concerné.

## 2. Modèle actuel

Le référentiel `permissions.tsx` déclare une constante `PERMISSIONS`, puis infère automatiquement le type TypeScript `Permission`. La table `rolePermissions` associe ensuite chaque permission aux rôles Keycloak admis. `usePermissions()` expose les fonctions `can`, `canAny` et `canAll`; le composant `Can` rend ses enfants uniquement lorsque la permission est accordée.

```tsx
import { Can, PERMISSIONS, usePermissions } from '@/auth/permissions'

function Example() {
  const { can } = usePermissions()

  return (
    <>
      {can(PERMISSIONS.LIS_WORKLIST_VIEW) && <p>Accès à la liste LIS</p>}
      <Can permission={PERMISSIONS.LIS_RESULT_VALIDATE}>
        <button>Valider biologiquement</button>
      </Can>
    </>
  )
}
```

| Primitive | Usage recommandé |
|---|---|
| `Can` | Masquer un bouton, une modale, une colonne d’action ou un bloc JSX. |
| `can(permission)` | Brancher un comportement, éviter une requête secondaire, ou choisir un rendu alternatif. |
| `canAny(...permissions)` | Autoriser une zone commune à plusieurs capacités métier. |
| `canAll(...permissions)` | Réserver une action qui nécessite cumulativement plusieurs capacités. À utiliser avec parcimonie. |
| `PermissionRoute` | Protéger une page complète contre les accès directs par URL. |

## 3. Procédure obligatoire pour un nouveau module

### Étape 1 — Définir la matrice métier côté backend

Avant tout changement frontend, documentez les opérations métier du module et les rôles autorisés. Implémentez les mêmes règles dans la configuration de sécurité du service backend. Une permission est une **capacité métier atomique**, et non le nom d’un écran.

Par exemple, pour un futur module de rendez-vous :

| Permission proposée | Intention | Rôles autorisés, à confirmer côté backend |
|---|---|---|
| `APPOINTMENTS_VIEW` | Consulter les rendez-vous accessibles au tenant | `MEDECIN`, `INFIRMIER`, `ADMIN_GAP`, `SUPER_ADMIN` |
| `APPOINTMENTS_CREATE` | Créer un rendez-vous | `ADMIN_GAP`, `SUPER_ADMIN` |
| `APPOINTMENTS_RESCHEDULE` | Replanifier un rendez-vous | `ADMIN_GAP`, `SUPER_ADMIN` |
| `APPOINTMENTS_CANCEL` | Annuler un rendez-vous | `ADMIN_GAP`, `SUPER_ADMIN` |

Le service doit aussi filtrer les lectures par tenant. Le frontend ne doit jamais transmettre un identifiant de tenant librement choisi par l’utilisateur : l’API Gateway et le JWT fournissent ce contexte.

### Étape 2 — Ajouter la permission au référentiel frontend

Modifiez `frontend/src/auth/permissions.tsx` à deux endroits. Ajoutez d’abord la clé à `PERMISSIONS`, puis ajoutez obligatoirement la règle correspondante dans `rolePermissions`. Le typage `Record<Permission, readonly string[]>` force TypeScript à signaler toute permission déclarée sans matrice de rôles.

```tsx
export const PERMISSIONS = {
  // Permissions existantes…
  APPOINTMENTS_VIEW: 'APPOINTMENTS_VIEW',
  APPOINTMENTS_CREATE: 'APPOINTMENTS_CREATE',
} as const

const rolePermissions: Record<Permission, readonly string[]> = {
  // Règles existantes…
  APPOINTMENTS_VIEW: ['MEDECIN', 'INFIRMIER', 'ADMIN_GAP', 'SUPER_ADMIN'],
  APPOINTMENTS_CREATE: ['ADMIN_GAP', 'SUPER_ADMIN'],
}
```

Conservez le format `DOMAINE_ACTION`, en majuscules. Préférez des verbes explicites tels que `VIEW`, `CREATE`, `UPDATE`, `VALIDATE`, `ISSUE`, `CANCEL` ou `EXPORT`. Ne créez pas une permission générique comme `APPOINTMENTS_ACCESS` lorsqu’un écran propose plusieurs opérations soumises à des rôles différents.

### Étape 3 — Protéger la route

Ajoutez ou réutilisez une route protégée dans `frontend/src/App.tsx`. Toute page métier disposant de données sensibles doit être protégée même si son entrée de menu est masquée.

```tsx
<Route
  path="/appointments"
  element={
    <PermissionRoute permission={PERMISSIONS.APPOINTMENTS_VIEW}>
      <AppointmentsPage />
    </PermissionRoute>
  }
/>
```

`PermissionRoute` redirige l’utilisateur non autorisé vers `/dashboard`. Ne vous fiez jamais au seul filtrage de navigation : un utilisateur peut saisir une URL manuellement.

### Étape 4 — Protéger le menu

Dans `frontend/src/components/layout/AppShell.tsx`, déclarez l’entrée de navigation avec la même permission de consultation que la route.

```tsx
{
  label: 'Rendez-vous',
  to: '/appointments',
  icon: <CalendarDays className="h-4 w-4" />,
  permission: PERMISSIONS.APPOINTMENTS_VIEW,
}
```

Le filtrage de `AppShell` utilise `usePermissions().can`. Une entrée sans `permission` ni `roles` est visible par tous les utilisateurs authentifiés : cela ne convient qu’aux espaces explicitement communs, comme le tableau de bord.

### Étape 5 — Protéger toutes les actions de l’écran

Entourez chaque bouton, lien d’action, menu contextuel, modale et zone d’édition d’un composant `Can`. La permission de consultation de la page ne suffit jamais à autoriser une mutation.

```tsx
<Can permission={PERMISSIONS.APPOINTMENTS_CREATE}>
  <Button onClick={() => setCreateModalOpen(true)}>Nouveau rendez-vous</Button>
</Can>

<Can permission={PERMISSIONS.APPOINTMENTS_CANCEL}>
  <Button variant="danger" onClick={() => cancelMutation.mutate(appointment.id)}>
    Annuler
  </Button>
</Can>

<Can permission={PERMISSIONS.APPOINTMENTS_CREATE}>
  <AppointmentModal open={createModalOpen} onClose={() => setCreateModalOpen(false)} />
</Can>
```

Le dernier garde-fou est important : ne rendez pas une modale interactive si le profil ne possède pas la permission, même si son bouton déclencheur est déjà masqué. Appliquez cette règle aux actions dans les tables, aux actions mobiles, aux raccourcis clavier et aux composants enfants.

### Étape 6 — Éviter les appels API inutiles ou révélateurs

Une page complète est déjà bloquée par `PermissionRoute`. Lorsqu’un sous-onglet, un panneau secondaire ou une requête complémentaire nécessite une permission plus précise, activez la requête seulement si l’utilisateur est autorisé.

```tsx
const { can } = usePermissions()
const canViewAudit = can(PERMISSIONS.APPOINTMENTS_VIEW_AUDIT)

const auditQuery = useQuery({
  queryKey: ['appointment-audit', appointmentId],
  queryFn: () => appointmentService.listAudit(appointmentId),
  enabled: canViewAudit && Boolean(appointmentId),
})
```

N’essayez pas de filtrer des données sensibles uniquement dans le navigateur. Le service backend doit retourner un jeu de données déjà filtré par tenant et par autorisation. Le frontend ne doit ni appeler un endpoint interdit, ni supprimer des lignes après réception pour simuler une autorisation.

### Étape 7 — Mettre à jour les tests et la documentation du module

Ajoutez des tests de rendu ou de parcours qui couvrent au minimum la consultation autorisée, l’absence de menu pour un rôle non autorisé, la redirection d’une URL directe, l’absence de bouton de mutation et le refus backend d’un appel API non autorisé. Mettez aussi à jour la documentation du module avec la matrice rôle-permission.

## 4. Checklist de revue avant fusion

| Contrôle | Critère d’acceptation |
|---|---|
| Backend | Chaque endpoint concerné possède une règle RBAC explicite et vérifie le tenant. |
| JWT / Keycloak | Tous les rôles utilisés sont réellement émis dans `realm_access.roles`. |
| Référentiel frontend | Chaque nouvelle constante de `PERMISSIONS` a une entrée dans `rolePermissions`. |
| Menu | L’entrée utilise une permission de consultation et n’apparaît pas pour les autres rôles. |
| Route | L’URL est protégée par `PermissionRoute` ou une garde de rôle volontairement justifiée. |
| Actions | Tous les boutons, modales, actions de tableau et actions mobiles sont entourés par `Can`. |
| Données | Les requêtes secondaires utilisent `enabled` si leur permission diffère de celle de la page. |
| Tests | Les scénarios autorisés et refusés sont couverts, y compris l’accès direct par URL. |
| Qualité | `npm run build` et `npm run lint` se terminent sans erreur ni avertissement. |

## 5. Anti-modèles à éviter

| Anti-modèle | Pourquoi il est incorrect | Alternative |
|---|---|---|
| Vérifier un rôle directement dans chaque page | Duplique la matrice et crée des divergences avec le backend. | Déclarer une permission dans `permissions.tsx`, puis employer `Can` ou `usePermissions`. |
| Masquer seulement le menu | L’URL directe reste accessible. | Ajouter un `PermissionRoute`. |
| Protéger seulement le bouton d’ouverture | Une modale ou un composant enfant peut rester rendu. | Protéger le bouton **et** la modale/action cible. |
| Filtrer les données après leur réception | Les données ont déjà été exposées au navigateur. | Filtrer côté backend et désactiver les requêtes frontend non autorisées. |
| Utiliser le rôle transmis par le formulaire | Un client peut modifier son propre payload. | Déduire rôle et tenant exclusivement du JWT côté backend. |
| Ajouter une permission sans test négatif | Une régression peut réafficher une action sensible. | Tester explicitement l’absence de menu, de route et d’action pour un rôle refusé. |

## 6. Convention de contribution

Toute pull request ajoutant un module métier ou une action sensible doit inclure la matrice de permissions, les gardes backend, les contrôles frontend, les tests associés et une mise à jour de la documentation du module. Les reviewers doivent refuser une implémentation qui traite le frontend comme l’unique barrière de sécurité.

## Références internes

- `frontend/src/auth/permissions.tsx` — référentiel, hook `usePermissions` et composant `Can`.
- `frontend/src/App.tsx` — garde `PermissionRoute` et routes protégées.
- `frontend/src/components/layout/AppShell.tsx` — filtrage des menus par permission.
- `docs/IMPLEMENTATION_MODULES_3_4.md` — exemple de matrice RBAC appliquée aux modules LIS, RIS, Banque de sang et Pharmacie.

---

**Responsable de la mise à jour :** toute équipe ajoutant ou modifiant un module frontend eHealth.
