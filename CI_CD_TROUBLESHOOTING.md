# CI/CD Troubleshooting Guide

**Date:** 11 Décembre 2025  
**Version:** 1.0  
**Purpose:** Documenter les erreurs communes et leurs solutions

---

## 🔴 Erreur 1: pnpm not found in GitHub Actions

### ❌ Problème
```
Error: Unable to locate executable file: pnpm. Please verify either the file path 
exists or the file can be found within a directory specified by the PATH environment 
variable. Also check the file mode to verify the file is executable.
```

### 🔍 Cause
L'ordre des étapes dans GitHub Actions est incorrect. `setup-node` doit être appelé **APRÈS** `pnpm/action-setup`, sinon pnpm n'est pas disponible dans le PATH.

### ✅ Solution

**MAUVAIS ordre:**
```yaml
- name: Setup Node.js
  uses: actions/setup-node@v4
  with:
    node-version: '20'
    cache: 'pnpm'  # ❌ pnpm n'existe pas encore!

- name: Install pnpm
  uses: pnpm/action-setup@v2
  with:
    version: 8
```

**BON ordre:**
```yaml
- name: Install pnpm
  uses: pnpm/action-setup@v2
  with:
    version: 8

- name: Setup Node.js
  uses: actions/setup-node@v4
  with:
    node-version: '20'
    cache: 'pnpm'  # ✅ pnpm existe maintenant
```

### 🔧 Configuration complète
```yaml
steps:
  - name: Checkout code
    uses: actions/checkout@v4

  # ÉTAPE 1: Installer pnpm en premier
  - name: Install pnpm
    uses: pnpm/action-setup@v2
    with:
      version: 8

  # ÉTAPE 2: Setup Node.js après pnpm
  - name: Setup Node.js
    uses: actions/setup-node@v4
    with:
      node-version: '20'
      cache: 'pnpm'

  # ÉTAPE 3: Installer les dépendances
  - name: Install dependencies
    run: pnpm install --no-frozen-lockfile
```

### 📝 Notes importantes
- **Toujours installer pnpm AVANT setup-node**
- Utiliser `--no-frozen-lockfile` en CI si `pnpm-lock.yaml` n'existe pas
- Utiliser `--frozen-lockfile` en production (lockfile doit exister)

---

## 🔴 Erreur 2: pnpm-lock.yaml not found

### ❌ Problème
```
Error: ENOENT: no such file or directory, open '/home/ubuntu/eHealth/pnpm-lock.yaml'
```

### 🔍 Cause
Le fichier `pnpm-lock.yaml` n'existe pas dans le repository. Par défaut, pnpm en CI utilise `--frozen-lockfile` qui échoue si le lockfile n'existe pas.

### ✅ Solutions

**Option 1: Utiliser `--no-frozen-lockfile` en CI** (Recommandé pour MVP)
```yaml
- name: Install dependencies
  run: pnpm install --no-frozen-lockfile
```

**Option 2: Générer le lockfile localement et le committer**
```bash
# Localement
pnpm install
git add pnpm-lock.yaml
git commit -m "chore: add pnpm lock file"
git push
```

**Option 3: Générer le lockfile en CI et l'uploader**
```yaml
- name: Install dependencies
  run: pnpm install

- name: Upload lock file
  uses: actions/upload-artifact@v3
  with:
    name: pnpm-lock
    path: pnpm-lock.yaml
```

### 📝 Recommandation
Pour un monorepo en développement actif:
- Utiliser `--no-frozen-lockfile` en CI
- Générer et committer `pnpm-lock.yaml` en local
- En production, utiliser `--frozen-lockfile` avec lockfile commité

---

## 🔴 Erreur 3: Maven build fails with missing dependencies

### ❌ Problème
```
[ERROR] Failed to execute goal on project empi-service: 
Could not find artifact com.sih:types-java:jar:1.0.0
```

### 🔍 Cause
Les dépendances entre modules Maven ne sont pas résolues. Le module `types-java` doit être compilé avant les services qui en dépendent.

### ✅ Solution

**Utiliser le pom.xml parent avec modules:**
```xml
<project>
  <modelVersion>4.0.0</modelVersion>
  <groupId>com.sih</groupId>
  <artifactId>ehealth-parent</artifactId>
  <version>1.0.0</version>
  <packaging>pom</packaging>

  <modules>
    <module>shared/types-java</module>
    <module>services/java/empi-service</module>
    <module>services/java/dpi-service</module>
    <!-- ... -->
  </modules>
</project>
```

**Build Maven en CI:**
```yaml
- name: Build with Maven
  run: mvn clean package -DskipTests
```

Maven résoudra automatiquement l'ordre de compilation.

---

## 🔴 Erreur 4: Node.js workspace resolution fails

### ❌ Problème
```
Error: Cannot find module '@sih/types-ts'
```

### 🔍 Cause
Les workspaces pnpm ne sont pas configurés correctement ou les dépendances ne sont pas installées.

### ✅ Solution

