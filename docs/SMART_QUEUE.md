# Triage et file d’attente intelligente

Le sous-module Smart Queue ajoute au GAP un mécanisme de passage fondé sur l’acuité clinique et non sur le seul ordre d’arrivée. Le triage reste une décision clinique : le système fournit l’ordre opérationnel, la visibilité des délais et un journal de décisions ; il ne pose pas de diagnostic.

## Workflow

| Étape | Données et résultat |
|---|---|
| Accueil / triage | Patient, service, motif, constantes brèves et niveau P1–P5 validé par un professionnel compétent. |
| Ticket | Numéro anonymisé `Q-AAAAMMJJ-NNN`, remis au patient. |
| File clinique | Les professionnels voient l’identité, le niveau de triage, l’heure d’arrivée et le prochain candidat. |
| Appel | L’action « Appeler le suivant » sélectionne le patient prioritaire et horodate l’appel. |
| Écran public | Seuls le numéro de ticket et le service sont diffusés ; aucun nom, motif, niveau d’urgence ou donnée médicale ne sort du poste clinique. |

## Règle de sélection

La base de priorité est P1 à P5, P1 étant immédiat et P5 correspondant à une consultation de routine. À niveau égal, l’ancienneté est utilisée. Une composante d’aging augmente progressivement la priorité effective des patients non critiques qui attendent longtemps, sans pouvoir dépasser les cas P1/P2. Cette règle ne réalise donc pas une alternance fixe « contrôle puis urgence » : un patient critique est toujours sélectionné avant un contrôle lorsqu’un créneau devient disponible.

## Rôles

| Permission | Rôles | Usage |
|---|---|---|
| `SMART_QUEUE_VIEW` | `MEDECIN`, `INFIRMIER`, `ADMIN_GAP`, `SUPER_ADMIN` | Consultation du poste clinique. |
| `SMART_QUEUE_CALL_NEXT` | `MEDECIN`, `INFIRMIER`, `SUPER_ADMIN` | Appel du prochain patient. |

L’écran `/queue-display` est volontairement anonymisé. Son déploiement réel doit utiliser une session d’affichage dédiée, restreinte au réseau hospitalier et à un service configuré.

## API

| Endpoint | Fonction |
|---|---|
| `POST /api/v1/gap/queue/check-in` | Création du ticket après triage. |
| `GET /api/v1/gap/queue?serviceArea=...` | Vue clinique de la file et du prochain candidat. |
| `POST /api/v1/gap/queue/next?serviceArea=...&clinicianId=...` | Sélection et appel du prochain ticket. |
| `GET /api/v1/gap/queue/display?serviceArea=...` | Projection publique anonymisée. |

Les tickets sont tenant-isolés à partir du `TenantContext`. Les tests GAP existants, le build et le lint frontend ont été exécutés après l’intégration. Le moteur Docker n’étant pas disponible, aucun démarrage Compose complet n’est revendiqué.
