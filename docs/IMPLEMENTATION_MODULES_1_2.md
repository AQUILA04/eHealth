# Documentation d'Implémentation : Modules I et II

**Auteur :** Francis AHONSU

Ce document détaille l'implémentation des modules fondamentaux du Système d'Information Hospitalier (SIH) eHealth, conformément aux spécifications du projet.

## 1. Vue d'ensemble

Dans le cadre de l'architecture Hub-and-Spoke du SIH, nous avons implémenté les deux premiers modules critiques : la Gestion Administrative du Patient (GAP) constituant le Module I, et le Dossier Patient Informatisé (DPI / EMR) représentant le Module II. Un service transverse faisant office de Mock EMPI (Enterprise Master Patient Index) a également été mis en place pour permettre des tests isolés.

L'implémentation a été réalisée en Java 21 avec le framework Spring Boot dans sa version 3.5.14. Pour faciliter le développement et les tests automatisés, un profil `mock` utilisant une base de données H2 en mémoire a été configuré, permettant une exécution rapide sans dépendance à une infrastructure de base de données externe.

## 2. Mock EMPI Service

Bien que l'EMPI complet soit un système complexe souvent géré par une solution externe spécialisée, nous avons développé un serveur fictif (mock) robuste pour permettre le développement autonome des autres modules du système hospitalier.

Ce service gère l'enregistrement centralisé des identités en créant des patients et en générant un identifiant unique universel (UUID global). L'une des fonctionnalités majeures est la déduplication en temps réel. Le système effectue d'abord une recherche de doublons exacts en comparant le nom, le prénom et la date de naissance. Ensuite, il procède à une recherche probabiliste en s'appuyant sur les algorithmes de Jaro-Winkler et de Levenshtein normalisé pour identifier les variantes orthographiques ou les erreurs de saisie. Le service expose également des endpoints REST permettant une recherche textuelle complète sur les données démographiques.

L'ensemble de ces fonctionnalités est couvert par des tests unitaires approfondis, notamment sur la classe `SimilarityUtil` et le service principal, ainsi que par des tests d'intégration validant le comportement des endpoints et la logique de détection des doublons.

## 3. Module I : GAP (Gestion Administrative du Patient)

Le module GAP constitue le point d'entrée du patient dans l'établissement hospitalier. Il a pour rôle principal de gérer l'identité locale du patient ainsi que ses différents mouvements au sein de l'hôpital, concept communément appelé ADT (Admission, Discharge, Transfer).

La gestion des patients est assurée par le `PatientController`, qui permet la création de dossiers administratifs locaux et génère un numéro de dossier médical (MRN) spécifique à l'établissement. Ce contrôleur gère également la liaison de ce dossier local avec l'UUID global fourni par l'EMPI.

La gestion des mouvements est orchestrée par l'`EncounterController`. Lors de l'admission, un épisode de soins est créé et un lit est assigné au patient. Si l'état du patient nécessite un changement de service, la fonction de transfert met à jour sa localisation précise (service, chambre, lit). Enfin, lors de la sortie du patient, l'épisode de soins est clôturé et le lit est marqué comme nécessitant un nettoyage (`BedStatus.CLEANING`). Le module inclut également un tableau de bord permettant de visualiser l'occupation actuelle des lits. Des tests d'intégration simulant un cycle ADT complet valident le bon fonctionnement de ce workflow.

## 4. Module II : DPI (Dossier Patient Informatisé)

Le DPI a pour vocation de centraliser l'intégralité des informations cliniques générées tout au long du séjour du patient.

L'ouverture d'un dossier clinique est systématiquement liée à une admission préalablement enregistrée dans le module GAP via un identifiant de liaison. Ce dossier permet la saisie structurée des antécédents médicaux, des allergies et des notes d'examen physique.

Le suivi du patient implique l'enregistrement régulier de ses constantes vitales (température, tension artérielle, fréquence cardiaque, etc.). Le système intègre un calcul automatique de l'Indice de Masse Corporelle (IMC) lors de la persistance des données. Concernant les prescriptions, le module implémente un système CPOE (Computerized Physician Order Entry) permettant la saisie détaillée des traitements médicamenteux, incluant la posologie, la voie d'administration et la fréquence, tout en assurant un suivi précis du statut de la prescription. Enfin, le DPI gère les demandes d'examens complémentaires, qu'il s'agisse de biologie ou d'imagerie, et permet la saisie ainsi que l'interprétation des résultats.

Une suite de tests d'intégration valide rigoureusement l'enchaînement de ces étapes, depuis l'ouverture du dossier jusqu'à sa clôture, en passant par l'enregistrement des constantes, des prescriptions et des examens.

## 5. Résolution de problèmes techniques

Au cours de la phase d'implémentation et de test, un conflit de versions concernant la bibliothèque JUnit a été identifié. Ce conflit opposait le composant `junit-platform-commons` fourni nativement par Spring Boot 3.5 et le composant `junit-jupiter-engine` dont la version 5.9.3 était imposée par le fichier de configuration parent.

Pour résoudre ce problème, la version de JUnit dans le projet parent a été mise à jour vers la version `5.12.2`. Parallèlement, le plugin `maven-surefire-plugin` a été mis à niveau vers la version `3.5.3` et configuré pour exécuter automatiquement les classes de tests d'intégration. Enfin, le flag de compilation `-parameters` a été ajouté au plugin Maven pour corriger les erreurs de résolution des noms de paramètres rencontrées par les contrôleurs Spring MVC lors de l'exécution des tests.

## 6. Conclusion

Les fondations des Modules I et II sont désormais pleinement opérationnelles, testées et validées. L'architecture modulaire mise en place permet d'envisager sereinement les prochaines étapes du projet, notamment l'intégration avec un véritable système EMPI en production et l'ajout des modules cliniques spécialisés (Laboratoire, Radiologie, Pharmacie) prévus dans la feuille de route.

---
*Ce document a été généré dans le cadre du projet eHealth.*