**Vérifier pnpm-workspace.yaml:**
```yaml
packages:
  - 'services/java/*'
  - 'services/nodejs/*'
  - 'services/frontend'
  - 'shared/*'
```

**Vérifier les package.json des services:**
```json
{
  "name": "@sih/api-gateway",
  "version": "1.0.0",
  "dependencies": {
    "@sih/types-ts": "workspace:*"
  }
}
```

**En CI:**
```yaml
- name: Install dependencies
  run: pnpm install --no-frozen-lockfile

- name: Build
  run: pnpm build --filter="...@sih/api-gateway"
```

---

## 🔴 Erreur 5: Docker build fails in CI

### ❌ Problème
```
failed to solve with frontend dockerfile.v0: failed to build LLM model
```

### 🔍 Cause
Les artifacts de build ne sont pas disponibles ou les chemins sont incorrects.

### ✅ Solution

**Utiliser les artifacts uploadés:**
```yaml
- name: Download Java artifacts
  uses: actions/download-artifact@v3
  with:
    name: java-build-artifacts
    path: services/java/

- name: Build Docker image
  run: docker build -f infrastructure/docker/Dockerfile.java -t ehealth-empi .
```

**Ou utiliser le multi-stage build:**
```dockerfile
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre
COPY --from=builder /app/services/java/empi-service/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
```

---

## 🔴 Erreur 6: Tests timeout in CI

### ❌ Problème
```
Error: Test timeout exceeded 30000ms
```

### 🔍 Cause
Les tests prennent trop de temps en CI (pas d'optimisation, pas de cache, etc.)

### ✅ Solution

**Augmenter le timeout:**
```yaml
- name: Run tests
  run: pnpm test
  timeout-minutes: 10
```

**Ou utiliser des test containers:**
```yaml
services:
  postgres:
    image: postgres:16
    env:
      POSTGRES_PASSWORD: test
    options: >-
      --health-cmd pg_isready
      --health-interval 10s
      --health-timeout 5s
      --health-retries 5
```

**Ou sauter les tests en CI:**
```yaml
- name: Build
  run: mvn clean package -DskipTests
```

---

## 🟢 Checklist pour éviter les erreurs

### Avant de pousser sur GitHub

- [ ] `pnpm install` fonctionne localement
- [ ] `mvn clean package` fonctionne localement
- [ ] `pnpm build` fonctionne localement
- [ ] `pnpm test` fonctionne localement
- [ ] Pas de fichiers `.env` commités
- [ ] Pas de dépendances non déclarées
- [ ] Tous les modules sont dans `pnpm-workspace.yaml`
- [ ] Tous les modules sont dans `pom.xml` parent

### Configuration CI/CD

- [ ] pnpm installé AVANT setup-node
- [ ] `--no-frozen-lockfile` utilisé si pas de lockfile
- [ ] Artifacts uploadés et téléchargés correctement
- [ ] Timeouts configurés correctement
- [ ] Services (DB, cache) démarrés si nécessaire
- [ ] Secrets configurés dans GitHub
- [ ] Branches protégées configurées
- [ ] Notifications configurées

---

## 🔧 Template de workflow correct

```yaml
name: CI/CD Pipeline

on:
  push:
    branches: [main, develop, feature/**, bugfix/**, release/**]
  pull_request:
    branches: [main, develop, release/**]

jobs:
  lint-and-format:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      
      # ✅ pnpm AVANT setup-node
      - uses: pnpm/action-setup@v2
        with:
          version: 8
      
      - uses: actions/setup-node@v4
        with:
          node-version: '20'
          cache: 'pnpm'
      
      - run: pnpm install --no-frozen-lockfile
      - run: pnpm lint
      - run: pnpm format:check

  build-java:
    runs-on: ubuntu-latest
    needs: lint-and-format
    steps:
      - uses: actions/checkout@v4
      
      - uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
          cache: maven
      
      - run: mvn clean package -DskipTests
      
      - uses: actions/upload-artifact@v3
        with:
          name: java-artifacts
          path: services/java/*/target/
          retention-days: 1

  build-nodejs:
    runs-on: ubuntu-latest
    needs: lint-and-format
    steps:
      - uses: actions/checkout@v4
      
      # ✅ pnpm AVANT setup-node
      - uses: pnpm/action-setup@v2
        with:
          version: 8
      
      - uses: actions/setup-node@v4
        with:
          node-version: '20'
          cache: 'pnpm'
      
      - run: pnpm install --no-frozen-lockfile
      - run: pnpm build
      
      - uses: actions/upload-artifact@v3
        with:
          name: nodejs-artifacts
          path: services/nodejs/*/dist/
          retention-days: 1
```

---

## 📚 Ressources

- [GitHub Actions Documentation](https://docs.github.com/en/actions)
- [pnpm GitHub Action](https://github.com/pnpm/action-setup)
- [Maven GitHub Action](https://github.com/actions/setup-java)
- [Node.js GitHub Action](https://github.com/actions/setup-node)

---

**Document créé pour éviter les erreurs CI/CD répétitives! 📋**

*Dernière mise à jour: 11 Décembre 2025*
