# Approfondissements — Modules I et II

## GAP / ADT

Le parcours hospitalier renforce désormais la sécurité d’occupation des lits. L’admission et le transfert vérifient qu’aucun séjour `IN_PROGRESS` n’occupe déjà le même couple service, chambre et lit. Une sortie place toujours le lit en état `CLEANING`; l’endpoint `PATCH /api/v1/gap/encounters/{id}/bed-ready` est la seule transition qui le rend `AVAILABLE`, après validation du bio-nettoyage.

## DPI / sécurité clinique

La réponse de constantes comprend maintenant `criticalAlerts`, une liste calculée au moment de la restitution. Les seuils couverts sont la saturation en oxygène inférieure à 90 %, les fréquences cardiaque et respiratoire critiques, la pression systolique critique, l’hyperthermie et la douleur sévère. Les alertes sont visibles dans la page frontend des constantes, sans être persistées comme diagnostic.

## Validation

Les tests Maven GAP/DPI, le build et le lint frontend ont été exécutés avec succès. Docker Compose n’a pas été démarré car le moteur Docker n’est pas disponible dans l’environnement.
