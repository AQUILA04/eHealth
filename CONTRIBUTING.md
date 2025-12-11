# Guide de Contribution - eHealth

Merci de votre intérêt pour contribuer au projet eHealth ! Ce document fournit les directives pour contribuer au projet de manière efficace et cohérente.

## 📋 Table des matières

- [Code de conduite](#code-de-conduite)
- [Comment commencer](#comment-commencer)
- [Processus de contribution](#processus-de-contribution)
- [Standards de code](#standards-de-code)
- [Conventions de commit](#conventions-de-commit)
- [Tests](#tests)
- [Documentation](#documentation)
- [Pull Requests](#pull-requests)
- [Signaler des bugs](#signaler-des-bugs)
- [Suggérer des améliorations](#suggérer-des-améliorations)

---

## 🤝 Code de conduite

Nous nous engageons à fournir un environnement accueillant et inclusif. Tous les contributeurs doivent respecter les principes suivants:

- **Respect:** Traitez tous les contributeurs avec respect et courtoisie
- **Inclusion:** Accueillez les personnes de tous horizons et niveaux d'expérience
- **Collaboration:** Travaillez ensemble pour atteindre les objectifs du projet
- **Intégrité:** Maintenez les normes éthiques et professionnelles les plus élevées

---

## 🚀 Comment commencer

### 1. Forker le repository

```bash
# Visiter https://github.com/AQUILA04/eHealth et cliquer sur "Fork"
```

### 2. Cloner votre fork

```bash
git clone https://github.com/VOTRE_USERNAME/eHealth.git
cd eHealth
```

### 3. Ajouter le remote upstream

```bash
git remote add upstream https://github.com/AQUILA04/eHealth.git
git fetch upstream
```

### 4. Créer une branche de travail

```bash
git checkout -b feature/ma-fonctionnalite
```

---

## 📝 Processus de contribution

### Étape 1: Préparer votre environnement

```bash
# Installer les dépendances
pnpm install
mvn clean install -DskipTests

# Démarrer l'infrastructure
pnpm docker:up

# Vérifier que tout fonctionne
pnpm test
```

### Étape 2: Créer votre branche

Suivez la convention Git Flow:

```bash
# Pour une nouvelle fonctionnalité
git checkout -b feature/description-courte

# Pour une correction de bug
git checkout -b bugfix/description-courte

# Pour une correction urgente en production
git checkout -b hotfix/description-courte
```

### Étape 3: Développer et tester

```bash
# Développer votre fonctionnalité
# Exécuter les tests régulièrement
pnpm test
mvn test

# Vérifier le linting
pnpm lint
```

### Étape 4: Commiter vos changements

Utilisez les conventions de commit (voir section ci-dessous):

```bash
git add .
git commit -m "feat: ajout de la nouvelle fonctionnalité"
```

### Étape 5: Pousser et créer une Pull Request

```bash
# Pousser votre branche
git push origin feature/ma-fonctionnalite

# Créer une PR via GitHub UI
# Remplir le template de PR avec les détails
```

### Étape 6: Répondre aux reviews

- Répondez aux commentaires des reviewers
- Faites les modifications demandées
- Committez les changements avec `git commit --amend`
- Poussez les changements avec `git push --force-with-lease`

### Étape 7: Merger

Une fois approuvé, votre PR sera mergée dans `develop` ou `main`.

---

## 💻 Standards de code

### Java

**Conventions:**
- Packages: `com.sih.{service}.{module}`
- Classes: PascalCase (ex: `EmpiService`)
- Méthodes: camelCase (ex: `getPatientById()`)
- Constantes: UPPER_SNAKE_CASE

**Exemple:**
```java
@Service
@RequiredArgsConstructor
public class PatientService {
    private final PatientRepository patientRepository;
    
    public Patient getPatientById(String id) {
        return patientRepository.findById(id)
            .orElseThrow(() -> new PatientNotFoundException(id));
    }
}
```

### TypeScript/JavaScript

**Conventions:**
- Fichiers: kebab-case (ex: `patient.service.ts`)
- Classes/Interfaces: PascalCase (ex: `PatientService`)
- Fonctions/Variables: camelCase (ex: `getPatientById()`)
- Constantes: UPPER_SNAKE_CASE

**Exemple:**
```typescript
@Injectable()
export class PatientService {
  constructor(private readonly patientRepository: PatientRepository) {}

  getPatientById(id: string): Promise<Patient> {
    return this.patientRepository.findById(id);
  }
}
```

### Linting et Formatage

```bash
# Vérifier le linting
pnpm lint
mvn checkstyle:check

# Corriger automatiquement
pnpm lint:fix

# Formater le code
pnpm format
```

---

## 📌 Conventions de commit

Nous utilisons [Conventional Commits](https://www.conventionalcommits.org/):

### Format

```
<type>(<scope>): <subject>

<body>

<footer>
```

### Types

- **feat:** Nouvelle fonctionnalité
- **fix:** Correction de bug
- **docs:** Changements de documentation
- **style:** Formatage, points-virgules, etc. (pas de logique)
- **refactor:** Refactorisation du code (pas de changement fonctionnel)
- **perf:** Amélioration de performance
- **test:** Ajout ou modification de tests
- **chore:** Maintenance, dépendances, etc.
- **ci:** Changements CI/CD

### Exemples

```bash
# Nouvelle fonctionnalité
git commit -m "feat(empi): ajouter la recherche par biométrie"

# Correction de bug
git commit -m "fix(dpi): corriger la validation du formulaire clinique"

# Documentation
git commit -m "docs: mettre à jour le guide d'installation"

# Refactorisation
git commit -m "refactor(api-gateway): simplifier la logique d'authentification"
```

---

## 🧪 Tests

### Couverture minimale

- **Services critiques:** 80% minimum
- **Services secondaires:** 60% minimum
- **Frontend:** 70% minimum

### Exécuter les tests

```bash
# Tous les tests
pnpm test
mvn test

# Tests spécifiques
cd services/java/empi-service && mvn test
cd services/nodejs/api-gateway && pnpm test

# Avec couverture
pnpm test:coverage
mvn test jacoco:report
```

### Écrire des tests

**Java:**
```java
@SpringBootTest
class PatientServiceTest {
    @MockBean
    private PatientRepository patientRepository;
    
    @InjectMocks
    private PatientService patientService;
    
    @Test
    void testGetPatientById() {
        // Arrange
        Patient patient = new Patient();
        when(patientRepository.findById("123")).thenReturn(Optional.of(patient));
        
        // Act
        Patient result = patientService.getPatientById("123");
        
        // Assert
        assertNotNull(result);
        verify(patientRepository).findById("123");
    }
}
```

**TypeScript:**
```typescript
describe('PatientService', () => {
  let service: PatientService;
  let repository: PatientRepository;

  beforeEach(() => {
    repository = mock(PatientRepository);
    service = new PatientService(repository);
  });

  it('should get patient by id', async () => {
    // Arrange
    const patient = { id: '123', name: 'John' };
    when(repository.findById('123')).thenResolve(patient);

    // Act
    const result = await service.getPatientById('123');

    // Assert
    expect(result).toEqual(patient);
    verify(repository).findById('123');
  });
});
```

---

## 📚 Documentation

### Documenter votre code

**Java:**
```java
/**
 * Récupère un patient par son identifiant.
 *
 * @param id l'identifiant unique du patient
 * @return le patient trouvé
 * @throws PatientNotFoundException si le patient n'existe pas
 */
public Patient getPatientById(String id) {
    // ...
}
```

**TypeScript:**
```typescript
/**
 * Récupère un patient par son identifiant.
 * @param id - L'identifiant unique du patient
 * @returns Le patient trouvé
 * @throws PatientNotFoundException si le patient n'existe pas
 */
getPatientById(id: string): Promise<Patient> {
    // ...
}
```

### Mettre à jour la documentation

- Mettez à jour le README si vous ajoutez une nouvelle fonctionnalité
- Ajoutez des commentaires pour la logique complexe
- Documentez les API dans `contract/`
- Créez des fichiers de documentation si nécessaire

---

## 🔀 Pull Requests

### Template de PR

```markdown
## Description
Brève description des changements

## Type de changement
- [ ] Correction de bug
- [ ] Nouvelle fonctionnalité
- [ ] Changement cassant
- [ ] Documentation

## Services affectés
- [ ] EMPI Service
- [ ] DPI Service
- [ ] API Gateway
- [ ] Frontend
- [ ] Infrastructure

## Checklist
- [ ] Mon code suit les conventions du projet
- [ ] J'ai exécuté les tests localement
- [ ] J'ai ajouté des tests pour mes changements
- [ ] J'ai mis à jour la documentation
- [ ] Pas de dépendances non autorisées
- [ ] Pas de secrets/credentials exposés

## Tests effectués
Décrivez les tests que vous avez exécutés

## Lien vers les issues
Closes #123
```

### Critères d'approbation

Une PR doit satisfaire:
1. ✅ Tous les tests passent
2. ✅ Code review approuvée
3. ✅ Pas de conflits de merge
4. ✅ Couverture de tests suffisante
5. ✅ Documentation à jour

---

## 🐛 Signaler des bugs

### Template de bug

```markdown
## Description du bug
Description claire et concise du bug

## Étapes pour reproduire
1. Aller à...
2. Cliquer sur...
3. Observer le comportement...

## Comportement attendu
Description de ce qui devrait se passer

## Comportement actuel
Description de ce qui se passe réellement

## Environnement
- OS: [ex: Ubuntu 22.04]
- Java: [ex: 17.0.1]
- Node.js: [ex: 20.x]
- Service affecté: [ex: EMPI Service]

## Logs/Screenshots
Attachez les logs ou screenshots pertinents

## Contexte supplémentaire
Toute information supplémentaire utile
```

---

## 💡 Suggérer des améliorations

### Template de suggestion

```markdown
## Description de l'amélioration
Description claire de l'amélioration proposée

## Motivation
Pourquoi cette amélioration est-elle nécessaire?

## Solution proposée
Description de la solution proposée

## Alternatives considérées
Autres solutions envisagées

## Impact potentiel
Quel serait l'impact de cette amélioration?
```

---

## 📞 Questions?

- **Discussions:** https://github.com/AQUILA04/eHealth/discussions
- **Issues:** https://github.com/AQUILA04/eHealth/issues

Merci de contribuer à eHealth! 🙏
